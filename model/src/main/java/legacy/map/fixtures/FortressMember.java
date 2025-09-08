package legacy.map.fixtures;

import legacy.map.SubsettableFixture;

/**
 * A (marker) interface for things that can be in a fortress.
 *
 * TODO: Members?
 */
public interface FortressMember extends SubsettableFixture {
	/**
	 * Specialization.
	 */
	@Override
	FortressMember copy(CopyBehavior zero);
}
