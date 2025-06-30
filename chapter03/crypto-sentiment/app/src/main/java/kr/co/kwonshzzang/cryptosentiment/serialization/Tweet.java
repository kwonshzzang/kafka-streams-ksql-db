package kr.co.kwonshzzang.cryptosentiment.serialization;

import com.google.gson.annotations.SerializedName;

public class Tweet {
    @SerializedName("CreatedAt")
    private Long createdAt;

    @SerializedName("Id")
    private Long id;

    @SerializedName("Lang")
    private String lang;

    @SerializedName("Text")
    private String text;

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
