package common.map.fixtures.mobile;

import java.util.Collection;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * An interface for "proxy" implementations.
 */
public interface ProxyFor<Type> /* implements Type */ {
	/**
	 * Add another object to be proxied.
	 */
	void addProxied(Type item);

	/**
	 * Get the proxied items. This should probably only ever be used in tests, or in proxies managing nexted proxies.
	 */
	Collection<Type> getProxied();

	/**
	 * If there is consensus among proxied items on the given property,
	 * return it; otherwise return null.
	 *
	 * TODO: Return Optional instead?
	 */
	@Nullable
	default <MemberType> MemberType getConsensus(final Function<Type, MemberType> accessor) {
		@Nullable MemberType retval = null;
		for (final Type proxied : getProxied()) {
			final MemberType item = accessor.apply(proxied);
			if (retval != null) {
				if (!retval.equals(item)) {
					return null;
				}
			} else {
				retval = item;
			}
		}
		return retval;
	}

	/**
	 * If there is consensus on the given property among proxied items that
	 * define it, return it; otherwise return null.
	 * @deprecated Use getConsensus
	 */
	@Deprecated
	@Nullable
	default <MemberType> MemberType getNullableConsensus(final Function<Type, MemberType> accessor) {
		return getConsensus(accessor);
	}

	/**
	 * Whether this should be considered (if true) a proxy for multiple
	 * representations of the same item (such as in different maps), or (if
	 * false) a proxy for multiple related items (such as all workers in a
	 * single unit).
	 * TODO: If we keep proxies around, convert to enum.
	 */
	boolean isParallel();
}
