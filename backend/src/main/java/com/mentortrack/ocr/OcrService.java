package com.mentortrack.ocr;

import com.mentortrack.config.OcrProperties;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Thin wrapper around Tess4j (Tesseract OCR) for 10th/12th/Diploma and semester marksheet uploads,
 * per the spec. Deliberately NOT used for Aadhaar/PAN uploads.
 *
 * If Tesseract's native library / trained data is not installed on the host, this raises a clear
 * {@link OcrUnavailableException} rather than crashing the whole request.
 *
 * IMPORTANT: when Tesseract cannot find its "eng.traineddata" file (wrong/missing tessdata path,
 * or Tesseract not installed at all), its native bridge does NOT throw a normal Java exception —
 * it throws a bare {@code java.lang.Error("Invalid memory access")} straight out of the JNA layer.
 * A plain {@code catch (Exception e)} does not catch that, since {@code Error} is not an
 * {@code Exception}; it will propagate all the way out of the controller and surface to the client
 * as a generic 500 "Handler dispatch failed: java.lang.Error: Invalid memory access", which is
 * confusing and gives no indication of the real problem. This class catches {@link Throwable}
 * around the native call specifically to convert that into the same clear, actionable
 * {@link OcrUnavailableException} as every other OCR-unavailable case, and does a tessdata-path
 * preflight check first so the common case (path misconfigured) gets an even more specific message
 * before ever touching the native library.
 */
@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    private final OcrProperties ocrProperties;

    public OcrService(OcrProperties ocrProperties) {
        this.ocrProperties = ocrProperties;
    }

    public String extractText(InputStream imageStream) {
        checkTessdataAvailable();

        BufferedImage image;
        try {
            image = ImageIO.read(imageStream);
        } catch (IOException e) {
            throw new OcrUnavailableException("Could not read uploaded image: " + e.getMessage());
        }
        if (image == null) {
            throw new OcrUnavailableException("Uploaded file is not a readable image. "
                    + "Please upload a JPG/PNG scan of the marksheet (not a PDF).");
        }

        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(ocrProperties.getTessdataPath());
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            throw new OcrUnavailableException("OCR engine is not available on this server "
                    + "(Tesseract not installed or tessdata path misconfigured): " + e.getMessage());
        } catch (Throwable t) {
            // Tess4j's native bridge can throw a bare java.lang.Error("Invalid memory access")
            // (not a normal Exception) when it fails to load its language data — most commonly
            // because MENTORTRACK_TESSDATA_PATH / mentortrack.ocr.tessdata-path points at a
            // directory that doesn't contain "eng.traineddata", or Tesseract isn't installed at
            // all. Catching Throwable here is intentional: without it, this Error propagates past
            // every layer of the app and surfaces to the client as a generic, confusing 500.
            log.warn("Tesseract OCR failed with a native/unexpected error (tessdata path: {})",
                    ocrProperties.getTessdataPath(), t);
            throw new OcrUnavailableException("OCR engine failed to run on this server. This usually means "
                    + "Tesseract's language data could not be loaded from the configured tessdata path ("
                    + ocrProperties.getTessdataPath() + "). Verify Tesseract is installed and "
                    + "MENTORTRACK_TESSDATA_PATH points at a directory containing eng.traineddata, "
                    + "then try again.");
        }
    }

    private void checkTessdataAvailable() {
        String tessdataPath = ocrProperties.getTessdataPath();
        if (tessdataPath == null || tessdataPath.isBlank()) {
            throw new OcrUnavailableException("OCR is not configured on this server "
                    + "(mentortrack.ocr.tessdata-path / MENTORTRACK_TESSDATA_PATH is not set).");
        }
        Path dir = Path.of(tessdataPath);
        if (!Files.isDirectory(dir)) {
            throw new OcrUnavailableException("OCR is not available on this server: configured tessdata "
                    + "directory does not exist (" + tessdataPath + "). Install Tesseract and/or set "
                    + "MENTORTRACK_TESSDATA_PATH to a valid tessdata directory.");
        }
        if (!Files.isRegularFile(dir.resolve("eng.traineddata"))) {
            throw new OcrUnavailableException("OCR is not available on this server: tessdata directory ("
                    + tessdataPath + ") does not contain eng.traineddata. Install the Tesseract English "
                    + "language pack and/or fix MENTORTRACK_TESSDATA_PATH.");
        }
    }
}
