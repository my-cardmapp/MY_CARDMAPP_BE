package com.thc.my_cardmapp.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

//lombok 의 Data -> Getter,Setter
@Data
@Builder
public class MerchantDetailDto {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private CategoryDto category;
    private List<CardDto> availableCards;
    private Double distance;
}