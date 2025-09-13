package lovelace.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to parse enum values from their corresponding toString values.
 *
 * We could throw ParseException, but some callers can't afford having a checked exception thrown.
 *
 * @param <T> the enum type.
 */
public final class EnumParser<T extends Enum<T>> implements ThrowingFunction<String, T, IllegalArgumentException> {
	private final String string;
	private final Map<String, T> cache;

	public EnumParser(final Class<T> classType, final int enumCount) {
		string = "EnumParser<%s>".formatted(classType.getSimpleName());
		cache = new HashMap<>(enumCount);
		for (final T item : classType.getEnumConstants()) {
			if (cache.containsKey(item.toString())) {
				throw new IllegalStateException("EnumParser used for type with non-unique toString");
			}
			cache.put(item.toString(), item);
		}
	}

	@Override
	public T apply(final String input) throws IllegalArgumentException {
		final T retval = cache.get(input);
		if (retval == null) {
			throw new IllegalArgumentException("Failed to parse value from " + input);
		}
		return retval;
	}

	@Override
	public String toString() {
		return string;
	}
}
