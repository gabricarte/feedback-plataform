package techchallenge.feedback.core.repository;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import techchallenge.feedback.core.domain.Feedback;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FeedbackItemMapperTest {

    private final Feedback feedback = Feedback.create(
            "Ótima aula", 9, Clock.fixed(Instant.parse("2026-10-28T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void roundTripsThroughDynamoDbItem() {
        Map<String, AttributeValue> item = FeedbackItemMapper.toItem(feedback);

        assertEquals("9", item.get("score").n());
        assertEquals("LOW", item.get("urgency").s());
        assertEquals("2026-10-28T10:00:00Z", item.get("sentAt").s());
        assertEquals(feedback, FeedbackItemMapper.fromItem(item));
    }

    @Test
    void failsClearlyWhenAttributeIsMissing() {
        Map<String, AttributeValue> item = new HashMap<>(FeedbackItemMapper.toItem(feedback));
        item.remove("score");

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> FeedbackItemMapper.fromItem(item));
        assertEquals("O item do DynamoDB não possui o atributo 'score'.", exception.getMessage());
    }
}
