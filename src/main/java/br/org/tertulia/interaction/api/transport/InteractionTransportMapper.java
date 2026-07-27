package br.org.tertulia.interaction.api.transport;

import br.org.tertulia.interaction.contract.InteractionConfidence;
import br.org.tertulia.interaction.contract.InteractionEvidence;
import br.org.tertulia.interaction.contract.InteractionLimitation;
import br.org.tertulia.interaction.contract.InteractionRequest;
import br.org.tertulia.interaction.contract.InteractionResponse;
import br.org.tertulia.interaction.contract.InteractionSource;
import br.org.tertulia.interaction.contract.InteractionTrace;

import java.util.Objects;

public final class InteractionTransportMapper {

    public InteractionRequest toInteractionRequest(
            String interactionId,
            InteractionJsonRequest request
    ) {
        Objects.requireNonNull(
                request,
                "request must not be null"
        );

        return new InteractionRequest(
                interactionId,
                request.input()
        );
    }

    public InteractionJsonResponse toJsonResponse(
            InteractionResponse response
    ) {
        Objects.requireNonNull(
                response,
                "response must not be null"
        );

        return new InteractionJsonResponse(
                response.getInteractionId(),
                response.isUnderstood(),
                response.getAnswer(),
                response.getEvidences()
                        .stream()
                        .map(this::projectEvidence)
                        .toList(),
                response.getSources()
                        .stream()
                        .map(this::projectSource)
                        .toList(),
                response.getConfidence()
                        .map(this::projectConfidence)
                        .orElse(null),
                response.getLimitations()
                        .stream()
                        .map(this::projectLimitation)
                        .toList(),
                response.getUnresolvedContradictions(),
                projectTrace(response.getTrace())
        );
    }

    private InteractionJsonResponse.Evidence projectEvidence(
            InteractionEvidence evidence
    ) {
        return new InteractionJsonResponse.Evidence(
                evidence.getReference(),
                evidence.getTitle(),
                evidence.getDescription(),
                evidence.getType()
        );
    }

    private InteractionJsonResponse.Source projectSource(
            InteractionSource source
    ) {
        return new InteractionJsonResponse.Source(
                source.getReference(),
                source.getTitle(),
                source.getOrigin(),
                source.getType()
        );
    }

    private InteractionJsonResponse.Confidence projectConfidence(
            InteractionConfidence confidence
    ) {
        return new InteractionJsonResponse.Confidence(
                confidence.isEvaluated(),
                confidence.getLevel().orElse(null),
                confidence.getJustification().orElse(null)
        );
    }

    private InteractionJsonResponse.Limitation projectLimitation(
            InteractionLimitation limitation
    ) {
        return new InteractionJsonResponse.Limitation(
                limitation.getOrigin().name(),
                limitation.getDescription()
        );
    }

    private InteractionJsonResponse.Trace projectTrace(
            InteractionTrace trace
    ) {
        return new InteractionJsonResponse.Trace(
                trace.getInteractionId(),
                trace.getQueryRequestId().orElse(null),
                trace.getQueryResponseRequestId().orElse(null),
                trace.getKnowledgeSteps()
                        .stream()
                        .map(this::projectStep)
                        .toList()
        );
    }

    private InteractionJsonResponse.Step projectStep(
            InteractionTrace.Step step
    ) {
        return new InteractionJsonResponse.Step(
                step.getReference(),
                step.getType(),
                step.getLabel()
        );
    }
}