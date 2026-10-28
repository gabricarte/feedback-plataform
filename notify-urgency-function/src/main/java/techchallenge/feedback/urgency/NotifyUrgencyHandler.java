package techchallenge.feedback.urgency;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import techchallenge.feedback.core.domain.Feedback;
import techchallenge.feedback.core.json.JsonMapper;
import techchallenge.feedback.core.notification.EmailSender;
import techchallenge.feedback.core.notification.SesEmailSender;

public class NotifyUrgencyHandler implements RequestHandler<SNSEvent, Void> {

    private final ObjectMapper objectMapper;
    private final EmailSender emailSender;

    public NotifyUrgencyHandler() {
        this(JsonMapper.create(), SesEmailSender.fromEnvironment());
    }

    NotifyUrgencyHandler(ObjectMapper objectMapper, EmailSender emailSender) {
        this.objectMapper = objectMapper;
        this.emailSender = emailSender;
    }

    @Override
    public Void handleRequest(SNSEvent event, Context context) {
        LambdaLogger logger = context.getLogger();

        if (event == null || event.getRecords() == null || event.getRecords().isEmpty()) {
            logger.log("O evento SNS não possui registros. Nada a notificar.");
            return null;
        }

        for (SNSEvent.SNSRecord snsRecord : event.getRecords()) {
            Feedback feedback = parse(snsRecord);
            emailSender.send(UrgencyAlertEmail.of(feedback));
            logger.log("Alerta de urgência enviado. feedbackId=%s".formatted(feedback.id()));
        }

        return null;
    }

    private Feedback parse(SNSEvent.SNSRecord snsRecord) {
        String messageId = snsRecord.getSNS().getMessageId();
        try {
            return objectMapper.readValue(snsRecord.getSNS().getMessage(), Feedback.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                    "A mensagem SNS %s não contém um feedback válido.".formatted(messageId), exception);
        }
    }
}
