package kr.co.kwonshzzang.cryptosentiment;

import kr.co.kwonshzzang.cryptosentiment.serialization.Tweet;
import kr.co.kwonshzzang.cryptosentiment.serialization.json.TweetSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;

import java.util.Map;

public class CryptoTopology {

    public static Topology build() {
        // the builder is used to construct the topology
        StreamsBuilder builder = new StreamsBuilder();

        // start streaming tweets using our custom value serdes. Note: regarding
        // the key serdes (Serdes.ByteArray()), if could also use Serdes.Void()
        // if we always expect our keys to be null
        KStream<byte[], Tweet> stream = builder.stream("tweets", Consumed.with(Serdes.ByteArray(), new TweetSerdes()));
        stream.print(Printed.<byte[], Tweet>toSysOut().withLabel("tweets-stream"));

        // filter out retweets
        KStream<byte[], Tweet> filtered = stream.filterNot(
                (key, tweet) -> {
                    return tweet.isRetweet();
                }
        );
        filtered.print(Printed.<byte[], Tweet>toSysOut().withLabel("tweets-filtered"));

        // branch  based on tweet language
        Map<String, KStream<byte[], Tweet>> branches = filtered.split(Named.as("my-"))
                .branch((key, tweet) -> tweet.getLang().equals("en"), Branched.as("english"))
                .branch((key, tweet) -> !tweet.getLang().equals("en"), Branched.as("non-english"))
                .noDefaultBranch();

        // English tweets
        KStream<byte[], Tweet> englishStream = branches.get("my-english");
        englishStream.print(Printed.<byte[], Tweet>toSysOut().withLabel("tweets-english"));

        // Non-English tweets
        KStream<byte[], Tweet> nonEnglishStream = branches.get("my-non-english");
        nonEnglishStream.print(Printed.<byte[], Tweet>toSysOut().withLabel("tweets-non-english"));

        return builder.build();


    }
}
