package br.com.monitoring.ocr;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TesseractCLIAdapter implements OCRAdapter {

    @Override
    public double readConsumption(File imageFile) throws Exception {
        // Execute tesseract command line
        ProcessBuilder pb = new ProcessBuilder("tesseract", imageFile.getAbsolutePath(), "stdout", "--psm", "6");
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        // Capture stderr as well for debugging
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder errorOutput = new StringBuilder();
        while ((line = errorReader.readLine()) != null) {
            errorOutput.append(line).append("\n");
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException(
                    "Tesseract CLI failed with code " + exitCode + ". Error: " + errorOutput.toString());
        }

        String ocrText = output.toString().trim();
        System.out.println("[DEBUG] OCR Raw Output: " + ocrText);

        return cleanAndParse(ocrText);
    }

    private double cleanAndParse(String text) {
        // Try to extract a number from the text
        // JoaoPaulo format: "010156" means 101.56 m³ (first 4 digits = integer, last 2 = decimal)
        // Rodrigues format: just digits representing m³ directly
        
        // Remove all non-digit characters first
        String digitsOnly = text.replaceAll("[^0-9]", "");
        
        if (digitsOnly.length() >= 6) {
            // Likely JoaoPaulo format: 6 digits = 4 integer + 2 decimal
            // Example: "010156" = 101.56 m³
            try {
                String integerPart = digitsOnly.substring(0, digitsOnly.length() - 2);
                String decimalPart = digitsOnly.substring(digitsOnly.length() - 2);
                String valueStr = integerPart + "." + decimalPart;
                return Double.parseDouble(valueStr);
            } catch (Exception e) {
                // Fall through to standard parsing
            }
        }
        
        // Standard pattern matching for other formats
        Pattern p = Pattern.compile("(\\d+[.,]?\\d*)");
        Matcher m = p.matcher(text);
        if (m.find()) {
            String numStr = m.group(1).replace(",", ".");
            try {
                double value = Double.parseDouble(numStr);
                // If value is very large (> 10000), might be in wrong format
                // Try interpreting as JoaoPaulo format (divide by 100)
                if (value > 10000 && digitsOnly.length() >= 6) {
                    return value / 100.0;
                }
                return value;
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0; // Fail safe
    }
}
