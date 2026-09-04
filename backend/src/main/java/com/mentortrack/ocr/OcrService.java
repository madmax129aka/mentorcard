package com.mentortrack.ocr;

import com.mentortrack.config.OcrProperties;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Thin wrapper around Tess4j (Tesseract OCR) for 10th/12th/Diploma and semester marksheet uploads,
 * per the spec. Deliberately NOT used for Aadhaar/PAN uploads.
 *
 * If Tesseract's native library / trained data is not installed on the host, this raises a clear
 * {@link OcrUnavailableException} rather than crashing the whole request, so the rest of the app
 * (and the demo) keeps working even in environments without Tesseract installed.
 */
@Service
public class OcrService {

    private final OcrProperties ocrProperties;

    public OcrService(OcrProperties ocrProperties) {
        this.ocrProperties = ocrProperties;
    }

    public String extractText(InputStream imageStream) {
        try {
            BufferedImage image = ImageIO.read(imageStream);
            if (image == null) {
                throw new OcrUnavailableException("Uploaded file is not a readable image");
            }
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(ocrProperties.getTessdataPath());
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            throw new OcrUnavailableException("OCR engine is not available on this server "
                    + "(Tesseract not installed or tessdata path misconfigured): " + e.getMessage());
        } catch (IOException e) {
            throw new OcrUnavailableException("Could not read uploaded image: " + e.getMessage());
        } catch (UnsatisfiedLinkError | ExceptionInInitializerError e) {
            throw new OcrUnavailableException("Tesseract native library is not installed on this server");
        }
    }
}
