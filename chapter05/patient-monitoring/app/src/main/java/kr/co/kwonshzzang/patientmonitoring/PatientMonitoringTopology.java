package kr.co.kwonshzzang.patientmonitoring;

import kr.co.kwonshzzang.patientmonitoring.model.BodyTemp;
import kr.co.kwonshzzang.patientmonitoring.model.Pulse;
import kr.co.kwonshzzang.patientmonitoring.serialization.json.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;


public class PatientMonitoringTopology {
    private static final Logger logger = LoggerFactory.getLogger(PatientMonitoringTopology.class);

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

        pulseEvents.print(Printed.toSysOut());

        // 1.2
        Consumed<String, BodyTemp> bodyTempConsumerOptions =
                Consumed.with(Serdes.String(), JsonSerdes.BodyTemp())
                        .withTimestampExtractor(new VitalTimestampExtractor());

        KStream<String, BodyTemp> tempEvents =
                // register the body-temp-events stream
                builder.stream("body-temp-events", bodyTempConsumerOptions);

        tempEvents.print(Printed.toSysOut());

        // turn pulse into a rate (bpm)
        TimeWindows tumblingWindows = TimeWindows.ofSizeAndGrace(Duration.ofSeconds(60), Duration.ofSeconds(5));

        /**
         * Examples of other windows(not needed for the tutorial) are commented out below
         *
         *  TimeWindows hoppingWindows = TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(5)).advanceBy(Duration.ofSeconds(4));
         *  SessionWindows sessionWindows = SessionWindows.ofInactivityGapWithNoGrace(Duration.ofSeconds(10));
         *  JoinWindows joinWindows = JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(5));
         *  SlidingWindows slidingWindows = SlidingWindows.ofTimeDifferenceAndGrace(Duration.ofSeconds(5), Duration.ofSeconds(0));
         */

        KTable<Windowed<String>, Long> pulseCounts =
                pulseEvents
                    // 2
                    .groupByKey()
                    // 3.1 - windowed aggregation
                    .windowedBy(tumblingWindows)
                    // 3.2 - windowed aggregation
                    .count(Materialized.as("pulse-counts"))
                    // 4
                    .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded().shutDownWhenFull()));

        pulseCounts
                .toStream()
                .print(Printed.<Windowed<String>, Long>toSysOut().withLabel("pulse-counts"));




        return builder.build();
    }
}
