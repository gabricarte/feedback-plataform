package techchallenge.feedback.urgency;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import techchallenge.feedback.core.domain.Feedback;
import techchallenge.feedback.core.json.JsonMapper;
import techchallenge.feedback.core.notification.EmailMessage;
import techchallenge.feedback.testsupport.RecordingEmailSender;
import techchallenge.feedback.testsupport.TestContext;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotifyUrgencyHandlerTest {

    private final ObjectMapper mapper = JsonMapper.create();
    private final RecordingEmailSender emailSender = new RecordingEmailSender();
    private final NotifyUrgencyHandler handler = new NotifyUrgencyHandler(mapper, emailSender);

    @Test
    void emailsDescriptionUrgencyAndSentDate() throws Exception {
        Feedback feedback = Feedback.create(
                "Sem áudio na aula", 1, Clock.fixed(Instant.parse("2026-10-28T10:00:00Z"), ZoneOffset.UTC));

        handler.handleRequest(eventWith(mapper.writeValueAsString(feedback)), new TestContext());

        assertEquals(1, emailSender.sent().size());
        EmailMessage email = emailSender.sent().get(0);
        assertTrue(email.body().contains("Descrição: Sem áudio na aula"));
        assertTrue(email.body().contains("Urgência: ALTA"));
        assertTrue(email.body().contains("Data de envio: 28/10/2026 10:00:00 UTC"));
    }

    @Test
    void ignoresEmptyEvent() {
        handler.handleRequest(new SNSEvent(), new TestContext());

        assertTrue(emailSender.sent().isEmpty());
    }

    @Test
    void failsOnInvalidMessageSoLambdaRetries() {
        assertThrows(IllegalArgumentException.class,
                () -> handler.handleRequest(eventWith("not json"), new TestContext()));
    }

    private static SNSEvent eventWith(String message) {
        SNSEvent.SNS sns = new SNSEvent.SNS().withMessageId("message-1").withMessage(message);
        return new SNSEvent().withRecords(List.of(new SNSEvent.SNSRecord().withSns(sns)));
    }
}
