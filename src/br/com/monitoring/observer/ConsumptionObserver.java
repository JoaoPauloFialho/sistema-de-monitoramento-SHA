package br.com.monitoring.observer;

import br.com.monitoring.model.User;
import br.com.monitoring.model.ConsumptionRecord;

public interface ConsumptionObserver {
    void onNewReading(User user, ConsumptionRecord record);
}
