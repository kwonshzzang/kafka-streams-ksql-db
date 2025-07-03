package kr.co.kwonshzzang.cryptosentiment;

import kr.co.kwonshzzang.cryptosentiment.language.DummyClient;
import kr.co.kwonshzzang.cryptosentiment.language.GcpClient;
import kr.co.kwonshzzang.cryptosentiment.language.LanguageClient;
import kr.co.kwonshzzang.cryptosentiment.model.EntitySentiment;
import kr.co.kwonshzzang.cryptosentiment.serialization.Tweet;
import kr.co.kwonshzzang.cryptosentiment.serialization.avro.AvroSerdes;
import kr.co.kwonshzzang.cryptosentiment.serialization.json.TweetSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CryptoTopology {
    private static final List<String> currencies = Arrays.asList("bitcoin", "ethereum");

    public static Topology build() {
        if(System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null) {
            return build(new GcpClient());
        }
        return build(new DummyClient());
    }

    public static Topology build(LanguageClient languageClient) {
        return build(languageClient, true);
    }

    public static Topology build(LanguageClient languageClient, boolean useSchemaRegistry) {
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

        // for non-English tweets, translate the tweet text first.
//        KStream<byte[], Tweet> translatedStream = nonEnglishStream.map(
//                (key, tweet) -> {
//                    byte[] newKey = tweet.getUsername().getBytes();
//                    Tweet translatedTweet = languageClient.translate(tweet, "en");
//                    return KeyValue.pair(newKey, translatedTweet);
//                }
//        );
        KStream<byte[], Tweet> translatedStream = nonEnglishStream.mapValues(
                (tweet -> {
                    return languageClient.translate(tweet, "en");
                })
        );

        // merge the two tweets
        KStream<byte[], Tweet> merged = englishStream.merge(translatedStream);

        // enrich with sentiment and salience scores
        // note: the EntitySentiment class is  auto-generated from the schema
        // definition in src/main/avro/entity_sentiment.avsc
        KStream<byte[], EntitySentiment> enriched =
                merged.flatMapValues(
                        (tweet) -> {
                            // perform entity-level sentiment analysis
                            List<EntitySentiment> results = languageClient.getEntitySentimentList(tweet);

                            // remove all entity results that don't match a currency
                            results.removeIf(
                              entitySentiment -> !currencies.contains(entitySentiment.getEntity())
                            );
                            return results;
                        }
                );

        // write to the output topic. note: the following code shows how to use
        // both a registry-aware Avro Serde and a registryless Avro Serde
        if (useSchemaRegistry) {
            enriched.to(
                    "crypto-sentiment",
                    // registry-aware Avro Serde
                    Produced.with(
                            Serdes.ByteArray(), AvroSerdes.EntitySentiment("http://localhost:8081", false)
                    )
            );
        } else {
            enriched.to(
                    "crypto-sentiment",
                    // registryless Avro Serde
                    Produced.with(
                            Serdes.ByteArray(),
                            com.mitchseymour.kafka.serialization.avro.AvroSerdes.get(EntitySentiment.class)
                    )
            );
        }

        return builder.build();


    }
}
