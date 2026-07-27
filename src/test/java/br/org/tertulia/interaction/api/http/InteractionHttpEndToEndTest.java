package br.org.tertulia.interaction.api.http;

import br.org.tertulia.interaction.api.InteractionRequestProcessor;
import br.org.tertulia.interaction.interpreter.DeterministicInteractionInterpreter;
import br.org.tertulia.interaction.presenter.DefaultInteractionPresenter;
import br.org.tertulia.interaction.query.LocalInteractionQueryGateway;
import br.org.tertulia.interaction.service.DefaultInteractionService;
import br.org.tertulia.kernel.DefaultKnowledgeActivity;
import br.org.tertulia.kernel.DefaultKnowledgeEvidence;
import br.org.tertulia.kernel.DefaultKnowledgeLink;
import br.org.tertulia.kernel.DefaultKnowledgeProcess;
import br.org.tertulia.kernel.DefaultKnowledgeProcessReconstructor;
import br.org.tertulia.kernel.DefaultKnowledgeSource;
import br.org.tertulia.kernel.EvidenceType;
import br.org.tertulia.kernel.InMemoryKnowledgeGraph;
import br.org.tertulia.kernel.KnowledgeActivity;
import br.org.tertulia.kernel.KnowledgeDomain;
import br.org.tertulia.kernel.KnowledgeEvidence;
import br.org.tertulia.kernel.KnowledgeLinkType;
import br.org.tertulia.kernel.KnowledgeProcess;
import br.org.tertulia.kernel.KnowledgeSource;
import br.org.tertulia.query.service.DefaultQueryService;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractionHttpEndToEndTest {

    @Test
    void shouldExecuteVerifyOccurrenceThroughCompleteStack()
            throws Exception {

        HttpResponse<String> response =
                execute(
                        """
                        {
                          "input": "No processo Execução da Ação 210V, a atividade Capacitação dos agricultores aconteceu?"
                        }
                        """
                );

        assertSuccessfulStructuredResponse(response);

        assertTrue(
                response.body().contains(
                        "\"understood\":true"
                )
        );

        assertTrue(
                response.body().contains(
                        "\"EVIDENCE:evidencia-capacitacao-001\""
                )
        );

        assertTrue(
                response.body().contains(
                        "\"SOURCE:lista-presenca-001\""
                )
        );
    }

    @Test
    void shouldExecuteFindEvidenceThroughCompleteStack()
            throws Exception {

        HttpResponse<String> response =
                execute(
                        """
                        {
                          "input": "No processo Execução da Ação 210V, quais evidências existem para a atividade Capacitação dos agricultores?"
                        }
                        """
                );

        assertSuccessfulStructuredResponse(response);

        assertTrue(
                response.body().contains(
                        "\"understood\":true"
                )
        );

        assertTrue(
                response.body().contains(
                        "\"EVIDENCE:evidencia-capacitacao-001\""
                )
        );
    }

    @Test
    void shouldExecuteIdentifySourceThroughCompleteStack()
            throws Exception {

        HttpResponse<String> response =
                execute(
                        """
                        {
                          "input": "No processo Execução da Ação 210V, qual é a fonte da atividade Capacitação dos agricultores?"
                        }
                        """
                );

        assertSuccessfulStructuredResponse(response);

        assertTrue(
                response.body().contains(
                        "\"understood\":true"
                )
        );

        assertTrue(
                response.body().contains(
                        "\"SOURCE:lista-presenca-001\""
                )
        );
    }

    private static void assertSuccessfulStructuredResponse(
            HttpResponse<String> response
    ) {

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
                response.body().contains(
                        "\"interactionId\":"
                )
        );

        assertTrue(
                response.body().contains(
                        "\"confidence\":"
                )
        );

        assertTrue(
                response.body().contains(
                        "\"trace\":"
                )
        );

        assertTrue(
                response.body().contains(
                        "\"queryRequestId\":"
                )
        );

        assertTrue(
                response.body().contains(
                        "\"queryResponseRequestId\":"
                )
        );
    }

    private static HttpResponse<String> execute(
            String requestBody
    ) throws Exception {

        try (InteractionHttpServer server =
                     serverWithCompleteStack()) {

            server.start();

            URI uri =
                    URI.create(
                            "http://127.0.0.1:"
                                    + server.getPort()
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
                                    HttpRequest.BodyPublishers
                                            .ofString(requestBody)
                            )
                            .build();

            return HttpClient.newHttpClient()
                    .send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );
        }
    }

    private static InteractionHttpServer
    serverWithCompleteStack()
            throws Exception {

        InMemoryKnowledgeGraph graph =
                graphWithEvidence();

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

        return new InteractionHttpServer(
                new InteractionRequestProcessor(
                        interactionService
                ),
                0
        );
    }

    private static InMemoryKnowledgeGraph
    graphWithEvidence() {

        KnowledgeDomain domain =
                new KnowledgeDomain(
                        "Desenvolvimento Rural",
                        "Comunidade da Uruba - Mataraca/PB",
                        "desenvolvimento-rural/acao-210v",
                        "ACTIVE",
                        "1.0"
                );

        KnowledgeProcess process =
                new DefaultKnowledgeProcess(
                        "acao-210v",
                        "Execução da Ação 210V",
                        "Estruturação produtiva da comunidade da Uruba",
                        domain
                );

        KnowledgeActivity activity =
                new DefaultKnowledgeActivity(
                        "capacitacao-agricultores",
                        "Capacitação dos agricultores",
                        "Capacitação prevista no processo",
                        process
                );

        KnowledgeSource source =
                new DefaultKnowledgeSource(
                        "lista-presenca-001",
                        "Lista de presença da capacitação",
                        "Equipe técnica",
                        "ACPRM",
                        "2026",
                        "Desenvolvimento Rural",
                        "DOCUMENT"
                );

        KnowledgeEvidence evidence =
                new DefaultKnowledgeEvidence(
                        "evidencia-capacitacao-001",
                        "Participação dos agricultores",
                        "Lista que comprova a realização da capacitação",
                        EvidenceType.ATTENDANCE_LIST,
                        activity,
                        source
                );

        InMemoryKnowledgeGraph graph =
                new InMemoryKnowledgeGraph();

        graph.addLink(
                new DefaultKnowledgeLink(
                        process,
                        KnowledgeLinkType.HAS_ACTIVITY,
                        activity
                )
        );

        graph.addLink(
                new DefaultKnowledgeLink(
                        activity,
                        KnowledgeLinkType.PRODUCES_EVIDENCE,
                        evidence
                )
        );

        graph.addLink(
                new DefaultKnowledgeLink(
                        evidence,
                        KnowledgeLinkType.USES_SOURCE,
                        source
                )
        );

        return graph;
    }
}