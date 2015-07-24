package model.map.fixtures.mobile;
/**
 * An interface for 'proxy' implementations.
 * @author Jonathan Lovelace
 *
 * @param <T> the type being proxied
 */
public interface ProxyFor<T> /* extends T */ {
	/**
	 * Add another object to be proxied.
	 * @param item the object to be proxied.
	 */
	void addProxied(T item);
	/**
	 * This should probably only ever be used in tests.
	 * @return the proxied items.
	 */
	Iterable<T> getProxied();
}
