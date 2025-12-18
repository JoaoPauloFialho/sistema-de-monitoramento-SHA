package br.com.monitoring.ocr;

import java.io.File;

/**
 * Adapter for Tess4J library.
 * note: Requires tess4j jar in classpath.
 */
public class Tess4JAdapter implements OCRAdapter {

    @Override
    public double readConsumption(File imageFile) throws Exception {
        // Implementation using reflection to avoid compile errors if library is missing
        // In a real setup with jars, you would import net.sourceforge.tess4j.*;

        System.out.println("Attempting to use Tess4J for: " + imageFile.getName());

        // Pseudo-code for Tess4J usage:
        // ITesseract instance = new Tesseract();
        // String result = instance.doOCR(imageFile);
        // return parse(result);

        throw new UnsupportedOperationException(
                "Tess4J library not found in classpath. Please use CLIAdapter or add dependencies.");
    }
}
