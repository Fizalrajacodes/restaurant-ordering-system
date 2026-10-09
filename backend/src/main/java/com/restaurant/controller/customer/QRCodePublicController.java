package com.restaurant.controller.customer;

import com.restaurant.service.QRCodeService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
@Validated
public class QRCodePublicController {

    private final QRCodeService qrCodeService;

    @GetMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrImage(
            @RequestParam String data,
            @RequestParam(defaultValue = "300") @Min(50) @Max(2000) int size,
            @RequestParam(defaultValue = "L") String errorCorrection,
            @RequestParam(defaultValue = "qr-code") String filename) {

        if (data == null || data.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        byte[] image = qrCodeService.generateQrImage(data.trim(), size, errorCorrection);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(image.length);
        headers.setContentDispositionFormData(filename, filename + ".png");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(image);
    }

    @GetMapping(value = "/data-url", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generateQrDataUrl(
            @RequestParam String data,
            @RequestParam(defaultValue = "300") @Min(50) @Max(2000) int size,
            @RequestParam(defaultValue = "L") String errorCorrection) {

        if (data == null || data.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        byte[] image = qrCodeService.generateQrImage(data.trim(), size, errorCorrection);
        String dataUrl = "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(image);
        return ResponseEntity.ok(dataUrl);
    }
}
