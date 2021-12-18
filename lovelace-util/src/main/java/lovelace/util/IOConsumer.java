package lovelace.util;

import java.io.IOException;

@FunctionalInterface
public interface IOConsumer<Type> {
	void accept(Type item) throws IOException;
}


