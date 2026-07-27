package br.org.tertulia.interaction.api;

import br.org.tertulia.interaction.api.transport.InteractionJsonCodec;
import br.org.tertulia.interaction.api.transport.InteractionTransportMapper;
import br.org.tertulia.interaction.contract.InteractionConfidence;
import br.org.tertulia.interaction.contract.InteractionRequest;
import br.org.tertulia.interaction.contract.InteractionResponse;
import br.org.tertulia.interaction.contract.InteractionTrace;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractionRequestProcessorTest {

    @Test
    void shouldGenerateInteractionIdAndExecuteInteraction()
            throws JsonProcessingException {

        AtomicReference<InteractionRequest> captured =
                new AtomicReference<>();

        InteractionRequestProcessor processor =
                new InteractionRequestProcessor(
                        new InteractionJsonCodec(),
                        new InteractionTransportMapper(),
                        request -> {
                            captured.set(request);

                            return understoodResponse(
                                    request.getId()
                            );
                        },
                        () -> "interaction-generated-001"
                );

        String responseJson =
                processor.process(
                        """
                        {
                          "input": "No processo Execução da Ação 210V, a atividade Capacitação dos agricultores aconteceu?"
                        }
                        """
                );

        assertEquals(
                "interaction-generated-001",
                captured.get().getId()
        );

        assertTrue(
                captured.get()
                        .getInput()
                        .contains("Capacitação dos agricultores")
        );

        assertTrue(
                responseJson.contains(
                        "\"interactionId\":\"interaction-generated-001\""
                )
        );

        assertTrue(
                responseJson.contains(
                        "\"understood\":true"
                )
        );
    }

    @Test
    void shouldNotExecuteInteractionForMalformedJson() {

        AtomicBoolean executed =
                new AtomicBoolean(false);

        InteractionRequestProcessor processor =
                new InteractionRequestProcessor(
                        new InteractionJsonCodec(),
                        new InteractionTransportMapper(),
                        request -> {
                            executed.set(true);

                            return understoodResponse(
                                    request.getId()
                            );
                        },
                        () -> "interaction-generated-002"
                );

        assertThrows(
                JsonProcessingException.class,
                () -> processor.process(
                        """
                        {
                          "input":
                        """
                )
        );

        assertFalse(executed.get());
    }

    @Test
    void shouldPreserveNotUnderstoodResponse()
            throws JsonProcessingException {

        InteractionRequestProcessor processor =
                new InteractionRequestProcessor(
                        new InteractionJsonCodec(),
                        new InteractionTransportMapper(),
                        request ->
                                new InteractionResponse(
                                        request.getId(),
                                        false,
                                        "A solicitação não pôde ser interpretada.",
                                        List.of(),
                                        List.of(),
                                        null,
                                        List.of(
                                                new br.org.tertulia.interaction.contract.InteractionLimitation(
                                                        br.org.tertulia.interaction.contract.InteractionLimitationOrigin.INTERPRETATION,
                                                        "Input não reconhecido"
                                                )
                                        ),
                                        List.of(),
                                        new InteractionTrace(
                                                request.getId(),
                                                null,
                                                null,
                                                List.of()
                                        )
                                ),
                        () -> "interaction-generated-003"
                );

        String responseJson =
                processor.process(
                        """
                        {
                          "input": "pergunta ainda não suportada"
                        }
                        """
                );

        assertTrue(
                responseJson.contains(
                        "\"interactionId\":\"interaction-generated-003\""
                )
        );

        assertTrue(
                responseJson.contains(
                        "\"understood\":false"
                )
        );

        assertTrue(
                responseJson.contains(
                        "\"confidence\":null"
                )
        );
    }

    private static InteractionResponse understoodResponse(
            String interactionId
    ) {
        return new InteractionResponse(
                interactionId,
                true,
                "Consulta executada.",
                List.of(),
                List.of(),
                new InteractionConfidence(
                        false,
                        null,
                        null
                ),
                List.of(),
                List.of(),
                new InteractionTrace(
                        interactionId,
                        interactionId + "-query",
                        interactionId + "-query",
                        List.of()
                )
        );
    }
}