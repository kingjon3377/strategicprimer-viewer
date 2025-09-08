package legacy.map.fixtures;

import legacy.map.Subsettable;
import legacy.map.SubsettableFixture;

/**
 * A (marker) interface for things that can be part of a unit.
 *
 * We extend {@link Subsettable} to make Unit's subset calculation show
 * differences in workers, but without hard-coding {@link
 * legacy.map.fixtures.mobile.Worker} in the Unit implementation. Most
 * implementations of this will essentially delegate {@link #isSubset} to
 * {@link Object#equals}.
 *
 * TODO: Members?
 */
public interface UnitMember extends SubsettableFixture {
	/**
	 * Specialization.
	 */
	@Override
	UnitMember copy(CopyBehavior zero);
}
