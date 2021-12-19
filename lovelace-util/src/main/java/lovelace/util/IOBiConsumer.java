package lovelace.util;

import java.io.IOException;

@FunctionalInterface
public interface IOBiConsumer<One, Two> {
	void accept(One one, Two two) throws IOException;
}


