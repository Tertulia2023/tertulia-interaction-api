package br.org.tertulia.interaction.api.application;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalInteractionApplicationTest {

    @Test
    void shouldUseDefaultPort() {
        assertEquals(
                18080,
                LocalInteractionApplication.resolvePort(
                        new String[0]
                )
        );
    }

    @Test
    void shouldAcceptConfiguredPort() {
        assertEquals(
                19090,
                LocalInteractionApplication.resolvePort(
                        new String[]{"19090"}
                )
        );
    }

    @Test
    void shouldRejectInvalidPortConfiguration() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        LocalInteractionApplication.resolvePort(
                                new String[]{"invalid"}
                        )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        LocalInteractionApplication.resolvePort(
                                new String[]{"0"}
                        )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        LocalInteractionApplication.resolvePort(
                                new String[]{"65536"}
                        )
        );
    }

    @Test
    void shouldRunWithEmptyKnowledgeGraph()
            throws Exception {

        try (LocalInteractionApplication application =
                     LocalInteractionApplication.create(0)) {

            application.start();

            URI uri =
                    URI.create(
                            "http://127.0.0.1:"
                                    + application.getPort()
                                    + "/interactions"
                    );

            HttpRequest request =
                    HttpRequest.newBuilder(uri)
                            .timeout(
                                    Duration.ofSeconds(5)
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            """
                                            {
                                              "input": "No processo Execução da Ação 210V, a atividade Capacitação dos agricultores aconteceu?"
                                            }
                                            """
                                    )
                            )
                            .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient()
                            .send(
                                    request,
                                    HttpResponse.BodyHandlers.ofString()
                            );

            assertEquals(
                    200,
                    response.statusCode()
            );

            assertTrue(
                    response.body()
                            .contains(
                                    "\"understood\":true"
                            )
            );

            assertTrue(
                    response.body()
                            .contains(
                                    "\"evidences\":[]"
                            )
            );

            assertTrue(
                    response.body()
                            .contains(
                                    "\"sources\":[]"
                            )
            );

            assertTrue(
                    response.body()
                            .contains(
                                    "No node with the requested subject exists in the Kernel"
                            )
            );
        }
    }

    @Test
    void shouldReleasePortWhenClosed()
            throws Exception {

        int port;

        try (LocalInteractionApplication firstApplication =
                     LocalInteractionApplication.create(0)) {

            firstApplication.start();
            port = firstApplication.getPort();
        }

        try (LocalInteractionApplication secondApplication =
                     LocalInteractionApplication.create(port)) {

            secondApplication.start();

            assertEquals(
                    port,
                    secondApplication.getPort()
            );
        }
    }
}