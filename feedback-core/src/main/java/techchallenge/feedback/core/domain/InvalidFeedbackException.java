package techchallenge.feedback.core.domain;

public class InvalidFeedbackException extends RuntimeException {

    public InvalidFeedbackException(String message) {
        super(message);
    }

    public InvalidFeedbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
