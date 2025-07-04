package kr.co.kwonshzzang.cryptosentiment.language;

import com.google.cloud.language.v1.*;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import kr.co.kwonshzzang.cryptosentiment.model.EntitySentiment;
import kr.co.kwonshzzang.cryptosentiment.serialization.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GcpClient implements LanguageClient {
    private static final Logger log = LoggerFactory.getLogger(GcpClient.class);

    private static ThreadLocal<LanguageServiceClient> nlpClients =
            ThreadLocal.withInitial(
                    () -> {
                        try {
                            LanguageServiceSettings settings = LanguageServiceSettings.newBuilder().build();
                            LanguageServiceClient client = LanguageServiceClient.create(settings);
                            return client;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

    @Override
    public Tweet translate(Tweet tweet, String targetLanguage) {
        // instantiate a client
        Translate translate = TranslateOptions.getDefaultInstance().getService();

        // translate the tweet text into the target language
        Translation translation = translate.translate(tweet.getText(),
                Translate.TranslateOption.sourceLanguage("en"),
                Translate.TranslateOption.targetLanguage(targetLanguage));

        // if you want to get real functional, clone the tweet and set the text on the new object
        tweet.setText(translation.getTranslatedText());
        return tweet;
    }

    @Override
    public List<EntitySentiment> getEntitySentimentList(Tweet tweet) {
        List<EntitySentiment> results = new ArrayList<>();

        try {
            Document doc = Document.newBuilder().setContent(tweet.getText()).setType(Document.Type.PLAIN_TEXT).build();
            AnalyzeEntitySentimentRequest request = AnalyzeEntitySentimentRequest
                    .newBuilder()
                    .setDocument(doc)
                    .setEncodingType(EncodingType.UTF8)
                    .build();

            // Detects the sentiment of the text
            AnalyzeEntitySentimentResponse response = nlpClients.get().analyzeEntitySentiment(request);
            for(Entity entity: response.getEntitiesList()) {
                Sentiment sentiment = entity.getSentiment();
                EntitySentiment entitySentiment = EntitySentiment
                        .newBuilder()
                        .setCreatedAt(tweet.getCreatedAt())
                        .setId(tweet.getId())
                        .setEntity(entity.getName().replace("#", "").toLowerCase())
                        .setText(tweet.getText())
                        .setSalience((double) entity.getSalience())
                        .setSentimentScore((double) sentiment.getScore())
                        .setSentimentMagnitude((double) sentiment.getMagnitude())
                        .build();
                results.add(entitySentiment);
            }
        } catch (Exception e) {
            log.error("Could not detect sentiment for provided text : {}", tweet.getText(), e);
            throw e;
        }
        return results;
    }
}
