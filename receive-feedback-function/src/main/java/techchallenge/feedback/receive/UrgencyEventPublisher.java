package techchallenge.feedback.receive;

import techchallenge.feedback.core.domain.Feedback;

public interface UrgencyEventPublisher {

    void publish(Feedback feedback);
}
