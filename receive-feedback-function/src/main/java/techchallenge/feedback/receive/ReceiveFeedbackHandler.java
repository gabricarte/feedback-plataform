package techchallenge.feedback.receive;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import techchallenge.feedback.core.config.Logs;
import techchallenge.feedback.core.domain.Feedback;
import techchallenge.feedback.core.domain.InvalidFeedbackException;
import techchallenge.feedback.core.json.JsonMapper;
import techchallenge.feedback.core.repository.DynamoDbFeedbackRepository;
import techchallenge.feedback.core.repository.FeedbackRepository;

import java.time.Clock;

public class ReceiveFeedbackHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper objectMapper;
    private final FeedbackRepository feedbackRepository;
    private final UrgencyEventPublisher urgencyEventPublisher;
    private final Clock clock;
    private final ApiResponses responses;

    public ReceiveFeedbackHandler() {
        this(JsonMapper.create());
    }

    private ReceiveFeedbackHandler(ObjectMapper objectMapper) {
        this(
                objectMapper,
                DynamoDbFeedbackRepository.fromEnvironment(),
                SnsUrgencyEventPublisher.fromEnvironment(objectMapper),
                Clock.systemUTC()
        );
    }

    ReceiveFeedbackHandler(ObjectMapper objectMapper,
                           FeedbackRepository feedbackRepository,
                           UrgencyEventPublisher urgencyEventPublisher,
                           Clock clock) {
        this.objectMapper = objectMapper;
        this.feedbackRepository = feedbackRepository;
        this.urgencyEventPublisher = urgencyEventPublisher;
        this.clock = clock;
        this.responses = new ApiResponses(objectMapper);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        LambdaLogger logger = context.getLogger();

        try {
            FeedbackRequest feedbackRequest = parse(request == null ? null : request.getBody());
            Feedback feedback = Feedback.create(feedbackRequest.description(), feedbackRequest.score(), clock);

            feedbackRepository.save(feedback);
            logger.log("Feedback salvo. id=%s urgência=%s".formatted(feedback.id(), feedback.urgency()));

            if (feedback.isCritical()) {
                urgencyEventPublisher.publish(feedback);
                logger.log("Evento de urgência publicado. id=%s".formatted(feedback.id()));
            }

            return responses.created(feedback.id());
        } catch (InvalidFeedbackException exception) {
            logger.log("Feedback inválido rejeitado: " + exception.getMessage());
            return responses.badRequest(exception.getMessage());
        } catch (RuntimeException exception) {
            logger.log("Falha ao processar o feedback: " + Logs.describe(exception));
            return responses.internalError();
        }
    }

    private FeedbackRequest parse(String body) {
        if (body == null || body.isBlank()) {
            throw new InvalidFeedbackException("O corpo da requisição não pode estar vazio.");
        }
        try {
            FeedbackRequest request = objectMapper.readValue(body, FeedbackRequest.class);
            if (request == null) {
                throw new InvalidFeedbackException("O corpo da requisição deve ser um objeto JSON.");
            }
            return request;
        } catch (JsonProcessingException exception) {
            throw new InvalidFeedbackException("O corpo da requisição não é um JSON válido.", exception);
        }
    }
}
