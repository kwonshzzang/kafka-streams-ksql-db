package kr.co.kwonshzzang.patientmonitoring.model;

public class Pulse implements  Vital {
    private String timestamp;

    @Override
    public String getTimestamp() {
        return  timestamp;
    }
}
