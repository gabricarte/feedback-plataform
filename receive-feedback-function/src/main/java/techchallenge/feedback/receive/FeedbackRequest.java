package techchallenge.feedback.receive;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FeedbackRequest(
        @JsonProperty("descricao") String description,
        @JsonProperty("nota") Integer score
) {
}
