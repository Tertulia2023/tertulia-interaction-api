package br.org.tertulia.interaction.api;

import br.org.tertulia.interaction.api.transport.InteractionJsonCodec;
import br.org.tertulia.interaction.api.transport.InteractionJsonRequest;
import br.org.tertulia.interaction.api.transport.InteractionJsonResponse;
import br.org.tertulia.interaction.api.transport.InteractionTransportMapper;
import br.org.tertulia.interaction.contract.InteractionRequest;
import br.org.tertulia.interaction.contract.InteractionResponse;
import br.org.tertulia.interaction.service.DefaultInteractionService;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public final class InteractionRequestProcessor {

    private final InteractionJsonCodec codec;
    private final InteractionTransportMapper mapper;
    private final Function<InteractionRequest, InteractionResponse> executor;
    private final Supplier<String> interactionIdSupplier;

    public InteractionRequestProcessor(
            DefaultInteractionService interactionService
    ) {
        this(
                new InteractionJsonCodec(),
                new InteractionTransportMapper(),
                Objects.requireNonNull(
                        interactionService,
                        "interactionService must not be null"
                )::interact,
                () -> UUID.randomUUID().toString()
        );
    }

    InteractionRequestProcessor(
            InteractionJsonCodec codec,
            InteractionTransportMapper mapper,
            Function<InteractionRequest, InteractionResponse> executor,
            Supplier<String> interactionIdSupplier
    ) {
        this.codec = Objects.requireNonNull(
                codec,
                "codec must not be null"
        );
        this.mapper = Objects.requireNonNull(
                mapper,
                "mapper must not be null"
        );
        this.executor = Objects.requireNonNull(
                executor,
                "executor must not be null"
        );
        this.interactionIdSupplier = Objects.requireNonNull(
                interactionIdSupplier,
                "interactionIdSupplier must not be null"
        );
    }

    public String process(
            String requestJson
    ) throws JsonProcessingException {

        InteractionJsonRequest jsonRequest =
                codec.readRequest(requestJson);

        String interactionId =
                requireNonBlank(
                        interactionIdSupplier.get(),
                        "generated interactionId"
                );

        InteractionRequest request =
                mapper.toInteractionRequest(
                        interactionId,
                        jsonRequest
                );

        InteractionResponse response =
                Objects.requireNonNull(
                        executor.apply(request),
                        "interaction response must not be null"
                );

        InteractionJsonResponse jsonResponse =
                mapper.toJsonResponse(response);

        return codec.writeResponse(jsonResponse);
    }

    private static String requireNonBlank(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    fieldName + " must not be null or blank"
            );
        }

        return value;
    }
}