package techchallenge.feedback.core.domain;

public enum UrgencyLevel {

    HIGH(0, 3, "ALTA"),
    MEDIUM(4, 6, "MÉDIA"),
    LOW(7, 10, "BAIXA");

    private final int minScore;
    private final int maxScore;
    private final String label;

    UrgencyLevel(int minScore, int maxScore, String label) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static UrgencyLevel fromScore(int score) {
        for (UrgencyLevel level : values()) {
            if (score >= level.minScore && score <= level.maxScore) {
                return level;
            }
        }
        throw new InvalidFeedbackException("A nota deve estar entre %d e %d."
                .formatted(Feedback.MIN_SCORE, Feedback.MAX_SCORE));
    }

    public boolean isCritical() {
        return this == HIGH;
    }
}
