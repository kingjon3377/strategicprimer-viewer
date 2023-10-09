package lovelace.util;

import java.util.Objects;

/**
 * A simple Pair type, primarily intended for usage where {@link org.javatuples.Pair} becomes a performance bottleneck.
 * @param <Type> the type of items in the pair.
 */
public final class SimplePair<Type> {
    private final Type first;
    private final Type second;
    private final int hash;

    private SimplePair(final Type first, final Type second) {
        this.first = first;
        this.second = second;
        hash = Objects.hash(first, second);
    }

    public Type getFirst() {
        return first;
    }

    public Type getSecond() {
        return second;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof final SimplePair sp) {
            return Objects.equals(first, sp.getFirst()) && Objects.equals(second, sp.getSecond());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public static <Type> SimplePair<Type> of(final Type first, final Type second) {
        return new SimplePair<>(first, second);
    }
}
