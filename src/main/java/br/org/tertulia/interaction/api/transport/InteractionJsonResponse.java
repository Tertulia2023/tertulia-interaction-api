package br.org.tertulia.interaction.api.transport;

import java.util.List;

public record InteractionJsonResponse(
        String interactionId,
        boolean understood,
        String answer,
        List<Evidence> evidences,
        List<Source> sources,
        Confidence confidence,
        List<Limitation> limitations,
        List<String> unresolvedContradictions,
        Trace trace
) {

    public record Evidence(
            String reference,
            String title,
            String description,
            String type
    ) {
    }

    public record Source(
            String reference,
            String title,
            String origin,
            String type
    ) {
    }

    public record Confidence(
            boolean evaluated,
            String level,
            String justification
    ) {
    }

    public record Limitation(
            String origin,
            String description
    ) {
    }

    public record Trace(
            String interactionId,
            String queryRequestId,
            String queryResponseRequestId,
            List<Step> knowledgeSteps
    ) {
    }

    public record Step(
            String reference,
            String type,
            String label
    ) {
    }
}