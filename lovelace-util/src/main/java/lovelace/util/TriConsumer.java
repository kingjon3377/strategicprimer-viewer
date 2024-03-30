package lovelace.util;

@FunctionalInterface
public interface TriConsumer<One, Two, Three> {
	@SuppressWarnings("QuestionableName")
	void accept(One one, Two two, Three three);
}
