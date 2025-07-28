package kr.co.kwonshzzang.patientmonitoring.serialization.json;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

public class JsonSerializer<T> implements Serializer<T> {
    private final Gson gson =
            new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

    // Default constructor needed by kafka
    public JsonSerializer() {}

    @Override
    public byte[] serialize(String topic, T type) {
        return gson.toJson(type).getBytes(StandardCharsets.UTF_8);
    }
}
