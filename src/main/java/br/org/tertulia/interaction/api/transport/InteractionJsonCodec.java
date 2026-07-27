package br.org.tertulia.interaction.api.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

public final class InteractionJsonCodec {

    private final ObjectMapper objectMapper;

    public InteractionJsonCodec() {
        this(new ObjectMapper());
    }

    InteractionJsonCodec(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "objectMapper must not be null"
        );
    }

    public InteractionJsonRequest readRequest(
            String json
    ) throws JsonProcessingException {
        Objects.requireNonNull(
                json,
                "json must not be null"
        );

        return objectMapper.readValue(
                json,
                InteractionJsonRequest.class
        );
    }

    public String writeResponse(
            InteractionJsonResponse response
    ) throws JsonProcessingException {
        Objects.requireNonNull(
                response,
                "response must not be null"
        );

        return objectMapper.writeValueAsString(
                response
        );
    }
}