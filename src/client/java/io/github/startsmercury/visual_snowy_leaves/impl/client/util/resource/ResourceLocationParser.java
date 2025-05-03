package io.github.startsmercury.visual_snowy_leaves.impl.client.util.resource;

import static net.minecraft.resources.ResourceLocation.DEFAULT_NAMESPACE;
import static net.minecraft.resources.ResourceLocation.validPathChar;

import net.minecraft.resources.ResourceLocation;

public final class ResourceLocationParser {
    private static ResourceLocation createUntrusted(
        final String namespaceSrc,
        final int beginNamespace,
        final int endNamespace,
        final String pathSrc,
        final int beginPath,
        final int endPath
    ) throws ResourceLocationParseException {
        assertValidNamespace(namespaceSrc, beginNamespace, endNamespace);
        assertValidPath(pathSrc, beginPath, endPath);
        final var namespace = namespaceSrc.substring(beginNamespace, endNamespace);
        final var path = pathSrc.substring(beginPath, endPath);
        return new ResourceLocation(namespace, path);
    }

    public static ResourceLocation fromNamespaceAndPath(final String namespace, final String path) throws ResourceLocationParseException {
        return fromNamespaceAndPath(namespace, 0, namespace.length(), path, 0, path.length());
    }

    public static ResourceLocation fromNamespaceAndPath(
        final String namespaceSrc,
        final int beginNamespace,
        final int endNamespace,
        final String pathSrc,
        final int beginPath,
        final int endPath
    ) throws ResourceLocationParseException {
        return createUntrusted(
            namespaceSrc,
            beginNamespace,
            endNamespace,
            pathSrc,
            beginPath,
            endPath
        );
    }

    public static ResourceLocation parse(final String string) throws ResourceLocationParseException {
        return bySeparator(string, ':');
    }

    public static ResourceLocation parse(
        final String string,
        final int beginIndex,
        final int endIndex
    ) throws ResourceLocationParseException {
        return bySeparator(string, ':', beginIndex, endIndex);
    }

    public static ResourceLocation withDefaultNamespace(final String string, final int beginIndex, final int endIndex) throws ResourceLocationParseException {
        assertValidPath(string, beginIndex, endIndex);
        final var path = string.substring(beginIndex, endIndex);
        return new ResourceLocation(DEFAULT_NAMESPACE, path);
    }

    public static ResourceLocation bySeparator(final String string, final char ch) throws ResourceLocationParseException {
        return bySeparator(string, ch, 0, string.length());
    }

    public static ResourceLocation bySeparator(
        final String string,
        final char ch,
        final int beginIndex,
        final int endIndex
    ) throws ResourceLocationParseException {
        final var delimiter = string.indexOf(ch, beginIndex, endIndex);
        if (delimiter < 0) {
            return withDefaultNamespace(string, beginIndex, endIndex);
        }

        if (!validPathChar(ch)) {
            final var second = string.indexOf(ch, delimiter + 1, endIndex);
            if (second >= 0) {
                throw new ResourceLocationParseException(
                    "Duplicate non [a-z0-9/._-] delimiting character",
                    string,
                    beginIndex,
                    endIndex,
                    new ResourceLocationParseException.Kind.DuplicateSeparator(delimiter, second)
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
    ) throws ResourceLocationParseException {
        for (var i = beginIndex; i < endIndex; i++) {
            final var ch = string.charAt(i);
            if (ResourceLocation.validNamespaceChar(ch)) continue;
            throw new ResourceLocationParseException(
                "Non [a-z0-9_.-] character in namespace of location",
                string,
                beginIndex,
                endIndex,
                new ResourceLocationParseException.Kind.InvalidNamespaceChar(i, ch)
            );
        }
    }

    private static void assertValidPath(
        final String string,
        final int beginIndex,
        final int endIndex
    ) throws ResourceLocationParseException {
        for (var i = beginIndex; i < endIndex; i++) {
            final var ch = string.charAt(i);
            if (ResourceLocation.validPathChar(ch)) continue;
            throw new ResourceLocationParseException(
                "Non [a-z0-9/._-] character in path of location",
                string,
                beginIndex,
                endIndex,
                new ResourceLocationParseException.Kind.InvalidPathChar(i, ch)
            );
        }
    }

    private ResourceLocationParser() {}
}
