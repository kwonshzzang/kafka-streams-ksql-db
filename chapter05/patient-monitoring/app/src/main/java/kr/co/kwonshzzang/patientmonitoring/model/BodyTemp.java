package kr.co.kwonshzzang.patientmonitoring.model;

import lombok.Getter;

@Getter
public class BodyTemp implements Vital {
    private String timestamp;
    private Double temperature;
    private String unit;
}
