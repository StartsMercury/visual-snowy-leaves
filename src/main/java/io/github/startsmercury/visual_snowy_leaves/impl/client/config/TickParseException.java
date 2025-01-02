package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

public class TickParseException extends RuntimeException {
    private final String input;

    private final int index;

    public TickParseException(final String input, final String reason, final int index) {
        super(reason);
        if ((input == null) || (reason == null))
            throw new NullPointerException();
        if (index < -1)
            throw new IllegalArgumentException();
        this.input = input;
        this.index = index;
    }

    public TickParseException(final String input, final String reason) {
        this(input, reason, -1);
    }

    public String getInput() {
        return this.input;
    }

    public String getReason() {
        return super.getMessage();
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public String getMessage() {
        final var builder = new StringBuilder();

        builder.append(this.getReason());

        if (this.index > -1) {
            builder.append(" at index ");
            builder.append(this.index);
        }

        builder.append(": ");
        builder.append(this.input);

        return builder.toString();
    }
}
