package techchallenge.feedback.testsupport;

import techchallenge.feedback.core.domain.Feedback;
import techchallenge.feedback.core.repository.FeedbackRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InMemoryFeedbackRepository implements FeedbackRepository {

    private final List<Feedback> feedbacks = new ArrayList<>();

    @Override
    public void save(Feedback feedback) {
        feedbacks.add(feedback);
    }

    @Override
    public List<Feedback> findSentSince(Instant since) {
        return feedbacks.stream()
                .filter(feedback -> !feedback.sentAt().isBefore(since))
                .sorted(Comparator.comparing(Feedback::sentAt))
                .toList();
    }

    public List<Feedback> all() {
        return List.copyOf(feedbacks);
    }
}
