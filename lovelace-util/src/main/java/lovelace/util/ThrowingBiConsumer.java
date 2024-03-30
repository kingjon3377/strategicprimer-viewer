package lovelace.util;

@FunctionalInterface
public interface ThrowingBiConsumer<One, Two, Except extends Exception> {
	@SuppressWarnings("QuestionableName")
	void accept(One one, Two two) throws Except;
}


