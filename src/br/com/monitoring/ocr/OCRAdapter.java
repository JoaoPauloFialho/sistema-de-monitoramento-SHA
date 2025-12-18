package br.com.monitoring.ocr;

import java.io.File;

public interface OCRAdapter {
    /**
     * Reads numerical value from a meter image.
     * 
     * @param imageFile The image file to process.
     * @return The read value as a double.
     * @throws Exception If reading fails.
     */
    double readConsumption(File imageFile) throws Exception;
}
