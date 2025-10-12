package com.example.backendseller.service;

import com.example.backendseller.dto.CreateEtsyProductRequest;
import com.example.backendseller.dto.EtsyProductDTO;
import com.example.backendseller.dto.EtsyVariationDTO;
import com.example.backendseller.entity.*;
import com.example.backendseller.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EtsyProductService {

    private final EtsyProductRepository productRepository;
    private final TagRepository tagRepository;
    private final ProductTypeService productTypeService;
    private final AmazonAccountService amazonAccountService;
    private final EtsyShopService etsyShopService;

    /**
     * Tạo hoặc cập nhật sản phẩm Etsy
     * - Tối ưu: Sử dụng batch query để tìm tags hiện có
     * - Không trùng lặp: Kiểm tra và tái sử dụng tags, product type
     * - Transaction: Đảm bảo dữ liệu nhất quán
     * - Hỗ trợ manual ID từ Etsy API
     */
    @Transactional
    @CacheEvict(
            value = {"etsyProducts"},
            allEntries = true
    )
    public EtsyProduct saveProduct(CreateEtsyProductRequest dto) {
        log.info("Received request to save product with dto: {}", dto);

        if (dto.getId() == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        // Kiểm tra sản phẩm đã tồn tại chưa
        if (productRepository.existsById(dto.getId())) {
            log.warn("Product with ID {} already exists, updating...", dto.getId());
            throw new IllegalArgumentException("Product with ID " + dto.getId() + " already exists");
        }
        try {
            // 1. Tạo hoặc lấy Product Type
            String productTypeName = dto.getProductType();
            ProductType productType = getProductType(productTypeName);
            log.info("Saving productType: {} with ID: {}", productType.getName(), productType.getId());

            // 2. Tạo product mới
            EtsyProduct product = new EtsyProduct();

            // Set ID từ DTO (manual assignment)
            product.setId(dto.getId());
            product.setTitle(dto.getTitle());
            product.setDescription(dto.getDescription());
            product.setPrice(dto.getPrice());
            product.setMaterial(dto.getMaterial());
            product.setProductType(productType);
            product.setGenerateStatus(EtsyProduct.GenerateStatus.PENDING);

            product.setAmazonAccount(amazonAccountService.getAccountById(dto.getAcc()));

            EtsyShop etsyShop = getOrCreateEtsyShop(dto.getShop());
            product.setEtsyShop(etsyShop);


            // 3. Xử lý Tags (tối ưu với batch query)
            List<Tag> tags;
            if (dto.getTags() == null || dto.getTags().isEmpty()) {
                List<String> tagsFromDMM = getTagsFromDMM(dto.getId(), dto.getUrl(), dto.getShop().getName());
                tags = getOrCreateTags(tagsFromDMM);
            } else {
                tags = getOrCreateTags(dto.getTags());
            }
            product.setTags(tags);

            // 4. Xử lý Images
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                List<EtsyImage> images = createImages(dto.getImages(), product);
                product.setEtsyImages(images);
            }

            // 5. Xử lý Personalizations
            if (dto.getPersonalization() != null && !dto.getPersonalization().isEmpty()) {
                List<EtsyProductPersonalization> personalizations =
                        createPersonalizations(dto.getPersonalization(), product);
                product.setPersonalizations(personalizations);
            }

            // 6. Xử lý Variations
            if (dto.getVariation() != null && !dto.getVariation().isEmpty()) {
                List<EtsyVariation> variations = createVariations(dto.getVariation(), product);
                product.setVariations(variations);
            }

            // 7. Lưu product (cascade sẽ tự động lưu các entities liên quan)
            EtsyProduct savedProduct = productRepository.save(product);
            log.info("Product saved successfully with ID: {}", savedProduct.getId());

            return savedProduct;
        }  catch (Exception e) {
            log.error("Error while saving product: {} with ID: {}", e.getMessage(), dto.getId());
            throw e;
        }
    }

    private EtsyShop getOrCreateEtsyShop(CreateEtsyProductRequest.Shop shopDto) {
        if (shopDto == null || shopDto.getName() == null || shopDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Shop information is required");
        }

        String shopName = shopDto.getName().trim();
        log.info("Checking if shop exists: {}", shopName);

        try {
            // Tìm shop nếu đã tồn tại (có cache @Cacheable)
            EtsyShop existingShop = etsyShopService.searchByName(shopName);
            log.info("Shop already exists: {}", shopName);

            if (existingShop.getAccept() == null || !existingShop.getAccept()) {
                throw new IllegalArgumentException("Product from this shop is not acceptable");
            }

            return existingShop;
        } catch (RuntimeException e) {
            // Shop không tồn tại, cần tạo mới
            log.info("Shop not found, creating new shop: {} - Reason: {}", shopName, e.getMessage());

            try {
                // Gọi hàm create của service - nó sẽ xử lý Transactional riêng
                EtsyShop newShop = etsyShopService.create(
                        shopName,
                        shopDto.getUrl() != null ? shopDto.getUrl().trim() : "",
                        shopDto.getAvatarUrl() != null ? shopDto.getAvatarUrl().trim() : "",
                        Boolean.TRUE
                );
                log.info("New shop created successfully: {}", shopName);
                return newShop;
            } catch (RuntimeException createException) {
                log.warn("Failed to create new shop: {} - Error: {}", shopName, createException.getMessage());

                // Nếu shop đã tồn tại (tạo bởi request khác), thử lấy lại
                Optional<EtsyShop> existingShopOpt = etsyShopService.findByName(shopName);
                if (existingShopOpt.isPresent()) {
                    log.info("Shop found in retry: {}", shopName);
                    EtsyShop shop = existingShopOpt.get();
                    if (shop.getAccept() == null || !shop.getAccept()) {
                        throw new IllegalArgumentException("Product from this shop is not acceptable");
                    }
                    return shop;
                }

                // Nếu vẫn không tìm thấy, throw exception
                log.error("Failed to get or create shop: {}", shopName, createException);
                throw new RuntimeException("Failed to get or create shop: " + shopName, createException);
            }
        }
    }
    /**
     * Lấy tất cả sản phẩm với phân trang
     */
    @Cacheable(
            value = "etsyProducts",
            key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort",
            unless = "#result.isEmpty()"
    )
    public List<EtsyProduct> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pageable: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAll(pageable).getContent();
    }

    /**
     * Lấy hoặc tạo mới ProductType
     * - Tránh trùng lặp bằng cách kiểm tra tên
     */
    private ProductType getProductType(String typeName) {
        return productTypeService.getProductTypeByName(typeName);
    }

    private List<String> getTagsFromDMM(Long id, String url, String shopName){
        List<String> tags = new ArrayList<>();
        try {
            // Encode URL param để tránh lỗi ký tự đặc biệt
            String encodedShopName = URLEncoder.encode(shopName, StandardCharsets.UTF_8);
            String encodedListingUrl = URLEncoder.encode(url, StandardCharsets.UTF_8);

            String fullUrl = String.format(
                    "https://extension.dmmetsy.com/listing-embed/%d?shop_name=%s&url=%s",
                    id, encodedShopName, encodedListingUrl
            );

            Document doc = Jsoup.connect(fullUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            // Lấy tất cả span có class label-default trong span#tags_field
            Elements tagElements = doc.select("span#tags_field span.label.label-default");

            for (Element tagElement : tagElements) {
                String tagText = tagElement.text().trim();
                if (!tagText.isEmpty()) {
                    tags.add(tagText);
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
        log.info("Tags found: {}", tags);
        return tags;
    }

    /**
     * Lấy hoặc tạo mới Tags
     * - Tối ưu: Sử dụng 1 query duy nhất để tìm tất cả tags hiện có
     * - Chỉ tạo mới những tags chưa tồn tại
     */
    private List<Tag> getOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }

        // Normalize và loại bỏ trùng lặp
        Set<String> uniqueTagNames = tagNames.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (uniqueTagNames.isEmpty()) {
            return new ArrayList<>();
        }

        // Tìm tất cả tags hiện có trong 1 query duy nhất (tối ưu)
        List<Tag> existingTags = tagRepository.findByNameIn(uniqueTagNames);
        log.info("Existing tags found: {}", existingTags.size());

        // Xác định tags nào cần tạo mới
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        Set<String> newTagNames = uniqueTagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .collect(Collectors.toSet());

        // Tạo mới tags chưa tồn tại
        List<Tag> newTags = newTagNames.stream()
                .map(name -> {
                    Tag tag = new Tag();
                    tag.setName(name);
                    return tag;
                })
                .collect(Collectors.toList());

        // Lưu batch tags mới (tối ưu với saveAll)
        if (!newTags.isEmpty()) {
            newTags = tagRepository.saveAll(newTags);
            log.info("Created {} new tags", newTags.size());
        }

        // Kết hợp tags hiện có và mới tạo
        List<Tag> allTags = new ArrayList<>(existingTags);
        allTags.addAll(newTags);

        return allTags;
    }

    /**
     * Tạo danh sách images cho product
     */
    private List<EtsyImage> createImages(List<String> imageUrls, EtsyProduct product) {
        List<EtsyImage> images = new ArrayList<>();

        for (int i = 0; i < imageUrls.size(); i++) {
            String url = imageUrls.get(i);
            if (StringUtils.hasText(url)) {
                EtsyImage image = new EtsyImage();
                image.setUrl(url.trim());
                image.setDisplayOrder(i + 1);
                image.setEtsyProduct(product);
                images.add(image);
                log.info("Save image with id {}, url: {}", image.getId(), url);
            }

        }

        return images;
    }

    /**
     * Tạo danh sách personalizations cho product
     */
    private List<EtsyProductPersonalization> createPersonalizations(
            List<String> personalizationContents, EtsyProduct product) {
        List<EtsyProductPersonalization> personalizations = new ArrayList<>();

        for (int i = 0; i < personalizationContents.size(); i++) {
            String content = personalizationContents.get(i);
            if (StringUtils.hasText(content)) {
                EtsyProductPersonalization personalization = new EtsyProductPersonalization();
                personalization.setContent(content.trim());
                personalization.setLineOrder(i + 1);
                personalization.setEtsyProduct(product);
                personalizations.add(personalization);
            }
        }
        log.info("Create personalizations found: {}", personalizations.size());

        return personalizations;
    }

    /**
     * Tạo danh sách variations và options cho product
     */
    private List<EtsyVariation> createVariations(
            List<EtsyVariationDTO> variationDTOs, EtsyProduct product) {
        List<EtsyVariation> variations = new ArrayList<>();

        for (EtsyVariationDTO dto : variationDTOs) {
            if (!StringUtils.hasText(dto.getLabel())) {
                continue;
            }

            EtsyVariation variation = new EtsyVariation();
            variation.setLabel(dto.getLabel().trim());
            variation.setEtsyProduct(product);

            // Tạo options cho variation
            if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
                List<EtsyVariationOption> options = new ArrayList<>();
                for (int i = 0; i < dto.getOptions().size(); i++) {
                    String optionLabel = dto.getOptions().get(i);
                    if (StringUtils.hasText(optionLabel)) {
                        EtsyVariationOption option = new EtsyVariationOption();
                        option.setLabel(optionLabel.trim());
                        option.setEtsyVariation(variation);
                        options.add(option);
                    }
                }
                variation.setVariationOptions(options);
            }

            variations.add(variation);
        }

        return variations;
    }

    /**
     * Cập nhật trạng thái generate
     */
    @Transactional
    public void updateGenerateStatus(Long productId, EtsyProduct.GenerateStatus status) {
        EtsyProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        product.setGenerateStatus(status);
        productRepository.save(product);
        log.info("Updated product {} status to {}", productId, status);
    }

    /**
     * Xóa sản phẩm
     */
    @Transactional
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found: " + productId);
        }

        productRepository.deleteById(productId);
        log.info("Deleted product: {}", productId);
    }

    /**
     * Lấy thông tin sản phẩm
     */
    @Transactional(readOnly = true)
    public EtsyProduct getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    }

    /**
     * Lấy tất cả sản phẩm
     */
    @Transactional(readOnly = true)
    public List<EtsyProduct> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Lấy tất cả sản phẩm có status = PENDING
     */
    @Transactional(readOnly = true)
    public List<EtsyProduct> getPendingProducts() {
        List<EtsyProduct> pendingProducts = productRepository.findByGenerateStatus(
                EtsyProduct.GenerateStatus.PENDING
        );
        log.info("Found {} pending products", pendingProducts.size());
        return pendingProducts;
    }

    /**
     * Lấy sản phẩm theo status
     */
    @Transactional(readOnly = true)
    public List<EtsyProduct> getProductsByStatus(EtsyProduct.GenerateStatus status) {
        List<EtsyProduct> products = productRepository.findByGenerateStatus(status);
        log.info("Found {} products with status {}", products.size(), status);
        return products;
    }

    /**
     * Đếm số lượng sản phẩm pending
     */
    @Transactional(readOnly = true)
    public long countPendingProducts() {
        long count = productRepository.countByGenerateStatus(
                EtsyProduct.GenerateStatus.PENDING
        );
        log.info("Total pending products: {}", count);
        return count;
    }
}