package lovelace.util;

@FunctionalInterface
public interface ThrowingFunction<I, O, E extends Exception> {
	O apply(I input) throws E;
}
