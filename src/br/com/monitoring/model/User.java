package br.com.monitoring.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
    private String cpf;
    private String address;
    private double consumptionLimit;
    private List<Meter> meters;

    public User(String name, String cpf, String address, double consumptionLimit) {
        this.name = name;
        this.cpf = cpf;
        this.address = address;
        this.consumptionLimit = consumptionLimit;
        this.meters = new ArrayList<>();
    }

    public String getName() { return name; }
    public String getCpf() { return cpf; }
    public String getAddress() { return address; }
    public double getConsumptionLimit() { return consumptionLimit; }
    public List<Meter> getMeters() { return meters; }

    public void addMeter(Meter meter) {
        this.meters.add(meter);
    }
    
    @Override
    public String toString() {
        return "User{name='" + name + "', cpf='" + cpf + "', limit=" + consumptionLimit + "}";
    }
}
