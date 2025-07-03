package kr.co.kwonshzzang.cryptosentiment.language;

import kr.co.kwonshzzang.cryptosentiment.model.EntitySentiment;
import kr.co.kwonshzzang.cryptosentiment.serialization.Tweet;

import java.util.List;

public interface LanguageClient {
    public Tweet translate(Tweet tweet, String targetLanguage);

    public List<EntitySentiment> getEntitySentimentList(Tweet tweet);
}
