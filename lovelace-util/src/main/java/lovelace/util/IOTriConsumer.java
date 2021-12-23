package lovelace.util;

import java.io.IOException;

@FunctionalInterface
public interface IOTriConsumer<One, Two, Three> {
	void accept(One one, Two two, Three three) throws IOException;
}


