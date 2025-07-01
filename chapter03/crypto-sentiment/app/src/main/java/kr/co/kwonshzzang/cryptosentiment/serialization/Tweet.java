package kr.co.kwonshzzang.cryptosentiment.serialization;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Tweet {
    @SerializedName("CreatedAt")
    private Long createdAt;

    @SerializedName("Id")
    private Long id;

    @SerializedName("Lang")
    private String lang;

    @SerializedName("Retweet")
    private Boolean retweet;

    @SerializedName("Text")
    private String text;

    public Boolean isRetweet(){
        return retweet;
    }
}
