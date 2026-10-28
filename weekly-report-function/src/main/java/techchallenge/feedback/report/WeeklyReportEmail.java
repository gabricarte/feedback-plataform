package techchallenge.feedback.report;

import techchallenge.feedback.core.notification.EmailFormats;
import techchallenge.feedback.core.notification.EmailMessage;

final class WeeklyReportEmail {

    private static final String SUBJECT = "Relatório semanal de feedbacks";
    private static final String SEPARATOR = "========================================";

    private WeeklyReportEmail() {
    }

    static EmailMessage of(WeeklyReport report) {
        StringBuilder body = new StringBuilder()
                .append("RELATÓRIO SEMANAL DE FEEDBACKS\n")
                .append(SEPARATOR).append('\n')
                .append("Período: %s a %s%n".formatted(
                        EmailFormats.dateTime(report.periodStart()),
                        EmailFormats.dateTime(report.periodEnd())))
                .append("Total de avaliações: %d%n".formatted(report.totalFeedbacks()))
                .append("Média das avaliações: %s%n".formatted(EmailFormats.decimal(report.averageScore())));

        if (report.feedbacks().isEmpty()) {
            body.append("\nNenhuma avaliação foi recebida neste período.\n");
            return new EmailMessage(SUBJECT, body.toString());
        }

        body.append("\nQuantidade de avaliações por dia:\n");
        report.countByDay().forEach((day, count) ->
                body.append(" - %s: %d%n".formatted(EmailFormats.date(day), count)));

        body.append("\nQuantidade de avaliações por urgência:\n");
        report.countByUrgency().forEach((urgency, count) ->
                body.append(" - %s: %d%n".formatted(urgency.label(), count)));

        body.append("\nDetalhamento das avaliações:\n");
        report.feedbacks().forEach(feedback -> body.append(" - Data de envio: %s | Urgência: %s | Descrição: %s%n"
                .formatted(
                        EmailFormats.dateTime(feedback.sentAt()),
                        feedback.urgency().label(),
                        feedback.description())));

        return new EmailMessage(SUBJECT, body.toString());
    }
}
