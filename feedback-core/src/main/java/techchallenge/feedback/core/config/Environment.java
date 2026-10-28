package techchallenge.feedback.core.config;

public final class Environment {

    private Environment() {
    }

    public static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("A variável de ambiente '%s' não está configurada.".formatted(name));
        }
        return value;
    }
}
