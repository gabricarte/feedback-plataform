package techchallenge.feedback.testsupport;

import techchallenge.feedback.core.notification.EmailMessage;
import techchallenge.feedback.core.notification.EmailSender;

import java.util.ArrayList;
import java.util.List;

public class RecordingEmailSender implements EmailSender {

    private final List<EmailMessage> sent = new ArrayList<>();

    @Override
    public void send(EmailMessage message) {
        sent.add(message);
    }

    public List<EmailMessage> sent() {
        return sent;
    }
}
