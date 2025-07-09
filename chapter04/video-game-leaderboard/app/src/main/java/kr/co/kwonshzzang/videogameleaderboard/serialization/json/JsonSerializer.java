package kr.co.kwonshzzang.videogameleaderboard.serialization.json;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JsonSerializer<T> implements Serializer<T> {
    private final Gson gson  = new GsonBuilder()
            . setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    /** Default constructor needed by kafka */
    public JsonSerializer() {}

    @Override
    public void configure(Map<String, ?> props, boolean isKey) {}

    @Override
    public byte[] serialize(String topic, T type) {
        return gson.toJson(type).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void close() {}
}
