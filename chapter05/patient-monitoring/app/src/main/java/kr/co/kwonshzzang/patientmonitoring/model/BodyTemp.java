package kr.co.kwonshzzang.patientmonitoring.model;

public class BodyTemp implements Vital {
    private String timestamp;
    private Double temperature;
    private String unit;

    @Override
    public String getTimestamp() {
        return timestamp;
    }

    public Double getTemperature() {
        return temperature;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return "BodyTemp{" +
                "timestamp='" + timestamp + '\'' +
                ", temperature=" + temperature +
                ", unit='" + unit + '\'' +
                '}';
    }
}
