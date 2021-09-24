package common.map.fixtures;

import common.map.IFixture;
import common.map.Subsettable;

/**
 * A (marker) interface for things that can be in a fortress.
 *
 * TODO: Members?
 */
public interface FortressMember extends IFixture, Subsettable<IFixture> {
	/**
	 * Specialization.
	 */
	@Override
	FortressMember copy(boolean zero);
}
