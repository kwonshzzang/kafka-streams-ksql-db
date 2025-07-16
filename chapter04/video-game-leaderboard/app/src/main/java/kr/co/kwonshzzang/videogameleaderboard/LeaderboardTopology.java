package kr.co.kwonshzzang.videogameleaderboard;

import kr.co.kwonshzzang.videogameleaderboard.model.Player;
import kr.co.kwonshzzang.videogameleaderboard.model.Product;
import kr.co.kwonshzzang.videogameleaderboard.model.ScoreEvent;
import kr.co.kwonshzzang.videogameleaderboard.model.join.Enriched;
import kr.co.kwonshzzang.videogameleaderboard.model.join.ScoreWithPlayer;
import kr.co.kwonshzzang.videogameleaderboard.serialization.json.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

public class LeaderboardTopology {

    public static Topology build() {
        // the builder is used to construct the topology
        StreamsBuilder builder = new StreamsBuilder();

        // source: register the score events stream
        KStream<String, ScoreEvent> scoreEventKStream =
                builder.stream(
                        "score-events",
                        Consumed.with(Serdes.String(), JsonSerdes.ScoreEvent()))
                        // Key가 없음. 리파티셔닝이 필요함
                        .selectKey((key, value) ->String.valueOf(value.getPlayerId()));

        // source: create the sharded players table
        KTable<String, Player> playerKTable =
                builder.table(
                    "players",
                    Consumed.with(Serdes.String(), JsonSerdes.Player()));

        // source: create the global product table
        GlobalKTable<String, Product> productGlobalKTable =
                builder.globalTable(
                        "products",
                        Consumed.with(Serdes.String(), JsonSerdes.Product())
                );


        scoreEventKStream.print(Printed.toSysOut());

        // Joiner는 Join한 StateStore를 저장할 형태를 의미한다.
        KStream<String, ScoreWithPlayer> scoreWithPlayerKStream =
                scoreEventKStream.join(
                        playerKTable,
                        ScoreWithPlayer::new,
                        Joined.with(Serdes.String(), JsonSerdes.ScoreEvent(), JsonSerdes.Player())
                );
        scoreWithPlayerKStream.print(Printed.toSysOut());

        // Joiner는 Join한 StateStore를 저장할 형태을 의미한다.
        KStream<String, Enriched> enrichedKStream =
             scoreWithPlayerKStream.join(
                 productGlobalKTable,
                 (key, value) -> String.valueOf(value.getScoreEvent().getProductId()),
                 (readOnlyKey, scoreWithPlayer, product) -> new Enriched(scoreWithPlayer, product)
             );

        enrichedKStream.print(Printed.toSysOut());

        // Group the enriched product stream
        KGroupedStream<String, Enriched> grouped =
                enrichedKStream.groupBy(
                    (key, value) -> String.valueOf(value.getProductId()),
                        Grouped.with(Serdes.String(), JsonSerdes.Enriched())
                );

        // The Initial value of our aggregation wil be a new HighScores instances
        Initializer<HighScores> highScoresInitializer = HighScores::new;

        // The logic for aggregating high scores is implemented in the HighScores.add method
        Aggregator<String, Enriched, HighScores> highScoresAggregator =
                (key, value, aggregate) -> aggregate.add(value);

        KTable<String, HighScores> highScores =
                grouped.aggregate(
                        highScoresInitializer,
                        highScoresAggregator,
                        // give the state store an explicit name to make it available for interactive queries
                        Materialized.<String, HighScores, KeyValueStore<Bytes, byte[]>>
                            as("leader-boards")
                            .withKeySerde(Serdes.String())
                            .withValueSerde(JsonSerdes.HighScores())
                );

        // KTable example: players aggregation
        KGroupedTable<String, Player> groupedTable =
                playerKTable.groupBy(
                        KeyValue::pair,
                        Grouped.with(Serdes.String(), JsonSerdes.Player())
                );

        groupedTable.aggregate(
                () -> 0L,
                (key, value, aggregate) -> aggregate + 1L,
                (key, value, aggregate) -> aggregate - 1L
        );



        highScores.toStream().to("high-scores");

        return builder.build();
    }
}
