package techchallenge.feedback.core.config;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Logs {

    private Logs() {
    }

    public static String describe(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
