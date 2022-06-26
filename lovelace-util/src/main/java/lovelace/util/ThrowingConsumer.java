package lovelace.util;

@FunctionalInterface
public interface ThrowingConsumer<Type, Except extends Exception> {
	void accept(Type item) throws Except;
}

