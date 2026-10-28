package techchallenge.feedback.core.repository;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import techchallenge.feedback.core.domain.Feedback;
import techchallenge.feedback.core.domain.UrgencyLevel;

import java.time.Instant;
import java.util.Map;

final class FeedbackItemMapper {

    static final String ID = "id";
    static final String DESCRIPTION = "description";
    static final String SCORE = "score";
    static final String URGENCY = "urgency";
    static final String SENT_AT = "sentAt";

    private FeedbackItemMapper() {
    }

    static Map<String, AttributeValue> toItem(Feedback feedback) {
        return Map.of(
                ID, AttributeValue.fromS(feedback.id()),
                DESCRIPTION, AttributeValue.fromS(feedback.description()),
                SCORE, AttributeValue.fromN(Integer.toString(feedback.score())),
                URGENCY, AttributeValue.fromS(feedback.urgency().name()),
                SENT_AT, AttributeValue.fromS(feedback.sentAt().toString())
        );
    }

    static Feedback fromItem(Map<String, AttributeValue> item) {
        return new Feedback(
                requiredAttribute(item, ID).s(),
                requiredAttribute(item, DESCRIPTION).s(),
                Integer.parseInt(requiredAttribute(item, SCORE).n()),
                UrgencyLevel.valueOf(requiredAttribute(item, URGENCY).s()),
                Instant.parse(requiredAttribute(item, SENT_AT).s())
        );
    }

    private static AttributeValue requiredAttribute(Map<String, AttributeValue> item, String name) {
        AttributeValue value = item.get(name);
        if (value == null) {
            throw new IllegalStateException("O item do DynamoDB não possui o atributo '%s'.".formatted(name));
        }
        return value;
    }
}
