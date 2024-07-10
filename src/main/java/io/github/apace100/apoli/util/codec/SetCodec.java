package io.github.apace100.apoli.util.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 *  A {@link Set}-backed version of {@link com.mojang.serialization.codecs.ListCodec}
 */
public record SetCodec<E>(Codec<E> elementCodec, int minSize, int maxSize) implements Codec<Set<E>> {

    @Override
    public <T> DataResult<T> encode(Set<E> input, DynamicOps<T> ops, T prefix) {

        ListBuilder<T> listBuilder = ops.listBuilder();
        int size = input.size();

        if (size < minSize) {
            return createTooShortError(size);
        }

        else if (size > maxSize) {
            return createTooLongError(size);
        }

        for (E element : input) {
            listBuilder.add(elementCodec.encodeStart(ops, element));
        }

        return listBuilder.build(prefix);

    }

    @Override
    public <T> DataResult<Pair<Set<E>, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(stream -> {

            DecoderState<T> decoder = new DecoderState<>(ops);
            stream.accept(decoder::accept);

            return decoder.build();

        });
    }

    @Override
    public String toString() {
        return "SetCodec[" + elementCodec + "]";
    }

    private <R> DataResult<R> createTooShortError(int size) {
        return DataResult.error(() -> "Set has too few elements; expected set to have " + minSize + " to " + maxSize + " element(s), but has " + size + " element(s)");
    }

    private <R> DataResult<R> createTooLongError(int size) {
        return DataResult.error(() -> "Set has too much elements; expected set to have " + minSize + " to " + maxSize + " element(s), but has " + size + " element(s)");
    }

    public static <T> SetCodec<T> of(Codec<T> codec) {
        return new SetCodec<>(codec, 0, Integer.MAX_VALUE);
    }

    private class DecoderState<T> {

        private final Stream.Builder<T> failed;

        private final DynamicOps<T> ops;
        private final List<E> elements;

        private DataResult<Unit> result = DataResult.success(Unit.INSTANCE, Lifecycle.stable());
        private int totalCount;

        private DecoderState(DynamicOps<T> ops) {
            this.failed = Stream.builder();
            this.ops = ops;
            this.elements = new ArrayList<>();
        }

        public void accept(T value) {

            totalCount++;
            if (elements.size() >= maxSize) {
                failed.add(value);
            }

            else {

                DataResult<Pair<E, T>> elementResult = elementCodec.decode(ops, value);

                elementResult.error().ifPresent(error -> failed.add(value));
                elementResult.resultOrPartial().ifPresent(pair -> elements.add(pair.getFirst()));

                result = result.apply2stable((unit, prev) -> unit, elementResult);

            }

        }

        public DataResult<Pair<Set<E>, T>> build() {

            int size = elements.size();

            if (size < minSize) {
                return createTooShortError(size);
            }

            else {

                T errors = ops.createList(failed.build());
                Pair<Set<E>, T> pair = Pair.of(Set.copyOf(elements), errors);

                if (totalCount > maxSize) {
                    result = createTooLongError(totalCount);
                }

                return result.map(ignored -> pair).setPartial(pair);

            }

        }

    }

}
