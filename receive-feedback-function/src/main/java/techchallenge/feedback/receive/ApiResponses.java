package techchallenge.feedback.receive;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

final class ApiResponses {

    private static final Map<String, String> HEADERS = Map.of("Content-Type", "application/json");

    private final ObjectMapper objectMapper;

    ApiResponses(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    APIGatewayProxyResponseEvent created(String id) {
        return json(201, Map.of("id", id, "message", "Feedback registrado com sucesso."));
    }

    APIGatewayProxyResponseEvent badRequest(String error) {
        return json(400, Map.of("error", error));
    }

    APIGatewayProxyResponseEvent internalError() {
        return json(500, Map.of("error", "Erro interno no servidor."));
    }

    private APIGatewayProxyResponseEvent json(int statusCode, Map<String, String> body) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(HEADERS)
                    .withBody(objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar o corpo da resposta.", exception);
        }
    }
}
