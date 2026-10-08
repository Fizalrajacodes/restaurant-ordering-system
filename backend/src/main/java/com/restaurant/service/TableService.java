package com.restaurant.service;

import com.restaurant.dtos.admin.TableRequest;
import com.restaurant.dtos.admin.TableResponse;
import com.restaurant.dtos.customer.TableDTO;
import com.restaurant.entity.RestaurantTable;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TableService {

    private final RestaurantTableRepository tableRepository;
    private final OrderRepository orderRepository;

    public List<TableDTO> getAllActiveTables() {
        return tableRepository.findByIsActiveTrueOrderByTableNumberAsc().stream()
                .map(this::mapToTableDTO)
                .collect(Collectors.toList());
    }

    public TableDTO getTableByToken(String token) {
        RestaurantTable table = tableRepository.findActiveByQrToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or inactive table QR code"));
        return mapToTableDTO(table);
    }

    public TableDTO getTableByNumber(String tableNumber) {
        RestaurantTable table = tableRepository.findByTableNumber(tableNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found: " + tableNumber));
        return mapToTableDTO(table);
    }

    public TableResponse getTableById(Long id) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));
        return mapToTableResponse(table);
    }

    public List<TableResponse> getAllTables() {
        return tableRepository.findAll().stream()
                .map(this::mapToTableResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TableResponse createTable(TableRequest request) {
        if (tableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new IllegalArgumentException("Table number already exists: " + request.getTableNumber());
        }

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(request.getTableNumber())
                .tableName(request.getTableName())
                .capacity(request.getCapacity())
                .status(request.getStatus() != null ? request.getStatus() : RestaurantTable.TableStatus.AVAILABLE)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        table = tableRepository.save(table);
        return mapToTableResponse(table);
    }

    @Transactional
    public TableResponse updateTable(Long id, TableRequest request) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));

        if (!table.getTableNumber().equals(request.getTableNumber()) 
                && tableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new IllegalArgumentException("Table number already exists: " + request.getTableNumber());
        }

        table.setTableNumber(request.getTableNumber());
        table.setTableName(request.getTableName());
        table.setCapacity(request.getCapacity());
        if (request.getStatus() != null) {
            table.setStatus(request.getStatus());
        }
        if (request.getIsActive() != null) {
            table.setIsActive(request.getIsActive());
        }

        table = tableRepository.save(table);
        return mapToTableResponse(table);
    }

    @Transactional
    public void deleteTable(Long id) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));
        tableRepository.delete(table);
    }

    public long getActiveOrdersCount(Long tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + tableId));
        
        return orderRepository.findActiveOrdersByTable(table, List.of(
                com.restaurant.entity.Order.OrderStatus.PENDING,
                com.restaurant.entity.Order.OrderStatus.CONFIRMED,
                com.restaurant.entity.Order.OrderStatus.PREPARING,
                com.restaurant.entity.Order.OrderStatus.READY
        )).size();
    }

    private TableDTO mapToTableDTO(RestaurantTable table) {
        return TableDTO.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .tableName(table.getTableName())
                .capacity(table.getCapacity())
                .status(table.getStatus().name())
                .build();
    }

    private TableResponse mapToTableResponse(RestaurantTable table) {
        long activeOrders = getActiveOrdersCount(table.getId());
        return TableResponse.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .tableName(table.getTableName())
                .capacity(table.getCapacity())
                .status(table.getStatus().name())
                .qrToken(table.getQrToken())
                .qrCodeUrl(table.getQrCodeUrl())
                .isActive(table.getIsActive())
                .createdAt(table.getCreatedAt())
                .updatedAt(table.getUpdatedAt())
                .activeOrdersCount(activeOrders)
                .build();
    }
}