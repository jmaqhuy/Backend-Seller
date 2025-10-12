package com.example.backendseller.service;

import com.example.backendseller.dto.AmazonAccountDTO;
import com.example.backendseller.entity.AmazonAccount;
import com.example.backendseller.repository.AmazonAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AmazonAccountService {
    private final AmazonAccountRepository amazonAccountRepository;

    // Lấy tất cả Account với phân trang
    @Cacheable(
            value = "accounts",
            key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<AmazonAccount> getAllAccounts(Pageable pageable) {
        log.info("Fetching all accounts with pageable: {}", pageable);
        return amazonAccountRepository.findAll(pageable).getContent();
    }

    // Lấy Account theo ID

    public AmazonAccount getAccountById(Integer id) {
        log.info("Fetching account with id: {}", id);
        return amazonAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
    }

    // Tìm kiếm Account theo tên
    public List<AmazonAccount> searchByName(String name, Pageable pageable) {
        log.info("Searching accounts by name: {} with pageable: {}", name, pageable);
        return amazonAccountRepository.findByNameContainingIgnoreCase(name, pageable).getContent();
    }


    // Tìm kiếm Account theo abbreviation
    public List<AmazonAccount> searchByAbbreviation(String abbreviation, Pageable pageable) {
        log.info("Searching accounts by abbreviation: {} with pageable: {}", abbreviation, pageable);
        return amazonAccountRepository.findByAbbreviationContainingIgnoreCase(abbreviation, pageable).getContent();
    }

    // Tạo mới Account
    @CacheEvict(value = {"accounts"}, allEntries = true)
    public AmazonAccountDTO createAccount(AmazonAccountDTO account) {
        log.info("Creating new account: {}", account);

        // Validate name
        if (account.getName() == null || account.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be empty");
        }

        // Check trùng lặp name
        Page<AmazonAccount> existsByName = amazonAccountRepository.findByNameContainingIgnoreCase(
                account.getName().trim(),
                org.springframework.data.domain.PageRequest.of(0, 1)
        );
        if (!existsByName.isEmpty()) {
            throw new IllegalArgumentException("Account name already exists: " + account.getName());
        }

        // Check trùng lặp abbreviation
        if (account.getAbbreviation() != null && !account.getAbbreviation().trim().isEmpty()) {
            Page<AmazonAccount> existsByAbbr = amazonAccountRepository.findByAbbreviationContainingIgnoreCase(
                    account.getAbbreviation().trim(),
                    org.springframework.data.domain.PageRequest.of(0, 1)
            );
            if (!existsByAbbr.isEmpty()) {
                throw new IllegalArgumentException("Account abbreviation already exists: " + account.getAbbreviation());
            }
        } else {
            throw new IllegalArgumentException("Account abbreviation cannot be empty");
        }

        // Check trùng lặp googleSheetId
        if (account.getGoogleSheetId() != null && !account.getGoogleSheetId().trim().isEmpty()) {
            Page<AmazonAccount> existsByGoogleSheet = amazonAccountRepository.findByGoogleSheetIdContainingIgnoreCase(
                    account.getGoogleSheetId(),
                    org.springframework.data.domain.PageRequest.of(0, 1)
            );
            if (!existsByGoogleSheet.isEmpty()) {
                throw new IllegalArgumentException("Google Sheet ID already exists: " + account.getGoogleSheetId());
            }
        } else {
            throw new IllegalArgumentException("Google Sheet ID cannot be empty");
        }
        AmazonAccount newAmazonAccount = new AmazonAccount();
        newAmazonAccount.setName(account.getName());
        newAmazonAccount.setAbbreviation(account.getAbbreviation());
        newAmazonAccount.setGoogleSheetId(account.getGoogleSheetId());

        return AmazonAccountDTO.fromEntity(amazonAccountRepository.save(newAmazonAccount));
    }

    // Cập nhật Account
    @CacheEvict(value = {"accounts"}, allEntries = true)
    @CachePut(value = "account", key = "#id")
    public AmazonAccount updateAccount(Integer id, AmazonAccount amazonAccountDetails) {
        log.info("Updating account with id: {}", id);
        AmazonAccount amazonAccount = amazonAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

        if (amazonAccountDetails.getName() != null && !amazonAccountDetails.getName().trim().isEmpty()) {
            amazonAccount.setName(amazonAccountDetails.getName());
        }
        if (amazonAccountDetails.getAbbreviation() != null) {
            amazonAccount.setAbbreviation(amazonAccountDetails.getAbbreviation());
        }
        if (amazonAccountDetails.getGoogleSheetId() != null) {
            amazonAccount.setGoogleSheetId(amazonAccountDetails.getGoogleSheetId());
        }

        AmazonAccount updatedAmazonAccount = amazonAccountRepository.save(amazonAccount);
        log.info("Account updated successfully with id: {}", id);
        return updatedAmazonAccount;
    }

    // Xóa Account
    @CacheEvict(value = {"accounts"}, allEntries = true)
    public void deleteAccount(Integer id) {
        log.info("Deleting account with id: {}", id);
        if (!amazonAccountRepository.existsById(id)) {
            throw new RuntimeException("Account not found with id: " + id);
        }
        amazonAccountRepository.deleteById(id);
        log.info("Account deleted successfully with id: {}", id);
    }
}
