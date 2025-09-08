package lovelace.util;

import org.jspecify.annotations.NullUnmarked;

@FunctionalInterface
@NullUnmarked
public interface ThrowingBiConsumer<One, Two, Except extends Exception> {
	@SuppressWarnings("QuestionableName")
	void accept(One one, Two two) throws Except;
}


