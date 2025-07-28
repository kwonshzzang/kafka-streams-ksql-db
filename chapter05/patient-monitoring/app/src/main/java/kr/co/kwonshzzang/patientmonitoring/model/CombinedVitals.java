package kr.co.kwonshzzang.patientmonitoring.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class CombinedVitals {
    private final int heartRate;
    private final BodyTemp bodyTemp;
}
