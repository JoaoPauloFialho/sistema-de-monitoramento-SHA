package br.com.monitoring.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MeterDiscoveryService {

    public static class DetectedMeter {
        private String label;
        private String path;
        private String suggestedId;

        public DetectedMeter(String label, String path, String suggestedId) {
            this.label = label;
            this.path = path;
            this.suggestedId = suggestedId;
        }

        public String getLabel() {
            return label;
        }

        public String getPath() {
            return path;
        }

        public String getSuggestedId() {
            return suggestedId;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public List<DetectedMeter> discoverMeters() {
        List<DetectedMeter> meters = new ArrayList<>();
        File rootDir = new File(".."); // Assuming we run from MonitoringSystem project root

        try {
            // 1. Scan Rodrigues - precisa procurar em Simulador-Hidrometro
            File rodBaseDir = new File(rootDir, "rodrigues-hidrometro");
            if (rodBaseDir.exists() && rodBaseDir.isDirectory()) {
                File rodSimDir = new File(rodBaseDir, "Simulador-Hidrometro");
                if (rodSimDir.exists() && rodSimDir.isDirectory()) {
                    findMedicaoFoldersRecursive(rodSimDir, meters, "ROD", 0);
                }
                // Também procura diretamente no rodrigues-hidrometro (caso tenha pastas lá)
                findMedicaoFoldersRecursive(rodBaseDir, meters, "ROD", 0);
            }

            // 2. Scan JoaoPaulo - já está no caminho correto
            File joaoDir = new File(rootDir, "joaopaulo-hidrometro/hidrometro");
            if (joaoDir.exists() && joaoDir.isDirectory()) {
                findMedicaoFoldersRecursive(joaoDir, meters, "JOAO", 0);
            }
        } catch (Exception e) {
            System.err.println("Error discovering meters: " + e.getMessage());
            e.printStackTrace();
        }

        return meters;
    }

    private void findMedicaoFoldersRecursive(File parent, List<DetectedMeter> result, String prefix, int depth) {
        // Limitar profundidade para evitar loops infinitos
        if (depth > 5) {
            return;
        }

        File[] files = parent.listFiles();
        if (files == null) {
            return;
        }

        for (File f : files) {
            if (f.isDirectory()) {
                String dirName = f.getName().toLowerCase();
                // Procura por pastas que começam com "medi" (case insensitive)
                if (dirName.startsWith("medi")) {
                    try {
                        String absPath = f.getCanonicalPath();
                        String name = f.getName();
                        
                        // Verifica se a pasta tem imagens (jpeg/jpg)
                        File[] images = f.listFiles((dir, fileName) -> 
                            fileName.toLowerCase().endsWith(".jpeg") || 
                            fileName.toLowerCase().endsWith(".jpg")
                        );
                        
                        // Só adiciona se tiver imagens
                        if (images != null && images.length > 0) {
                            // Gera ID baseado no nome da pasta para ser mais consistente
                            String idSuffix = name.replaceAll("[^0-9]", "");
                            if (idSuffix.isEmpty()) {
                                idSuffix = String.valueOf(Math.abs(name.hashCode()) % 10000);
                            }
                            String id = "SHA-" + prefix + "-" + idSuffix;

                            result.add(new DetectedMeter(
                                    prefix + " Simulator: " + name,
                                    absPath,
                                    id));
                        }
                    } catch (IOException e) {
                        System.err.println("Error processing folder: " + f.getAbsolutePath());
                        e.printStackTrace();
                    }
                } else {
                    // Continua procurando recursivamente em subpastas
                    findMedicaoFoldersRecursive(f, result, prefix, depth + 1);
                }
            }
        }
    }
}
