package com.restaurant.dtos.customer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableDTO {

    private Long id;
    private String tableNumber;
    private String tableName;
    private Integer capacity;
    private String status;
}