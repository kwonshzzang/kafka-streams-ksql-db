package kr.co.kwonshzzang.patientmonitoring;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PatientMonitoringTopology {
    private static final Logger logger = LoggerFactory.getLogger(PatientMonitoringTopology.class);

    public static Topology build() {
        // the builder is used to construct the topology
        StreamsBuilder builder = new StreamsBuilder();

        return builder.build();
    }
}
