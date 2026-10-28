package techchallenge.feedback.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

public record Feedback(
        String id,
        String description,
        int score,
        UrgencyLevel urgency,
        Instant sentAt
) {

    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 10;
    public static final int MAX_DESCRIPTION_LENGTH = 2_000;

    public Feedback {
        requireNotBlank(id, "O id do feedback não pode estar vazio.");
        requireNotBlank(description, "A descrição não pode estar vazia.");
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidFeedbackException(
                    "A descrição deve ter no máximo %d caracteres.".formatted(MAX_DESCRIPTION_LENGTH));
        }
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new InvalidFeedbackException(
                    "A nota deve estar entre %d e %d.".formatted(MIN_SCORE, MAX_SCORE));
        }
        Objects.requireNonNull(urgency, "A urgência não pode ser nula.");
        Objects.requireNonNull(sentAt, "A data de envio não pode ser nula.");

        description = description.strip();
        sentAt = sentAt.truncatedTo(ChronoUnit.SECONDS);
    }

    public static Feedback create(String description, Integer score, Clock clock) {
        if (score == null) {
            throw new InvalidFeedbackException("A nota é obrigatória.");
        }
        return new Feedback(
                UUID.randomUUID().toString(),
                description,
                score,
                UrgencyLevel.fromScore(score),
                clock.instant()
        );
    }

    @JsonIgnore
    public boolean isCritical() {
        return urgency.isCritical();
    }

    @JsonIgnore
    public LocalDate sentDate() {
        return LocalDate.ofInstant(sentAt, ZoneOffset.UTC);
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidFeedbackException(message);
        }
    }
}
