package lovelace.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * A wrapper around {@link Enumeration} where callers want an {@link Iterable}.
 * In practice the APIs that use Enumeration rather than {@link Iterable} don't
 * parameterize it, so we cast each item returned to the desired type instead
 * of requiring callers to coerce the type of the enumeration to be
 * parameterized properly.
 */
public final class EnumerationWrapper<Element> implements Iterator<Element> {
	private final Enumeration<?> wrapped;
	private final Class<? extends Element> cls;

	public EnumerationWrapper(final Enumeration<?> enumeration, final Class<? extends Element> cls) {
		wrapped = enumeration;
		this.cls = cls;
	}

	@Override
	public boolean hasNext() {
		return wrapped.hasMoreElements();
	}

	@Override
	public Element next() {
		return cls.cast(wrapped.nextElement());
	}

	@Override
	public String toString() {
		return "EnumerationWrapper{wrapped=" + wrapped + '}';
	}
}
