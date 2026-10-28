package techchallenge.feedback.report;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import techchallenge.feedback.core.domain.Feedback;
import techchallenge.feedback.core.notification.EmailSender;
import techchallenge.feedback.core.notification.SesEmailSender;
import techchallenge.feedback.core.repository.DynamoDbFeedbackRepository;
import techchallenge.feedback.core.repository.FeedbackRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class GenerateWeeklyReportHandler implements RequestHandler<ScheduledEvent, Void> {

    private static final Duration REPORT_PERIOD = Duration.ofDays(7);

    private final FeedbackRepository feedbackRepository;
    private final EmailSender emailSender;
    private final Clock clock;

    public GenerateWeeklyReportHandler() {
        this(DynamoDbFeedbackRepository.fromEnvironment(), SesEmailSender.fromEnvironment(), Clock.systemUTC());
    }

    GenerateWeeklyReportHandler(FeedbackRepository feedbackRepository, EmailSender emailSender, Clock clock) {
        this.feedbackRepository = feedbackRepository;
        this.emailSender = emailSender;
        this.clock = clock;
    }

    @Override
    public Void handleRequest(ScheduledEvent event, Context context) {
        LambdaLogger logger = context.getLogger();

        Instant periodEnd = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        Instant periodStart = periodEnd.minus(REPORT_PERIOD);
        logger.log("Gerando relatório semanal. inicio=%s fim=%s".formatted(periodStart, periodEnd));

        List<Feedback> feedbacks = feedbackRepository.findSentSince(periodStart);
        WeeklyReport report = WeeklyReport.of(feedbacks, periodStart, periodEnd);
        emailSender.send(WeeklyReportEmail.of(report));

        logger.log("Relatório semanal enviado. totalAvaliacoes=%d media=%.2f"
                .formatted(report.totalFeedbacks(), report.averageScore()));
        return null;
    }
}
