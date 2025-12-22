package io.github.startsmercury.visual_snowy_leaves.impl.client.resources.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public final class SequencedCompletableFuture {
    public static <T> CompletableFuture<List<T>> tryFilter(
        final Collection<? extends CompletableFuture<? extends T>> actions,
        final Consumer<? super Throwable> catcher
    ) {
        return tryFilter(actions, ArrayList::new, catcher);
    }

    public static <C extends Collection<T>, T> CompletableFuture<C> tryFilter(
        final Collection<? extends CompletableFuture<? extends T>> actions,
        final IntFunction<C> collectionProvider,
        final Consumer<? super Throwable> catcher
    ) {
        final C collection;
        final CompletableFuture<?>[] buffer;

        {
            final var n = actions.size();
            collection = collectionProvider.apply(n);
            buffer = new CompletableFuture[n];
        }

        var idx = 0;
        for (final var action : actions) {
            buffer[idx++] = action.handle((result, throwable) -> {
                if (throwable != null) {
                    catcher.accept(throwable);
                } else {
                    collection.add(result);
                }

                return (Void) null;
            });
        }

        return CompletableFuture.allOf(buffer).thenApply(x -> collection);
    }

    private SequencedCompletableFuture() {}
}
