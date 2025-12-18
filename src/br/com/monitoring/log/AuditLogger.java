package br.com.monitoring.log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class AuditLogger {
    private static AuditLogger instance;
    private static final String LOG_FILE = "audit_log.txt";

    private AuditLogger() {
    }

    public static synchronized AuditLogger getInstance() {
        if (instance == null) {
            instance = new AuditLogger();
        }
        return instance;
    }

    public synchronized void log(String operation) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
                PrintWriter pw = new PrintWriter(fw)) {
            pw.println(LocalDateTime.now() + " - " + operation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
