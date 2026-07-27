package br.org.tertulia.interaction.api.application;

import br.org.tertulia.interaction.api.InteractionRequestProcessor;
import br.org.tertulia.interaction.api.http.InteractionHttpServer;
import br.org.tertulia.interaction.interpreter.DeterministicInteractionInterpreter;
import br.org.tertulia.interaction.presenter.DefaultInteractionPresenter;
import br.org.tertulia.interaction.query.LocalInteractionQueryGateway;
import br.org.tertulia.interaction.service.DefaultInteractionService;
import br.org.tertulia.kernel.DefaultKnowledgeProcessReconstructor;
import br.org.tertulia.kernel.InMemoryKnowledgeGraph;
import br.org.tertulia.query.service.DefaultQueryService;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LocalInteractionApplication
        implements AutoCloseable {

    private static final int DEFAULT_PORT = 18080;

    private final InteractionHttpServer server;
    private final CountDownLatch shutdownLatch;
    private final AtomicBoolean closed;

    private LocalInteractionApplication(
            InteractionHttpServer server
    ) {
        this.server = server;
        this.shutdownLatch = new CountDownLatch(1);
        this.closed = new AtomicBoolean(false);
    }

    public static LocalInteractionApplication create(
            int port
    ) throws IOException {

        InMemoryKnowledgeGraph graph =
                new InMemoryKnowledgeGraph();

        DefaultQueryService queryService =
                new DefaultQueryService(
                        graph,
                        new DefaultKnowledgeProcessReconstructor(
                                graph
                        )
                );

        DefaultInteractionService interactionService =
                new DefaultInteractionService(
                        new DeterministicInteractionInterpreter(),
                        new LocalInteractionQueryGateway(
                                queryService
                        ),
                        new DefaultInteractionPresenter()
                );

        return new LocalInteractionApplication(
                new InteractionHttpServer(
                        new InteractionRequestProcessor(
                                interactionService
                        ),
                        port
                )
        );
    }

    public void start() {
        server.start();
    }

    public int getPort() {
        return server.getPort();
    }

    public void awaitShutdown()
            throws InterruptedException {
        shutdownLatch.await();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            server.close();
            shutdownLatch.countDown();
        }
    }

    static int resolvePort(
            String[] args
    ) {
        if (args == null) {
            throw new IllegalArgumentException(
                    "args must not be null"
            );
        }

        if (args.length == 0) {
            return DEFAULT_PORT;
        }

        if (args.length != 1) {
            throw new IllegalArgumentException(
                    "expected zero or one port argument"
            );
        }

        final int port;

        try {
            port = Integer.parseInt(
                    args[0]
            );
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "port must be an integer",
                    exception
            );
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                    "port must be between 1 and 65535"
            );
        }

        return port;
    }

    public static void main(
            String[] args
    ) throws Exception {

        int port =
                resolvePort(args);

        LocalInteractionApplication application =
                create(port);

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                application::close,
                                "tertulia-interaction-api-shutdown"
                        )
                );

        application.start();

        System.out.println(
                "TERTULIA Interaction API listening on "
                        + "http://127.0.0.1:"
                        + application.getPort()
                        + "/interactions"
        );

        System.out.println(
                "Knowledge graph initialized empty; "
                        + "no territorial knowledge loaded."
        );

        application.awaitShutdown();
    }
}