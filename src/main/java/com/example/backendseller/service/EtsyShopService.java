package com.example.backendseller.service;

import com.example.backendseller.entity.EtsyShop;
import com.example.backendseller.repository.EtsyShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EtsyShopService {
    private final EtsyShopRepository etsyShopRepository;

    /**
     * Tìm kiếm shop theo tên (có cache)
     */
    @Cacheable(
            value = "etsyShopByName",
            key = "#shopName",
            unless = "#result == null"
    )
    public EtsyShop searchByName(String shopName) {
        if (shopName == null || shopName.trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide shop name");
        }

        return etsyShopRepository.findByNameContainingIgnoreCase(shopName.trim())
                .orElseThrow(() -> new RuntimeException("Shop not found with name: " + shopName));
    }

    /**
     * Tìm kiếm shop theo tên, trả về Optional (an toàn hơn)
     */
    public Optional<EtsyShop> findByName(String shopName) {
        if (shopName == null || shopName.trim().isEmpty()) {
            return Optional.empty();
        }
        return etsyShopRepository.findByNameContainingIgnoreCase(shopName.trim());
    }

    /**
     * Lấy tất cả shop (có cache)
     */
    @Cacheable(
            value = "etsyShops",
            key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize",
            unless = "#result == null || result.isEmpty()"
    )
    public List<EtsyShop> getAllShops(Pageable pageable) {
        log.info("Fetching all shops with pageable: {}", pageable);
        return etsyShopRepository.findAll(pageable).getContent();
    }

    /**
     * Lấy tất cả shop (không cache)
     */
    public Page<EtsyShop> getAllShopsPage(Pageable pageable) {
        return etsyShopRepository.findAll(pageable);
    }

    /**
     * Tạo shop mới
     */
    @CacheEvict(
            value = {"etsyShops", "etsyShopByName"},
            allEntries = true
    )
    public EtsyShop create(String name, String url, String avatarUrl, Boolean accept) {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Shop name is required");
            }

            if (url == null || url.trim().isEmpty()) {
                throw new IllegalArgumentException("Shop url is required");
            }

            // Kiểm tra shop đã tồn tại
            if (etsyShopRepository.existsByNameIgnoreCase(name.trim())) {
                throw new RuntimeException("Shop name already exists: " + name);
            }
            EtsyShop etsyShop = new EtsyShop();
            etsyShop.setName(name);
            etsyShop.setUrl(url);
            etsyShop.setAvatarUrl(avatarUrl);
            etsyShop.setAccept(accept);
            log.info("Creating new shop: {}", name);
            return etsyShopRepository.save(etsyShop);
        } catch (Exception e) {
            log.error("Exception in create shop, NOT throwing to preserve parent transaction", e);
            // Không throw exception để parent transaction không bị mark as rollback-only
            throw e;
        }
    }

    /**
     * Cập nhật shop
     */
    @CacheEvict(
            value = {"etsyShops", "etsyShopByName"},
            allEntries = true
    )
    public EtsyShop update(Long id, EtsyShop etsyShop) {
        EtsyShop existingShop = etsyShopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + id));

        if (etsyShop.getName() != null && !etsyShop.getName().trim().isEmpty()) {
            String shopName = etsyShop.getName().trim();
            // Kiểm tra tên đã tồn tại (ngoài trừ chính nó)
            if (etsyShopRepository.existsByNameIgnoreCaseAndIdNot(shopName, id)) {
                throw new RuntimeException("Shop name already exists: " + shopName);
            }
            existingShop.setName(shopName);
        }

        if (etsyShop.getUrl() != null) {
            existingShop.setUrl(etsyShop.getUrl().trim());
        }

        if (etsyShop.getAvatarUrl() != null) {
            existingShop.setAvatarUrl(etsyShop.getAvatarUrl().trim());
        }

        if (etsyShop.getAccept() != null) {
            existingShop.setAccept(etsyShop.getAccept());
        }

        log.info("Updating shop with id: {}", id);
        return etsyShopRepository.save(existingShop);
    }

    /**
     * Xóa shop
     */
    @CacheEvict(
            value = {"etsyShops", "etsyShopByName"},
            allEntries = true
    )
    @Transactional
    public void delete(Long id) {
        EtsyShop etsyShop = etsyShopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + id));

        log.info("Deleting shop with id: {}", id);
        etsyShopRepository.delete(etsyShop);
    }

    /**
     * Lấy shop theo ID
     */
    public EtsyShop getById(Long id) {
        return etsyShopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + id));
    }

    /**
     * Đếm tổng số shop
     */
    public long countTotalShops() {
        return etsyShopRepository.count();
    }
}
