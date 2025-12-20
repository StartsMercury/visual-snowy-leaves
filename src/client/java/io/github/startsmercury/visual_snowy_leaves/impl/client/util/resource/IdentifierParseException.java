package io.github.startsmercury.visual_snowy_leaves.impl.client.util.resource;

import org.jetbrains.annotations.Nullable;

public class IdentifierParseException extends Exception {
    public interface Kind {
        record DuplicateSeparator(int first, int second) implements Kind {}
        record InvalidNamespaceChar(int index, char ch) implements Kind {}
        record InvalidPathChar(int index, char ch) implements Kind {}
    }

    private final String string;
    private final int beginIndex;
    private final int endIndex;
    private final Kind kind;

    protected IdentifierParseException(
        final @Nullable String message,
        final String string,
        final int beginIndex,
        final int endIndex,
        final Kind kind
    ) {
        super(message);
        this.string = string;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        this.kind = kind;
    }

    protected IdentifierParseException(
        final @Nullable String message,
        final @Nullable Throwable cause,
        final String string,
        final int beginIndex,
        final int endIndex,
        final Kind kind
    ) {
        super(message, cause);
        this.string = string;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        this.kind = kind;
    }

    public String getString() {
        return this.string;
    }

    public int getBeginIndex() {
        return this.beginIndex;
    }

    public int getEndIndex() {
        return this.endIndex;
    }

    public Kind getKind() {
        return this.kind;
    }
}
