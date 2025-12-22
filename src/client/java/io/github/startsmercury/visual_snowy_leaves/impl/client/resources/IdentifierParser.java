package io.github.startsmercury.visual_snowy_leaves.impl.client.resources;

import static net.minecraft.resources.Identifier.DEFAULT_NAMESPACE;
import static net.minecraft.resources.Identifier.validPathChar;

import net.minecraft.resources.Identifier;

public final class IdentifierParser {
    private static Identifier createUntrusted(
        final String namespaceSrc,
        final int beginNamespace,
        final int endNamespace,
        final String pathSrc,
        final int beginPath,
        final int endPath
    ) throws IdentifierParseException {
        assertValidNamespace(namespaceSrc, beginNamespace, endNamespace);
        assertValidPath(pathSrc, beginPath, endPath);
        final var namespace = namespaceSrc.substring(beginNamespace, endNamespace);
        final var path = pathSrc.substring(beginPath, endPath);
        return new Identifier(namespace, path);
    }

    public static Identifier fromNamespaceAndPath(final String namespace, final String path) throws IdentifierParseException {
        return fromNamespaceAndPath(namespace, 0, namespace.length(), path, 0, path.length());
    }

    public static Identifier fromNamespaceAndPath(
        final String namespaceSrc,
        final int beginNamespace,
        final int endNamespace,
        final String pathSrc,
        final int beginPath,
        final int endPath
    ) throws IdentifierParseException {
        return createUntrusted(
            namespaceSrc,
            beginNamespace,
            endNamespace,
            pathSrc,
            beginPath,
            endPath
        );
    }

    public static Identifier parse(final String string) throws IdentifierParseException {
        return bySeparator(string, ':');
    }

    public static Identifier parse(
        final String string,
        final int beginIndex,
        final int endIndex
    ) throws IdentifierParseException {
        return bySeparator(string, ':', beginIndex, endIndex);
    }

    public static Identifier withDefaultNamespace(final String string, final int beginIndex, final int endIndex) throws IdentifierParseException {
        assertValidPath(string, beginIndex, endIndex);
        final var path = string.substring(beginIndex, endIndex);
        return new Identifier(DEFAULT_NAMESPACE, path);
    }

    public static Identifier bySeparator(final String string, final char ch) throws IdentifierParseException {
        return bySeparator(string, ch, 0, string.length());
    }

    public static Identifier bySeparator(
        final String string,
        final char ch,
        final int beginIndex,
        final int endIndex
    ) throws IdentifierParseException {
        final var delimiter = string.indexOf(ch, beginIndex, endIndex);
        if (delimiter < 0) {
            return withDefaultNamespace(string, beginIndex, endIndex);
        }

        if (!validPathChar(ch)) {
            final var second = string.indexOf(ch, delimiter + 1, endIndex);
            if (second >= 0) {
                throw new IdentifierParseException(
                    "Duplicate non [a-z0-9/._-] delimiting character",
                    string,
                    beginIndex,
                    endIndex,
                    new IdentifierParseException.Kind.DuplicateSeparator(delimiter, second)
                );
            }
        }

        if (delimiter == 0) {
            return withDefaultNamespace(string, delimiter + 1, endIndex);
        }

        return createUntrusted(string, 0, delimiter, string, delimiter + 1, endIndex);
    }

    private static void assertValidNamespace(
        final String string,
        final int beginIndex,
        final int endIndex
    ) throws IdentifierParseException {
        for (var i = beginIndex; i < endIndex; i++) {
            final var ch = string.charAt(i);
            if (Identifier.validNamespaceChar(ch)) continue;
            throw new IdentifierParseException(
                "Non [a-z0-9_.-] character in namespace of location",
                string,
                beginIndex,
                endIndex,
                new IdentifierParseException.Kind.InvalidNamespaceChar(i, ch)
            );
        }
    }

    private static void assertValidPath(
        final String string,
        final int beginIndex,
        final int endIndex
    ) throws IdentifierParseException {
        for (var i = beginIndex; i < endIndex; i++) {
            final var ch = string.charAt(i);
            if (Identifier.validPathChar(ch)) continue;
            throw new IdentifierParseException(
                "Non [a-z0-9/._-] character in path of location",
                string,
                beginIndex,
                endIndex,
                new IdentifierParseException.Kind.InvalidPathChar(i, ch)
            );
        }
    }

    private IdentifierParser() {}
}
