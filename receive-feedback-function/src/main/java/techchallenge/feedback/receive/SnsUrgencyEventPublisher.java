package techchallenge.feedback.receive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import techchallenge.feedback.core.config.Environment;
import techchallenge.feedback.core.domain.Feedback;

public class SnsUrgencyEventPublisher implements UrgencyEventPublisher {

    private static final String TOPIC_ARN_VARIABLE = "URGENCY_TOPIC_ARN";

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final String topicArn;

    public SnsUrgencyEventPublisher(SnsClient snsClient, ObjectMapper objectMapper, String topicArn) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.topicArn = topicArn;
    }

    public static SnsUrgencyEventPublisher fromEnvironment(ObjectMapper objectMapper) {
        SnsClient client = SnsClient.builder()
                .httpClient(UrlConnectionHttpClient.create())
                .build();
        return new SnsUrgencyEventPublisher(client, objectMapper, Environment.require(TOPIC_ARN_VARIABLE));
    }

    @Override
    public void publish(Feedback feedback) {
        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .subject("Feedback crítico recebido")
                .message(toJson(feedback))
                .build();

        snsClient.publish(request);
    }

    private String toJson(Feedback feedback) {
        try {
            return objectMapper.writeValueAsString(feedback);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar o feedback %s para JSON.".formatted(feedback.id()), exception);
        }
    }
}
