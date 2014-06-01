package model.map;

/**
 * An interface to use to make Tile.isSubset() work properly without
 * special-casing every Subsettable fixture.
 *
 * @author Jonathan Lovelace
 *
 */
public interface SubsettableFixture extends IFixture,
		Subsettable<IFixture> {
	// Marker interface
}
