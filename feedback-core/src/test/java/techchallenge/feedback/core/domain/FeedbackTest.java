package techchallenge.feedback.core.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import techchallenge.feedback.core.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedbackTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-28T13:45:30.987Z"), ZoneOffset.UTC);

    @ParameterizedTest
    @CsvSource({"0,HIGH", "3,HIGH", "4,MEDIUM", "6,MEDIUM", "7,LOW", "10,LOW"})
    void derivesUrgencyFromScore(int score, UrgencyLevel expected) {
        assertEquals(expected, Feedback.create("Avaliação da aula", score, CLOCK).urgency());
    }

    @Test
    void onlyHighUrgencyIsCritical() {
        assertTrue(Feedback.create("Ruim", 2, CLOCK).isCritical());
        assertFalse(Feedback.create("Razoável", 5, CLOCK).isCritical());
    }

    @Test
    void truncatesSentAtToSecondsAndStripsDescription() {
        Feedback feedback = Feedback.create("  Ótima aula  ", 9, CLOCK);

        assertEquals("2026-10-28T13:45:30Z", feedback.sentAt().toString());
        assertEquals("Ótima aula", feedback.description());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 11})
    void rejectsScoreOutOfRange(int score) {
        assertThrows(InvalidFeedbackException.class, () -> Feedback.create("Avaliação da aula", score, CLOCK));
    }

    @Test
    void rejectsMissingScoreAndBlankDescription() {
        assertThrows(InvalidFeedbackException.class, () -> Feedback.create("Avaliação da aula", null, CLOCK));
        assertThrows(InvalidFeedbackException.class, () -> Feedback.create("   ", 5, CLOCK));
        assertThrows(InvalidFeedbackException.class, () -> Feedback.create(null, 5, CLOCK));
    }

    @Test
    void roundTripsThroughJson() throws Exception {
        ObjectMapper mapper = JsonMapper.create();
        Feedback feedback = Feedback.create("Audio was broken", 1, CLOCK);

        String json = mapper.writeValueAsString(feedback);

        assertFalse(json.contains("critical"), json);
        assertTrue(json.contains("\"sentAt\":\"2026-10-28T13:45:30Z\""), json);
        assertEquals(feedback, mapper.readValue(json, Feedback.class));
    }
}
