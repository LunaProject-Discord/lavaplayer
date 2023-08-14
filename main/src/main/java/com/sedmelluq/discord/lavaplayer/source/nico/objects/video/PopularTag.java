
package com.sedmelluq.discord.lavaplayer.source.nico.objects.video;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "tag",
    "regularizedTag",
    "rank",
    "genre",
    "dateTime"
})

public class PopularTag {

    @JsonProperty("tag")
    private String tag;
    @JsonProperty("regularizedTag")
    private String regularizedTag;
    @JsonProperty("rank")
    private Long rank;
    @JsonProperty("genre")
    private String genre;
    @JsonProperty("dateTime")
    private String dateTime;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("tag")
    public String getTag() {
        return tag;
    }

    @JsonProperty("tag")
    public void setTag(String tag) {
        this.tag = tag;
    }

    @JsonProperty("regularizedTag")
    public String getRegularizedTag() {
        return regularizedTag;
    }

    @JsonProperty("regularizedTag")
    public void setRegularizedTag(String regularizedTag) {
        this.regularizedTag = regularizedTag;
    }

    @JsonProperty("rank")
    public Long getRank() {
        return rank;
    }

    @JsonProperty("rank")
    public void setRank(Long rank) {
        this.rank = rank;
    }

    @JsonProperty("genre")
    public String getGenre() {
        return genre;
    }

    @JsonProperty("genre")
    public void setGenre(String genre) {
        this.genre = genre;
    }

    @JsonProperty("dateTime")
    public String getDateTime() {
        return dateTime;
    }

    @JsonProperty("dateTime")
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(PopularTag.class.getName()).append('@').append(Integer.toHexString(java.lang.System.identityHashCode(this))).append('[');
        sb.append("tag");
        sb.append('=');
        sb.append(((this.tag == null)?"<null>":this.tag));
        sb.append(',');
        sb.append("regularizedTag");
        sb.append('=');
        sb.append(((this.regularizedTag == null)?"<null>":this.regularizedTag));
        sb.append(',');
        sb.append("rank");
        sb.append('=');
        sb.append(((this.rank == null)?"<null>":this.rank));
        sb.append(',');
        sb.append("genre");
        sb.append('=');
        sb.append(((this.genre == null)?"<null>":this.genre));
        sb.append(',');
        sb.append("dateTime");
        sb.append('=');
        sb.append(((this.dateTime == null)?"<null>":this.dateTime));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(((this.additionalProperties == null)?"<null>":this.additionalProperties));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.dateTime == null)? 0 :this.dateTime.hashCode()));
        result = ((result* 31)+((this.regularizedTag == null)? 0 :this.regularizedTag.hashCode()));
        result = ((result* 31)+((this.genre == null)? 0 :this.genre.hashCode()));
        result = ((result* 31)+((this.rank == null)? 0 :this.rank.hashCode()));
        result = ((result* 31)+((this.tag == null)? 0 :this.tag.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PopularTag)) {
            return false;
        }
        PopularTag rhs = ((PopularTag) other);
        return ((((((Objects.equals(this.dateTime, rhs.dateTime))&&(Objects.equals(this.regularizedTag, rhs.regularizedTag)))&&(Objects.equals(this.genre, rhs.genre)))&&(Objects.equals(this.rank, rhs.rank)))&&(Objects.equals(this.tag, rhs.tag)))&&(Objects.equals(this.additionalProperties, rhs.additionalProperties)));
    }

}