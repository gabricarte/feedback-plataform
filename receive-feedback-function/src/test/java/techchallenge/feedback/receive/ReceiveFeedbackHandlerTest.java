package techchallenge.feedback.receive;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import techchallenge.feedback.core.domain.Feedback;
import techchallenge.feedback.core.json.JsonMapper;
import techchallenge.feedback.testsupport.InMemoryFeedbackRepository;
import techchallenge.feedback.testsupport.TestContext;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiveFeedbackHandlerTest {

    private final InMemoryFeedbackRepository repository = new InMemoryFeedbackRepository();
    private final List<Feedback> publishedEvents = new ArrayList<>();
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-28T10:00:00Z"), ZoneOffset.UTC);
    private final ReceiveFeedbackHandler handler =
            new ReceiveFeedbackHandler(JsonMapper.create(), repository, publishedEvents::add, clock);

    @Test
    void storesFeedbackAndReturnsCreated() {
        APIGatewayProxyResponseEvent response = post("{\"descricao\": \"Ótima aula\", \"nota\": 9}");

        assertEquals(201, response.getStatusCode());
        assertEquals(1, repository.all().size());
        assertTrue(response.getBody().contains(repository.all().get(0).id()));
        assertTrue(publishedEvents.isEmpty());
    }

    @Test
    void publishesUrgencyEventForCriticalFeedback() {
        APIGatewayProxyResponseEvent response = post("{\"descricao\": \"Sem áudio na aula\", \"nota\": 1}");

        assertEquals(201, response.getStatusCode());
        assertEquals(1, publishedEvents.size());
        assertEquals(repository.all().get(0), publishedEvents.get(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "not json",
            "[]",
            "null",
            "{\"descricao\": \"Ótima aula\"}",
            "{\"nota\": 5}",
            "{\"descricao\": \"Ótima aula\", \"nota\": 11}",
            "{\"descricao\": \"Ótima aula\", \"nota\": -1}",
            "{\"descricao\": \"Ótima aula\", \"nota\": 7.5}"
    })
    void rejectsInvalidRequests(String body) {
        APIGatewayProxyResponseEvent response = post(body);

        assertEquals(400, response.getStatusCode(), response.getBody());
        assertTrue(repository.all().isEmpty());
    }

    @Test
    void returnsInternalErrorWhenStorageFails() {
        ReceiveFeedbackHandler failingHandler = new ReceiveFeedbackHandler(
                JsonMapper.create(),
                new InMemoryFeedbackRepository() {
                    @Override
                    public void save(Feedback feedback) {
                        throw new IllegalStateException("DynamoDB indisponível");
                    }
                },
                publishedEvents::add,
                clock);

        APIGatewayProxyResponseEvent response = failingHandler.handleRequest(
                new APIGatewayProxyRequestEvent().withBody("{\"descricao\": \"Bad\", \"nota\": 1}"), new TestContext());

        assertEquals(500, response.getStatusCode());
        assertEquals("{\"error\":\"Erro interno no servidor.\"}", response.getBody());
        assertTrue(publishedEvents.isEmpty());
    }

    private APIGatewayProxyResponseEvent post(String body) {
        return handler.handleRequest(new APIGatewayProxyRequestEvent().withBody(body), new TestContext());
    }
}
