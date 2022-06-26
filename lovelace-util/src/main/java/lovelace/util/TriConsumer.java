package lovelace.util;

@FunctionalInterface
public interface TriConsumer<One, Two, Three> {
	void accept(One one, Two two, Three three);
}
