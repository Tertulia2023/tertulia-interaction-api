package br.org.tertulia.interaction.api.http;

import br.org.tertulia.interaction.api.InteractionRequestProcessor;
import br.org.tertulia.interaction.contract.InteractionInterpretation;
import br.org.tertulia.interaction.presenter.DefaultInteractionPresenter;
import br.org.tertulia.interaction.service.DefaultInteractionService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractionHttpServerTest {

    @Test
    void shouldExposePostInteractionsLocally()
            throws Exception {

        try (InteractionHttpServer server =
                     server()) {

            server.start();

            HttpResponse<String> response =
                    send(
                            server,
                            "POST",
                            """
                            {
                              "input": "pergunta ainda não suportada"
                            }
                            """
                    );

            assertEquals(
                    200,
                    response.statusCode()
            );

            assertTrue(
                    response.headers()
                            .firstValue("Content-Type")
                            .orElseThrow()
                            .startsWith("application/json")
            );

            assertTrue(
                    response.body()
                            .contains("\"interactionId\":")
            );

            assertTrue(
                    response.body()
                            .contains("\"understood\":false")
            );
        }
    }

    @Test
    void shouldReturnBadRequestForMalformedJson()
            throws Exception {

        try (InteractionHttpServer server =
                     server()) {

            server.start();

            HttpResponse<String> response =
                    send(
                            server,
                            "POST",
                            """
                            {
                              "input":
                            """
                    );

            assertEquals(
                    400,
                    response.statusCode()
            );

            assertTrue(
                    response.body()
                            .contains(
                                    "\"error\":\"malformed_json\""
                            )
            );
        }
    }

    @Test
    void shouldRejectNonPostMethod()
            throws Exception {

        try (InteractionHttpServer server =
                     server()) {

            server.start();

            HttpResponse<String> response =
                    send(
                            server,
                            "GET",
                            null
                    );

            assertEquals(
                    405,
                    response.statusCode()
            );

            assertEquals(
                    "POST",
                    response.headers()
                            .firstValue("Allow")
                            .orElseThrow()
            );
        }
    }

    private static InteractionHttpServer server()
            throws IOException {

        DefaultInteractionService interactionService =
                new DefaultInteractionService(
                        request ->
                                new InteractionInterpretation(
                                        request.getId(),
                                        false,
                                        null,
                                        List.of(
                                                "Unsupported interaction in HTTP transport test"
                                        )
                                ),
                        request -> {
                            throw new AssertionError(
                                    "query gateway must not be called"
                            );
                        },
                        new DefaultInteractionPresenter()
                );

        return new InteractionHttpServer(
                new InteractionRequestProcessor(
                        interactionService
                ),
                0
        );
    }

    private static HttpResponse<String> send(
            InteractionHttpServer server,
            String method,
            String body
    ) throws Exception {

        URI uri =
                URI.create(
                        "http://127.0.0.1:"
                                + server.getPort()
                                + "/interactions"
                );

        HttpRequest.Builder builder =
                HttpRequest.newBuilder(uri)
                        .timeout(
                                Duration.ofSeconds(5)
                        );

        if ("POST".equals(method)) {
            builder.header(
                    "Content-Type",
                    "application/json"
            );

            builder.POST(
                    HttpRequest.BodyPublishers.ofString(
                            body
                    )
            );
        } else {
            builder.method(
                    method,
                    HttpRequest.BodyPublishers.noBody()
            );
        }

        return HttpClient.newHttpClient()
                .send(
                        builder.build(),
                        HttpResponse.BodyHandlers.ofString()
                );
    }
}