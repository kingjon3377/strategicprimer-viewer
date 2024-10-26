package lovelace.util;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A lazily-initialized cache of an enum's values.
 * @param <T> the enum type
 */
public class EnumCache<T extends Enum<T>> implements Supplier<List<T>> {
	private final Class<T> classType;
	private @Nullable List<T> cache = null;

	public EnumCache(final Class<T> classType) {
		this.classType = classType;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType") // List.of() is immutable
	@Override
	public List<T> get() {
		if (Objects.isNull(cache)) {
			cache = List.of(classType.getEnumConstants());
		}
		return cache;
	}

	@Override
	public String toString() {
		return "EnumCache for " + classType.getName();
	}
}
