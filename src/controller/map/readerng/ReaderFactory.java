package controller.map.readerng;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory class for per-class XML readers.
 * @author Jonathan Lovelace
 *
 */
public final class ReaderFactory {
	/**
	 * Do not instantiate.
	 */
	private ReaderFactory() {
		// Don't instantiate.
	}
	/**
	 * The cache.
	 */
	private static final Map<Class<?>, INodeReader<?>> CACHE = new HashMap<Class<?>, INodeReader<?>>();
	/**
	 * Add a reader to the cache.
	 * @param reader the reader to add
	 */
	private static void factory(final INodeReader<?> reader) {
		CACHE.put(reader.represents(), reader);
	}
	/**
	 * Add a bunch of readers to the cache.
	 * @param readers the readers to add
	 */
	private static void factoryMulti(final INodeReader<?>... readers) {
		for (INodeReader<?> reader : readers) {
			factory(reader);
		}
	}
	static {
		factoryMulti(new SPMapReader(), new PlayerReader());
	}
	/**
	 * @param <T> the type of reader wanted
	 * @param desiredClass the type of reader wanted
	 * @return an instance of that class
	 */
	public static <T> INodeReader<T> createReader(final Class<T> desiredClass) {
		if (CACHE.containsKey(desiredClass)) { 
			return (INodeReader<T>) CACHE.get(desiredClass);
		} else {
			throw new IllegalArgumentException("We don't have a reader for that");
		}
	}
}
