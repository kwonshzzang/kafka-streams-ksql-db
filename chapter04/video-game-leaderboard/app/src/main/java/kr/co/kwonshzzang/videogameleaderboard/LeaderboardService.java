package kr.co.kwonshzzang.videogameleaderboard;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class LeaderboardService {
    private final HostInfo hostInfo;
    private final KafkaStreams streams;

    public LeaderboardService(HostInfo hostInfo, KafkaStreams streams) {
        this.hostInfo = hostInfo;
        this.streams = streams;
    }

    public ReadOnlyKeyValueStore<String, HighScores> getStore() {
        return streams.store (
                StoreQueryParameters.fromNameAndType(
                        // state store name
                        "leader-boards",
                        // state store type
                        QueryableStoreTypes.keyValueStore()
                )
        );
    }

    void start() {

    }
}
