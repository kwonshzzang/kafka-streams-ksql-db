package kr.co.kwonshzzang.cryptosentiment.language;

import com.google.common.base.Splitter;
import kr.co.kwonshzzang.cryptosentiment.model.EntitySentiment;
import kr.co.kwonshzzang.cryptosentiment.serialization.Tweet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DummyClient implements LanguageClient {
    @Override
    public Tweet translate(Tweet tweet, String targetLanguage) {
        tweet.setText("Translated: " + tweet.getText());
        return tweet;
    }

    @Override
    public List<EntitySentiment> getEntitySentimentList(Tweet tweet) {
        List<EntitySentiment> results = new ArrayList<>();

        Iterable<String> words = Splitter.on(" ").split(tweet.getText().toLowerCase().replaceAll("#", ""));
        for(String entity : words) {
            EntitySentiment entitySentiment =
                    EntitySentiment.newBuilder()
                            .setCreatedAt(tweet.getCreatedAt())
                            .setId(tweet.getId())
                            .setEntity(entity)
                            .setText(tweet.getText())
                            .setSalience(randomDouble())
                            .setSentimentScore(randomDouble())
                            .setSentimentMagnitude(randomDouble())
                            .build();
            results.add(entitySentiment);
        }

        return results;
    }

    Double randomDouble() {
        return ThreadLocalRandom.current().nextDouble(0, 1);
    }
}
