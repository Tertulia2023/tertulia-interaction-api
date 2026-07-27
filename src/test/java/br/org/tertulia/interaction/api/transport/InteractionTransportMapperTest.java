package br.org.tertulia.interaction.api.transport;

import br.org.tertulia.interaction.contract.InteractionConfidence;
import br.org.tertulia.interaction.contract.InteractionEvidence;
import br.org.tertulia.interaction.contract.InteractionLimitation;
import br.org.tertulia.interaction.contract.InteractionLimitationOrigin;
import br.org.tertulia.interaction.contract.InteractionRequest;
import br.org.tertulia.interaction.contract.InteractionResponse;
import br.org.tertulia.interaction.contract.InteractionSource;
import br.org.tertulia.interaction.contract.InteractionTrace;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractionTransportMapperTest {

    private final InteractionTransportMapper mapper =
            new InteractionTransportMapper();

    @Test
    void shouldProjectJsonInputToInteractionRequest() {

        InteractionRequest request =
                mapper.toInteractionRequest(
                        "interaction-http-001",
                        new InteractionJsonRequest(
                                "No processo Execução da Ação 210V, "
                                        + "a atividade Capacitação dos agricultores "
                                        + "aconteceu?"
                        )
                );

        assertEquals(
                "interaction-http-001",
                request.getId()
        );

        assertTrue(
                request.getInput()
                        .contains("Capacitação dos agricultores")
        );
    }

    @Test
    void shouldProjectUnderstoodInteractionResponse() {

        InteractionResponse response =
                new InteractionResponse(
                        "interaction-http-002",
                        true,
                        "Consulta executada.",
                        List.of(
                                new InteractionEvidence(
                                        "EVIDENCE:evidence-001",
                                        "Evidência",
                                        "Descrição da evidência",
                                        "DOCUMENT"
                                )
                        ),
                        List.of(
                                new InteractionSource(
                                        "SOURCE:source-001",
                                        "Fonte",
                                        "ACPRM",
                                        "DOCUMENT"
                                )
                        ),
                        new InteractionConfidence(
                                true,
                                "HIGH",
                                "Evidence directly supports the answer"
                        ),
                        List.of(
                                new InteractionLimitation(
                                        InteractionLimitationOrigin.KNOWLEDGE,
                                        "Limitação conhecida"
                                )
                        ),
                        List.of(
                                "Contradição ainda não resolvida"
                        ),
                        new InteractionTrace(
                                "interaction-http-002",
                                "interaction-http-002-query",
                                "interaction-http-002-query",
                                List.of(
                                        new InteractionTrace.Step(
                                                "PROCESS:process-001",
                                                "PROCESS",
                                                "Processo"
                                        )
                                )
                        )
                );

        InteractionJsonResponse projected =
                mapper.toJsonResponse(response);

        assertEquals(
                "interaction-http-002",
                projected.interactionId()
        );

        assertTrue(projected.understood());

        assertEquals(
                "Consulta executada.",
                projected.answer()
        );

        assertEquals(
                1,
                projected.evidences().size()
        );

        assertEquals(
                "EVIDENCE:evidence-001",
                projected.evidences()
                        .get(0)
                        .reference()
        );

        assertEquals(
                1,
                projected.sources().size()
        );

        assertTrue(projected.confidence().evaluated());

        assertEquals(
                "HIGH",
                projected.confidence().level()
        );

        assertEquals(
                "KNOWLEDGE",
                projected.limitations()
                        .get(0)
                        .origin()
        );

        assertEquals(
                "interaction-http-002-query",
                projected.trace().queryRequestId()
        );

        assertEquals(
                "interaction-http-002-query",
                projected.trace().queryResponseRequestId()
        );
    }

    @Test
    void shouldPreserveNotUnderstoodStateWithoutQueryProjection() {

        InteractionResponse response =
                new InteractionResponse(
                        "interaction-http-003",
                        false,
                        "A solicitação não pôde ser interpretada.",
                        List.of(),
                        List.of(),
                        null,
                        List.of(
                                new InteractionLimitation(
                                        InteractionLimitationOrigin.INTERPRETATION,
                                        "Input não reconhecido"
                                )
                        ),
                        List.of(),
                        new InteractionTrace(
                                "interaction-http-003",
                                null,
                                null,
                                List.of()
                        )
                );

        InteractionJsonResponse projected =
                mapper.toJsonResponse(response);

        assertFalse(projected.understood());

        assertNull(projected.confidence());

        assertNull(
                projected.trace().queryRequestId()
        );

        assertNull(
                projected.trace().queryResponseRequestId()
        );

        assertEquals(
                "INTERPRETATION",
                projected.limitations()
                        .get(0)
                        .origin()
        );
    }
}