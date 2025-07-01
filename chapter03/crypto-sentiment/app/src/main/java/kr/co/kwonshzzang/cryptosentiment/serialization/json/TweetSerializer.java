package kr.co.kwonshzzang.cryptosentiment.serialization.json;

import com.google.gson.Gson;
import kr.co.kwonshzzang.cryptosentiment.serialization.Tweet;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

public class TweetSerializer implements Serializer<Tweet> {
    private final Gson gson = new Gson();

    @Override
    public byte[] serialize(String topic, Tweet tweet) {
        if(tweet == null) return null;
        return gson.toJson(tweet).getBytes(StandardCharsets.UTF_8);
    }
}
