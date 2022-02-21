package either;

import java.util.Optional;
import java.util.function.Function;

/**
 * Helper class for cases where we were using Ceylon's union types and can't easily refactor to
 * avoid that requirement.
 *
 * @author https://stackoverflow.com/users/464306/gdejohn (CC BY-SA 3.0)
 */
public abstract class Either<A, B> {
	private Either() {}

	public abstract <C> C either(Function<? super A, ? extends C> left,
		Function<? super B, ? extends C> right);

	public static <A, B> Either<A, B> left(final A value) {
		return new Either<>() {
			@Override
			public <C> C either(final Function<? super A, ? extends C> left,
			                    final Function<? super B, ? extends C> right) {
				return left.apply(value);
			}
		};
	}

	public static <A, B> Either<A, B> right(final B value) {
		return new Either<>() {
			@Override
			public <C> C either(final Function<? super A, ? extends C> left,
			                    final Function<? super B, ? extends C> right) {
				return right.apply(value);
			}
		};
	}

	public Optional<A> fromLeft() {
		return either(Optional::of, value -> Optional.empty());
	}

	public Optional<B> fromRight() {
		return either(value -> Optional.empty(), Optional::of);
	}
}
