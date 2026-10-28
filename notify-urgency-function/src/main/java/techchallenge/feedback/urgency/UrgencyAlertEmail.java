package techchallenge.feedback.urgency;

import techchallenge.feedback.core.domain.Feedback;
import techchallenge.feedback.core.notification.EmailFormats;
import techchallenge.feedback.core.notification.EmailMessage;

final class UrgencyAlertEmail {

    private static final String SUBJECT = "ALERTA CRÍTICO: avaliação de urgência alta recebida";

    private UrgencyAlertEmail() {
    }

    static EmailMessage of(Feedback feedback) {
        String body = """
                Um feedback crítico foi registrado na plataforma.

                Descrição: %s
                Urgência: %s
                Data de envio: %s
                """.formatted(
                feedback.description(),
                feedback.urgency().label(),
                EmailFormats.dateTime(feedback.sentAt()));

        return new EmailMessage(SUBJECT, body);
    }
}
