package com.example.helloworld.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import org.hibernate.validator.constraints.Length;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;

public class Saying {
    private long id;

    @Length(max = 3)
    private String content;
    
    @NotNull
    private DateTime time;

    public Saying() {
        // Jackson deserialization
    }

    public Saying(long id, String content, DateTime time) {
        this.id = id;
        this.content = content;
        this.time = time;
    }

    @JsonProperty
    public long getId() {
        return id;
    }

    @JsonProperty
    public String getContent() {
        return content;
    }

    @JsonProperty
    public DateTime getTime() {
        return time;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("content", content)
                .add("time", time)
                .toString();
    }
}
