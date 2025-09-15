package simplexity.simplewarps.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum LocaleMessage {
    ;
    private final String path;
    private String message;

    LocaleMessage(String path, String message) {
        this.path = path;
        this.message = message;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public String getMessage() {
        if (message == null) return "";
        return message;
    }

    public void setMessage(@Nullable String message) {
        if (message == null) message = "";
        this.message = message;
    }
}
