package br.org.tertulia.interaction.api.http;

import br.org.tertulia.interaction.api.InteractionRequestProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class InteractionHttpServer
        implements AutoCloseable {

    private static final String INTERACTIONS_PATH =
            "/interactions";

    private final HttpServer server;

    public InteractionHttpServer(
            InteractionRequestProcessor processor,
            int port
    ) throws IOException {

        Objects.requireNonNull(
                processor,
                "processor must not be null"
        );

        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException(
                    "port must be between 0 and 65535"
            );
        }

        this.server =
                HttpServer.create(
                        new InetSocketAddress(
                                "127.0.0.1",
                                port
                        ),
                        0
                );

        this.server.createContext(
                INTERACTIONS_PATH,
                new InteractionHandler(processor)
        );
    }

    public void start() {
        server.start();
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private static final class InteractionHandler
            implements HttpHandler {

        private final InteractionRequestProcessor processor;

        private InteractionHandler(
                InteractionRequestProcessor processor
        ) {
            this.processor = processor;
        }

        @Override
        public void handle(
                HttpExchange exchange
        ) throws IOException {

            try {
                if (!INTERACTIONS_PATH.equals(
                        exchange.getRequestURI().getPath()
                )) {
                    sendJson(
                            exchange,
                            404,
                            """
                            {"error":"not_found"}
                            """
                    );
                    return;
                }

                if (!"POST".equalsIgnoreCase(
                        exchange.getRequestMethod()
                )) {
                    exchange.getResponseHeaders()
                            .set("Allow", "POST");

                    sendJson(
                            exchange,
                            405,
                            """
                            {"error":"method_not_allowed"}
                            """
                    );
                    return;
                }

                String requestJson =
                        new String(
                                exchange.getRequestBody()
                                        .readAllBytes(),
                                StandardCharsets.UTF_8
                        );

                try {
                    String responseJson =
                            processor.process(requestJson);

                    sendJson(
                            exchange,
                            200,
                            responseJson
                    );
                } catch (JsonProcessingException exception) {
                    sendJson(
                            exchange,
                            400,
                            """
                            {"error":"malformed_json"}
                            """
                    );
                }
            } finally {
                exchange.close();
            }
        }

        private static void sendJson(
                HttpExchange exchange,
                int statusCode,
                String body
        ) throws IOException {

            byte[] bytes =
                    body.getBytes(
                            StandardCharsets.UTF_8
                    );

            exchange.getResponseHeaders()
                    .set(
                            "Content-Type",
                            "application/json; charset=UTF-8"
                    );

            exchange.sendResponseHeaders(
                    statusCode,
                    bytes.length
            );

            exchange.getResponseBody()
                    .write(bytes);
        }
    }
}