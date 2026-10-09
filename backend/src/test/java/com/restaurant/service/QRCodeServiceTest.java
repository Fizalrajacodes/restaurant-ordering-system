package com.restaurant.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QRCodeServiceTest {

    private QRCodeService service() {
        return new QRCodeService(null, null);
    }

    @Test
    void generateQrImage_returnsValidPng() {
        byte[] image = service().generateQrImage("https://example.com/menu?table=TBL-01", 300, "M");

        assertThat(image).isNotNull().isNotEmpty();
        // PNG magic bytes
        assertThat(image[0]).isEqualTo((byte) 0x89);
        assertThat(image[1]).isEqualTo((byte) 0x50);
        assertThat(image[2]).isEqualTo((byte) 0x4E);
        assertThat(image[3]).isEqualTo((byte) 0x47);
    }

    @Test
    void generateQrImage_usesDefaultSizeAndErrorCorrection() {
        byte[] image = service().generateQrImage("hello world");

        assertThat(image).isNotNull().isNotEmpty();
        assertThat(image[0]).isEqualTo((byte) 0x89);
    }

    @Test
    void generateQrImage_acceptsPlainText() {
        byte[] image = service().generateQrImage("Plain text content 123");

        assertThat(image).isNotNull().isNotEmpty();
        assertThat(image[0]).isEqualTo((byte) 0x89);
        assertThat(image[3]).isEqualTo((byte) 0x47);
    }

    @Test
    void generateQrImage_throwsOnNullData() {
        assertThatThrownBy(() -> service().generateQrImage(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void generateQrImage_supportsErrorCorrectionLevels() {
        for (String level : new String[]{"L", "M", "Q", "H"}) {
            byte[] image = service().generateQrImage("ec=" + level, 200, level);
            assertThat(image).isNotNull().isNotEmpty();
        }
    }
}
