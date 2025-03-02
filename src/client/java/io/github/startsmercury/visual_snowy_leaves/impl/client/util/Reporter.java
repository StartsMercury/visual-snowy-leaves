package io.github.startsmercury.visual_snowy_leaves.impl.client.util;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class Reporter<E> implements Iterable<E> {
    private static final BiConsumer<PrintWriter, Object> DEFAULT_FORMATTER = PrintWriter::println;

    private final Set<E> reported;

    private final Set<E> reportedView;

    private final BiConsumer<? super PrintWriter, ? super E> formatter;

    private boolean changed;

    public Reporter(final Set<E> reported) {
        this.reported = Objects.requireNonNull(reported, "Parameter reported is null");
        this.reportedView = Collections.unmodifiableSet(this.reported);
        this.formatter = DEFAULT_FORMATTER;
    }

    public Reporter(
        final Set<E> reported,
        final Function<? super E, String> formatter
    ) {
        this.reported = Objects.requireNonNull(reported, "Parameter reported is null");
        Objects.requireNonNull(formatter, "Parameter formatter is null");
        this.formatter = (writer, value) -> writer.println(formatter.apply(value));
        this.reportedView = Collections.unmodifiableSet(this.reported);
    }

    public boolean report(final E value) {
        return this.changed |= this.reported.add(value);
    }

    public boolean isReported(final Object value) {
        return this.reported.contains(value);
    }

    public boolean isChanged() {
        return this.changed;
    }

    public void setUnchanged() {
        this.changed = false;
    }

    public boolean consumeChanged() {
        final var changed = this.changed;
        this.changed = false;
        return changed;
    }

    public Set<? extends E> viewAsSet() {
        return this.reportedView;
    }

    public int size() {
        return this.reportedView.size();
    }

    public boolean isEmpty() {
        return this.reportedView.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return this.reportedView.iterator();
    }

    public boolean collectReport(final PrintWriter writer, final String header) {
        final var iterator = this.iterator();

        if (!iterator.hasNext()) {
            return false;
        }

        writer.println(header);
        iterator.forEachRemaining((final var value) -> {
            writer.print(" - ");
            this.formatter.accept(writer, value);
        });
        writer.println();

        return true;
    }
}
