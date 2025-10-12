package com.example.backendseller.controller;

import com.example.backendseller.dto.AmazonAccountDTO;
import com.example.backendseller.dto.CustomResponse;
import com.example.backendseller.entity.AmazonAccount;
import com.example.backendseller.service.AmazonAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AmazonAccountController {
    private final AmazonAccountService amazonAccountService;

    @GetMapping("/amazon_accounts")
    public Page<AmazonAccount> getAccounts() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        List<AmazonAccount> list = amazonAccountService.getAllAccounts(pageable);
        return new PageImpl<>(list, pageable, list.size());
    }

    @PostMapping("/amazon_accounts")
    public ResponseEntity<CustomResponse> createAccount(@RequestBody AmazonAccountDTO account) {
        try {
            AmazonAccountDTO amazonAccountDTO = amazonAccountService.createAccount(account);
            log.info("Account Created Successfully");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(CustomResponse.builder()
                            .data(amazonAccountDTO)
                            .message("Create account " + amazonAccountDTO.getName() + " successfully!")
                            .build());
        } catch (Exception e) {
            log.error("Account Creation Failed With Message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    CustomResponse.builder()
                            .data(null)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

}