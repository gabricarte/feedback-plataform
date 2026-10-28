package techchallenge.feedback.core.repository;

import techchallenge.feedback.core.domain.Feedback;

import java.time.Instant;
import java.util.List;

public interface FeedbackRepository {

    void save(Feedback feedback);

    List<Feedback> findSentSince(Instant since);
}
