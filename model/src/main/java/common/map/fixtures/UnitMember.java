package common.map.fixtures;

import common.map.IFixture;
import common.map.Subsettable;

/**
 * A (marker) interface for things that can be part of a unit.
 *
 * We extend {@link Subsettable} to make Unit's subset calculation show
 * differences in workers, but without hard-coding {@link
 * common.map.fixtures.mobile.Worker} in the Unit implementation. Most
 * implementations of this will essentially delegate {@link isSubset} to
 * {@link equals}.
 *
 * TODO: Members?
 */
public interface UnitMember extends IFixture, Subsettable<IFixture> {
	/**
	 * Specialization.
	 */
	@Override
	UnitMember copy(boolean zero);
}
