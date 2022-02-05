package impl.dbio;

import java.util.Objects;

import java.util.function.Consumer;
import buckelieg.jdbc.fn.TryConsumer;

/**
 * @see buckelieg.jdbc.fn.TryConsumer
 *
 * @author Jonathan Lovelace (skeleton, {@code accept}, <code>partial</code>)
 * @author rest adapted from @buckelieg/jdbc-fn TryConsumer
 */
@FunctionalInterface
interface TryBiConsumer<T1, T2, E extends Throwable> {
	void accept(T1 t1, T2 t2) throws E;

	static <T1, T2, E extends Throwable> TryBiConsumer<T1, T2, E> of(
			final TryBiConsumer<T1, T2, E> tryConsumer) {
		return Objects.requireNonNull(tryConsumer);
	}

	default TryBiConsumer<T1, T2, E> andThen(final TryBiConsumer<? super T1, ? super T2, E> after) throws E {
		Objects.requireNonNull(after);
		return (T1 t1, T2 t2) -> {
			accept(t1, t2);
			after.accept(t1, t2);
		};
	}

	default TryBiConsumer<T1, T2, E> compose(final TryBiConsumer<? super T1, ? super T2, E> before) throws E {
		Objects.requireNonNull(before);
		return (T1 t1, T2 t2) -> {
			before.accept(t1, t2);
			accept(t1, t2);
		};
	}

	default TryConsumer<T1, E> partial(final T2 t2) throws E {
		return (T1 t1) -> accept(t1, t2);
	}

	default Consumer<T1> wrappedPartial(final T2 t2) {
		return (T1 t1) -> {
			try {
				accept(t1, t2);
			} catch (final RuntimeException except) {
				// don't wrap unchecked exceptions
				throw except;
			} catch (final Throwable except) {
				throw new RuntimeException(except);
			}
		};
	}
}
