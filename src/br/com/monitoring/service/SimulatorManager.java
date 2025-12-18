package br.com.monitoring.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimulatorManager {
    private List<Process> simulatorProcesses;
    private boolean isRunning;

    public SimulatorManager() {
        this.simulatorProcesses = new ArrayList<>();
        this.isRunning = false;
    }

    public void startSimulators() throws IOException {
        if (isRunning)
            return;

        // Assuming we are running from MonitoringSystem/
        // Root is one level up
        File rootDir = new File("..").getCanonicalFile();

        // Rodrigues Simulator
        startSimulatorProcess(rootDir, "rodrigues-hidrometro", "Simulador-Hidrometro",
                "main.java.br.com.simulador.Main");

        // JoaoPaulo Simulator
        startSimulatorProcess(rootDir, "joaopaulo-hidrometro/hidrometro", ".", "main.java.br.hidrometro.Main");

        isRunning = true;
    }

    private void startSimulatorProcess(File root, String projectDirIdx, String binRelPath, String mainClass)
            throws IOException {
        File projectDir = new File(root, projectDirIdx);
        // The simulators seem to want to run from their specific directories based on
        // CLI run_simulators.sh
        // Rodrigues: cd rodrigues-hidrometro/Simulador-Hidrometro/.. -> so it runs from
        // rodrigues-hidrometro?
        // Wait, the bash script says:
        // cd rodrigues-hidrometro/Simulador-Hidrometro
        // cd ..
        // java -cp Simulador-Hidrometro/bin ...
        // So effectively it runs in 'rodrigues-hidrometro' folder.

        // JoaoPaulo:
        // cd joaopaulo-hidrometro/hidrometro
        // java -cp bin ...
        // So it runs in 'joaopaulo-hidrometro/hidrometro' folder.

        File workingDir;
        String cp;

        if (projectDirIdx.contains("rodrigues")) {
            workingDir = new File(root, "rodrigues-hidrometro");
            cp = "Simulador-Hidrometro/bin";
        } else {
            workingDir = new File(root, "joaopaulo-hidrometro/hidrometro");
            cp = "bin";
        }

        if (!workingDir.exists()) {
            throw new IOException("Simulator directory not found: " + workingDir.getAbsolutePath());
        }

        ProcessBuilder pb = new ProcessBuilder("java", "-cp", cp, mainClass);
        pb.directory(workingDir);
        // Inherit IO so we can see output in console if launched from there, or file
        // redirect could be better
        // pb.inheritIO();

        Process p = pb.start();
        simulatorProcesses.add(p);
        System.out.println("Started simulator: " + mainClass + " in " + workingDir);
    }

    public void stopSimulators() {
        for (Process p : simulatorProcesses) {
            if (p.isAlive()) {
                p.destroy();
            }
        }
        simulatorProcesses.clear();
        isRunning = false;
        System.out.println("Stopped all simulators.");
    }

    public boolean isRunning() {
        // Simple check, Process.isAlive() check might be better
        boolean anyAlive = false;
        for (Process p : simulatorProcesses) {
            if (p.isAlive())
                anyAlive = true;
        }
        return isRunning && anyAlive;
    }
}
