package techchallenge.feedback.core.notification;

import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import techchallenge.feedback.core.config.Environment;

import java.nio.charset.StandardCharsets;

public class SesEmailSender implements EmailSender {

    private static final String ADMIN_EMAIL_VARIABLE = "ADMIN_EMAIL";
    private static final String SENDER_EMAIL_VARIABLE = "SENDER_EMAIL";
    private static final String CHARSET = StandardCharsets.UTF_8.name();

    private final SesClient sesClient;
    private final String senderEmail;
    private final String adminEmail;

    public SesEmailSender(SesClient sesClient, String senderEmail, String adminEmail) {
        this.sesClient = sesClient;
        this.senderEmail = senderEmail;
        this.adminEmail = adminEmail;
    }

    public static SesEmailSender fromEnvironment() {
        SesClient client = SesClient.builder()
                .httpClient(UrlConnectionHttpClient.create())
                .build();
        return new SesEmailSender(
                client,
                Environment.require(SENDER_EMAIL_VARIABLE),
                Environment.require(ADMIN_EMAIL_VARIABLE)
        );
    }

    @Override
    public void send(EmailMessage message) {
        SendEmailRequest request = SendEmailRequest.builder()
                .source(senderEmail)
                .destination(Destination.builder().toAddresses(adminEmail).build())
                .message(Message.builder()
                        .subject(utf8(message.subject()))
                        .body(Body.builder().text(utf8(message.body())).build())
                        .build())
                .build();

        sesClient.sendEmail(request);
    }

    private static Content utf8(String data) {
        return Content.builder().data(data).charset(CHARSET).build();
    }
}
