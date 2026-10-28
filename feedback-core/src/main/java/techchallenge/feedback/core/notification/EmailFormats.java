package techchallenge.feedback.core.notification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class EmailFormats {

    public static final Locale PT_BR = new Locale("pt", "BR");

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneOffset.UTC);

    private EmailFormats() {
    }

    public static String date(LocalDate date) {
        return DATE.format(date);
    }

    public static String dateTime(Instant instant) {
        return DATE_TIME.format(instant) + " UTC";
    }

    public static String decimal(double value) {
        return String.format(PT_BR, "%.2f", value);
    }
}
