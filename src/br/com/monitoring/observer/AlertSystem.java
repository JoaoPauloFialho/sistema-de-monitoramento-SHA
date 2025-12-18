package br.com.monitoring.observer;

import br.com.monitoring.model.User;
import br.com.monitoring.model.ConsumptionRecord;
import br.com.monitoring.log.AuditLogger;
import javax.swing.JOptionPane;

public class AlertSystem implements ConsumptionObserver {
    private boolean emailEnabled = true;
    private java.util.List<javax.swing.JFrame> parentFrames = new java.util.ArrayList<>();

    public void setEmailEnabled(boolean enabled) {
        this.emailEnabled = enabled;
        System.out.println(">>> [SYSTEM] Email Notifications set to: " + enabled);
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void addParentFrame(javax.swing.JFrame frame) {
        parentFrames.add(frame);
    }

    @Override
    public void onNewReading(User user, ConsumptionRecord record) {
        if (record.getValue() > user.getConsumptionLimit()) {
            String alertMsg = String.format(
                "⚠️ ALERTA DE CONSUMO EXCEDIDO ⚠️\n\n" +
                "Usuário: %s\n" +
                "CPF: %s\n" +
                "Consumo Atual: %.2f m³\n" +
                "Limite: %.2f m³\n" +
                "Excesso: %.2f m³\n\n" +
                "Este é um alerta simulado de notificação por e-mail.",
                user.getName(),
                user.getCpf(),
                record.getValue(),
                user.getConsumptionLimit(),
                record.getValue() - user.getConsumptionLimit()
            );
            
            System.out.println(">>> [SYSTEM] " + alertMsg.replace("\n", " | "));
            AuditLogger.getInstance().log("ALERT: User " + user.getName() + " exceeded limit! Current: " 
                + String.format("%.2f", record.getValue()) + " m³, Limit: " 
                + String.format("%.2f", user.getConsumptionLimit()) + " m³");

            if (emailEnabled) {
                showEmailAlert(user, alertMsg);
            } else {
                System.out.println(">>> [EMAIL] Email suppressed (Disabled by user).");
            }
        }
    }

    private void showEmailAlert(User user, String messageBody) {
        // Simula envio de email mostrando um alerta GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            String title = "📧 Simulação de E-mail - Alerta de Consumo";
            JOptionPane.showMessageDialog(
                getParentFrame(),
                messageBody,
                title,
                JOptionPane.WARNING_MESSAGE
            );
            System.out.println(">>> [EMAIL SIMULADO] E-mail enviado para: " + user.getName() + " (CPF: " + user.getCpf() + ")");
        });
    }

    private javax.swing.JFrame getParentFrame() {
        if (!parentFrames.isEmpty()) {
            return parentFrames.get(0);
        }
        return null;
    }
}
