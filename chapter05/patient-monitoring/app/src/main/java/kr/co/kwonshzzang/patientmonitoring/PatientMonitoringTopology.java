package kr.co.kwonshzzang.patientmonitoring;

import kr.co.kwonshzzang.patientmonitoring.model.BodyTemp;
import kr.co.kwonshzzang.patientmonitoring.model.Pulse;
import kr.co.kwonshzzang.patientmonitoring.serialization.json.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class PatientMonitoringTopology {
    private static final Logger log = LoggerFactory.getLogger(PatientMonitoringTopology.class);

    public static Topology build() {
        // the builder is used to construct the topology
        StreamsBuilder builder = new StreamsBuilder();

        // the following topology steps are numbered. these numbers correlate with
        // the topology design shown in the book (Chapter 5: Windows and Time)

        // 1.1
        Consumed<String, Pulse> pulseConsumerOptions =
                Consumed.with(Serdes.String(), JsonSerdes.Pulse())
                        .withTimestampExtractor(new VitalTimestampExtractor());

        KStream<String, Pulse> pulseEvents =
                // register the pulse-events stream
                builder.stream("pulse-events", pulseConsumerOptions);

        // 1.2
        Consumed<String, BodyTemp> bodyTempConsumerOptions =
                Consumed.with(Serdes.String(), JsonSerdes.BodyTemp())
                         .withTimestampExtractor(new VitalTimestampExtractor());

        KStream<String, BodyTemp> tempEvents =
                builder.stream("body-temp-events", bodyTempConsumerOptions);

        // turn pulse into a rate (bpm)
        TimeWindows tumblingWindow =
                TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(60));

        return builder.build();
    }
}
