package com.library.ops.bo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookBO {
    @JsonProperty
    protected String isbn;
    @JsonProperty
    protected String title;
    @JsonProperty
    protected String author;
    @JsonProperty
    protected List<String> tags;
}
