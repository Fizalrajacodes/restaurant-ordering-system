package com.restaurant.controller.admin;

import com.restaurant.dtos.ApiResponse;
import com.restaurant.dtos.admin.TableRequest;
import com.restaurant.dtos.admin.TableResponse;
import com.restaurant.dtos.qr.GenerateQRRequest;
import com.restaurant.dtos.qr.QRCodePrintResponse;
import com.restaurant.dtos.qr.QRCodeResponse;
import com.restaurant.service.QRCodeService;
import com.restaurant.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;
    private final QRCodeService qrCodeService;

    @GetMapping
    public ApiResponse<List<TableResponse>> getAllTables() {
        return ApiResponse.success(tableService.getAllTables());
    }

    @GetMapping("/{id}")
    public ApiResponse<TableResponse> getTable(@PathVariable Long id) {
        return ApiResponse.success(tableService.getTableById(id));
    }

    @PostMapping
    public ApiResponse<TableResponse> createTable(@Valid @RequestBody TableRequest request) {
        return ApiResponse.success("Table created successfully", tableService.createTable(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TableResponse> updateTable(@PathVariable Long id, @Valid @RequestBody TableRequest request) {
        return ApiResponse.success("Table updated successfully", tableService.updateTable(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ApiResponse.successMessage("Table deleted successfully");
    }

    @PostMapping("/{id}/qr")
    public ApiResponse<QRCodeResponse> generateQRCode(@PathVariable Long id) {
        return ApiResponse.success("QR code generated successfully", 
                qrCodeService.generateQRCode(GenerateQRRequest.builder().tableId(id).build()));
    }

    @PostMapping("/{id}/qr/regenerate")
    public ApiResponse<QRCodeResponse> regenerateQRCode(@PathVariable Long id) {
        return ApiResponse.success("QR code regenerated successfully", qrCodeService.regenerateQRCode(id));
    }

    @GetMapping("/{id}/qr")
    public ApiResponse<QRCodeResponse> getQRCode(@PathVariable Long id) {
        return ApiResponse.success(qrCodeService.getQRCodeByTableId(id));
    }

    @GetMapping("/{id}/qr/print")
    public ApiResponse<QRCodePrintResponse> getPrintableQRCode(@PathVariable Long id) {
        return ApiResponse.success(qrCodeService.getPrintableQRCode(id));
    }

    @GetMapping("/qr/all")
    public ApiResponse<List<QRCodeResponse>> getAllQRCodes() {
        return ApiResponse.success(qrCodeService.getAllQRCodes());
    }
}