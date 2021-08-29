package com.library.ops.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManageBooksExceptionEntity {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected String userMessage;
}

