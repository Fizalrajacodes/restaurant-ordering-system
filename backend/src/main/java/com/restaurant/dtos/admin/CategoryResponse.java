package com.restaurant.dtos.admin;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isActive;
    private Long menuItemsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
