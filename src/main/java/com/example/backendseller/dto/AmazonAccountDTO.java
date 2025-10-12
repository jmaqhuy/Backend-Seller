package com.example.backendseller.dto;

import com.example.backendseller.entity.AmazonAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmazonAccountDTO {
    private String name;
    private String abbreviation;
    private String googleSheetId;

    public static AmazonAccountDTO fromEntity(AmazonAccount amazonAccount) {
        return AmazonAccountDTO.builder()
                .name(amazonAccount.getName())
                .abbreviation(amazonAccount.getAbbreviation())
                .googleSheetId(amazonAccount.getGoogleSheetId())
                .build();
    }
}
