package lovelace.util;

@FunctionalInterface
public interface ThrowingBiConsumer<One, Two, Except extends Exception> {
	void accept(One one, Two two) throws Except;
}


