package techchallenge.feedback.report;

import techchallenge.feedback.core.domain.Feedback;
import techchallenge.feedback.core.domain.UrgencyLevel;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public record WeeklyReport(
        Instant periodStart,
        Instant periodEnd,
        double averageScore,
        SortedMap<LocalDate, Long> countByDay,
        Map<UrgencyLevel, Long> countByUrgency,
        List<Feedback> feedbacks
) {

    public WeeklyReport {
        countByDay = Collections.unmodifiableSortedMap(new TreeMap<>(countByDay));
        countByUrgency = Collections.unmodifiableMap(new EnumMap<>(countByUrgency));
        feedbacks = List.copyOf(feedbacks);
    }

    public static WeeklyReport of(List<Feedback> feedbacks, Instant periodStart, Instant periodEnd) {
        double averageScore = feedbacks.stream()
                .mapToInt(Feedback::score)
                .average()
                .orElse(0.0);

        SortedMap<LocalDate, Long> countByDay = feedbacks.stream()
                .collect(Collectors.groupingBy(Feedback::sentDate, TreeMap::new, Collectors.counting()));

        Map<UrgencyLevel, Long> countByUrgency = new EnumMap<>(UrgencyLevel.class);
        for (UrgencyLevel level : UrgencyLevel.values()) {
            countByUrgency.put(level, 0L);
        }
        feedbacks.forEach(feedback -> countByUrgency.merge(feedback.urgency(), 1L, Long::sum));

        return new WeeklyReport(periodStart, periodEnd, averageScore, countByDay, countByUrgency, feedbacks);
    }

    public int totalFeedbacks() {
        return feedbacks.size();
    }
}
