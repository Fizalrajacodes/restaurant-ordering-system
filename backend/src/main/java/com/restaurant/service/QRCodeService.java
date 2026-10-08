package com.restaurant.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.restaurant.dtos.qr.GenerateQRRequest;
import com.restaurant.dtos.qr.QRCodePrintResponse;
import com.restaurant.dtos.qr.QRCodeResponse;
import com.restaurant.entity.QRCode;
import com.restaurant.entity.RestaurantTable;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.repository.QRCodeRepository;
import com.restaurant.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QRCodeService {

    private final QRCodeRepository qrCodeRepository;
    private final RestaurantTableRepository tableRepository;

    @Value("${qr.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${qr.size:300}")
    private int qrSize;

    @Value("${qr.storage-path:./qr-codes}")
    private String qrStoragePath;

    @Transactional
    public QRCodeResponse generateQRCode(GenerateQRRequest request) {
        RestaurantTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + request.getTableId()));

        String token = generateUniqueToken();
        String qrData = buildQrUrl(request.getBaseUrl() != null ? request.getBaseUrl() : baseUrl, token);

        QRCode qrCode = QRCode.builder()
                .table(table)
                .token(token)
                .qrData(qrData)
                .isActive(true)
                .build();

        try {
            byte[] imageData = generateQRCodeImage(qrData);
            qrCode.setImageData(imageData);
            
            // Save QR code image as file
            String filePath = saveQRCodeImageToFile(table.getTableNumber(), token, imageData);
            log.info("QR code image saved to: {}", filePath);
        } catch (Exception e) {
            log.error("Failed to generate QR code image for table: {}", table.getTableNumber(), e);
        }

        qrCode = qrCodeRepository.save(qrCode);
        table.setQrToken(token);
        table.setQrCodeUrl(qrCode.getImageUrl());
        tableRepository.save(table);

        return mapToResponse(qrCode);
    }

    @Transactional
    public QRCodeResponse regenerateQRCode(Long tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + tableId));

        qrCodeRepository.findByTable(table).ifPresent(existing -> {
            existing.setIsActive(false);
            qrCodeRepository.save(existing);
        });

        return generateQRCode(GenerateQRRequest.builder().tableId(tableId).build());
    }

    private String saveQRCodeImageToFile(String tableNumber, String token, byte[] imageData) {
        try {
            // Create storage directory
            Path storageDir = Paths.get(qrStoragePath);
            Files.createDirectories(storageDir);

            // Generate filename: TABLE-01_TBL-ABC12345.png
            String filename = String.format("%s_%s.png", tableNumber.replace("/", "_"), token);
            Path filePath = storageDir.resolve(filename);

            // Write image to file
            Files.write(filePath, imageData);
            
            // Also save a symlink/copy with just table number for easy access
            Path simplePath = storageDir.resolve(tableNumber.replace("/", "_") + ".png");
            Files.copy(filePath, simplePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            return filePath.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Failed to save QR code image to file for table: {}", tableNumber, e);
            return null;
        }
    }

    public QRCodeResponse getQRCodeByTableId(Long tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + tableId));

        return qrCodeRepository.findByTableAndIsActiveTrue(table)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Active QR code not found for table: " + table.getTableNumber()));
    }

    public QRCodeResponse getQRCodeByToken(String token) {
        return qrCodeRepository.findByToken(token)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("QR code not found for token: " + token));
    }

    public List<QRCodeResponse> getAllQRCodes() {
        return qrCodeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void recordScan(String token) {
        qrCodeRepository.findByToken(token).ifPresent(qrCode -> {
            qrCode.setScanCount(qrCode.getScanCount() + 1);
            qrCode.setLastScannedAt(java.time.LocalDateTime.now());
            qrCodeRepository.save(qrCode);
        });
    }

    public QRCodePrintResponse getPrintableQRCode(Long tableId) {
        QRCodeResponse qrCode = getQRCodeByTableId(tableId);
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

        return QRCodePrintResponse.builder()
                .tableNumber(table.getTableNumber())
                .tableName(table.getTableName())
                .qrCodeImageBase64(qrCode.getImageUrl() != null ? qrCode.getImageUrl().replace("data:image/png;base64,", "") : null)
                .qrDataUrl(qrCode.getQrData())
                .build();
    }

    private String generateUniqueToken() {
        String token;
        do {
            token = "TBL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (qrCodeRepository.existsByToken(token));
        return token;
    }

    private String buildQrUrl(String baseUrl, String token) {
        return String.format("%s/menu?table=%s", baseUrl.replaceAll("/$", ""), token);
    }

    private byte[] generateQRCodeImage(String data) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, qrSize, qrSize);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    private QRCodeResponse mapToResponse(QRCode qrCode) {
        return QRCodeResponse.builder()
                .id(qrCode.getId())
                .tableId(qrCode.getTable().getId())
                .tableNumber(qrCode.getTable().getTableNumber())
                .tableName(qrCode.getTable().getTableName())
                .token(qrCode.getToken())
                .qrData(qrCode.getQrData())
                .imageUrl(toImageDataUrl(qrCode))
                .isActive(qrCode.getIsActive())
                .scanCount(qrCode.getScanCount())
                .lastScannedAt(qrCode.getLastScannedAt())
                .createdAt(qrCode.getCreatedAt())
                .build();
    }

    private String toImageDataUrl(QRCode qrCode) {
        if (qrCode.getImageData() == null) {
            return null;
        }
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrCode.getImageData());
    }
}