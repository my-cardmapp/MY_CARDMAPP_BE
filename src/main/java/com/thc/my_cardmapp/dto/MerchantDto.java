package com.thc.my_cardmapp.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MerchantDto {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String businessHours;
    private CategoryDto category;
    private List<CardDto> availableCards;
    private Double distance;
}