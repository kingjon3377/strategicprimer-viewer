package model.map.fixtures;

import model.map.IFixture;
import model.map.Subsettable;

/**
 * A (marker) interface for things that can be part of a unit.
 *
 * We extend Subsettable to make Unit's subset calculation show differences in
 * workers, but without hard-coding "Worker" in the Unit implementation. Most
 * implementations of this will essentially delegate to equals().
 *
 * @author Jonathan Lovelace
 *
 */
public interface UnitMember extends IFixture, Subsettable<UnitMember> {
	// Just a marker interface for now. TODO: members?
}
