package br.org.tertulia.interaction.api.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractionJsonCodecTest {

    private final InteractionJsonCodec codec =
            new InteractionJsonCodec();

    @Test
    void shouldReadValidJsonRequest()
            throws JsonProcessingException {

        InteractionJsonRequest request =
                codec.readRequest(
                        """
                        {
                          "input": "No processo Execução da Ação 210V, a atividade Capacitação dos agricultores aconteceu?"
                        }
                        """
                );

        assertEquals(
                "No processo Execução da Ação 210V, "
                        + "a atividade Capacitação dos agricultores aconteceu?",
                request.input()
        );
    }

    @Test
    void shouldRejectMalformedJson() {

        assertThrows(
                JsonProcessingException.class,
                () -> codec.readRequest(
                        """
                        {
                          "input":
                        """
                )
        );
    }

    @Test
    void shouldWriteStructuredJsonResponse()
            throws JsonProcessingException {

        InteractionJsonResponse response =
                new InteractionJsonResponse(
                        "interaction-http-001",
                        true,
                        "Consulta executada.",
                        List.of(
                                new InteractionJsonResponse.Evidence(
                                        "EVIDENCE:evidence-001",
                                        "Evidência",
                                        "Descrição",
                                        "DOCUMENT"
                                )
                        ),
                        List.of(
                                new InteractionJsonResponse.Source(
                                        "SOURCE:source-001",
                                        "Fonte",
                                        "ACPRM",
                                        "DOCUMENT"
                                )
                        ),
                        new InteractionJsonResponse.Confidence(
                                true,
                                "HIGH",
                                "Evidence directly supports the answer"
                        ),
                        List.of(),
                        List.of(),
                        new InteractionJsonResponse.Trace(
                                "interaction-http-001",
                                "interaction-http-001-query",
                                "interaction-http-001-query",
                                List.of(
                                        new InteractionJsonResponse.Step(
                                                "PROCESS:process-001",
                                                "PROCESS",
                                                "Processo"
                                        )
                                )
                        )
                );

        String json =
                codec.writeResponse(response);

        assertTrue(
                json.contains(
                        "\"interactionId\":\"interaction-http-001\""
                )
        );

        assertTrue(
                json.contains(
                        "\"understood\":true"
                )
        );

        assertTrue(
                json.contains(
                        "\"reference\":\"EVIDENCE:evidence-001\""
                )
        );

        assertTrue(
                json.contains(
                        "\"queryRequestId\":\"interaction-http-001-query\""
                )
        );
    }
}