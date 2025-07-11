package kr.co.kwonshzzang.videogameleaderboard;

import kr.co.kwonshzzang.videogameleaderboard.model.Player;
import kr.co.kwonshzzang.videogameleaderboard.model.Product;
import kr.co.kwonshzzang.videogameleaderboard.model.ScoreEvent;
import kr.co.kwonshzzang.videogameleaderboard.serialization.json.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

public class LeaderboardTopology {

    public static Topology build() {
        // the builder is used to construct the topology
        StreamsBuilder builder = new StreamsBuilder();

        // register the score events stream
        KStream<String, ScoreEvent> scoreEvent =
                builder.stream(
                        "score-events",
                        Consumed.with(Serdes.ByteArray(), JsonSerdes.ScoreEvent()))
                        // now marked for re-partitioning
                        .selectKey((k, v) -> v.getPlayerId().toString());

        // create the sharded players table
        KTable<String, Player> players =
                builder.table(
                    "players",
                    Consumed.with(Serdes.String(), JsonSerdes.Player()));


        // create the global product table
        GlobalKTable<String, Product> products =
                builder.globalTable(
                        "products",
                        Consumed.with(Serdes.String(), JsonSerdes.Product())
                );

        return builder.build();
    }
}
