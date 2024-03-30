package lovelace.util;

@FunctionalInterface
public interface ThrowingTriConsumer<One, Two, Three, Except extends Exception> {
	@SuppressWarnings("QuestionableName")
	void accept(One one, Two two, Three three) throws Except;
}


