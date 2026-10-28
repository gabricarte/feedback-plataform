package techchallenge.feedback.report;

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import org.junit.jupiter.api.Test;
import techchallenge.feedback.core.domain.Feedback;
import techchallenge.feedback.core.notification.EmailMessage;
import techchallenge.feedback.testsupport.InMemoryFeedbackRepository;
import techchallenge.feedback.testsupport.RecordingEmailSender;
import techchallenge.feedback.testsupport.TestContext;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateWeeklyReportHandlerTest {

    private static final Instant NOW = Instant.parse("2026-10-28T12:00:00Z");

    private final InMemoryFeedbackRepository repository = new InMemoryFeedbackRepository();
    private final RecordingEmailSender emailSender = new RecordingEmailSender();
    private final GenerateWeeklyReportHandler handler =
            new GenerateWeeklyReportHandler(repository, emailSender, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void reportsAverageAndCountsForTheLastSevenDays() {
        save("Muito antiga", 0, "2026-10-21T11:59:59Z");
        save("Áudio com problema", 2, "2026-10-21T12:00:00Z");
        save("Aula mediana", 5, "2026-10-27T09:00:00Z");
        save("Ótima aula", 10, "2026-10-27T18:00:00Z");

        handler.handleRequest(new ScheduledEvent(), new TestContext());

        EmailMessage email = emailSender.sent().get(0);
        String body = email.body();
        assertTrue(body.contains("Total de avaliações: 3"), body);
        assertTrue(body.contains("Média das avaliações: 5,67"), body);
        assertTrue(body.contains(" - 21/10/2026: 1"), body);
        assertTrue(body.contains(" - 27/10/2026: 2"), body);
        assertTrue(body.contains(" - ALTA: 1"), body);
        assertTrue(body.contains(" - MÉDIA: 1"), body);
        assertTrue(body.contains(" - BAIXA: 1"), body);
        assertTrue(body.contains("Urgência: ALTA | Descrição: Áudio com problema"), body);
        assertFalse(body.contains("Muito antiga"), body);
        assertTrue(body.indexOf("21/10/2026:") < body.indexOf("27/10/2026:"), "os dias devem estar ordenados");
    }

    @Test
    void sendsReportEvenWithoutFeedbacks() {
        handler.handleRequest(new ScheduledEvent(), new TestContext());

        String body = emailSender.sent().get(0).body();
        assertTrue(body.contains("Total de avaliações: 0"), body);
        assertTrue(body.contains("Média das avaliações: 0,00"), body);
        assertTrue(body.contains("Nenhuma avaliação foi recebida neste período."), body);
    }

    private void save(String description, int score, String sentAt) {
        repository.save(Feedback.create(description, score, Clock.fixed(Instant.parse(sentAt), ZoneOffset.UTC)));
    }
}
