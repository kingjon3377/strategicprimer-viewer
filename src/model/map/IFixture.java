package model.map;


/**
 * A supertype for both TileFixture and any UnitMembers (etc.) that shouldn't be
 * TileFixtures, so we don't have to special-case them for things like
 * searching.
 *
 * @author Jonathan Lovelace
 *
 */
public interface IFixture {
	/**
	 * @return an ID (UID for most fixtures, though perhaps not for things like
	 *         mountains and hills) for the fixture.
	 */
	int getID();

	/**
	 * @param fix a fixture
	 * @return whether it's equal, ignoring ID (and DC for events), to this one
	 */
	boolean equalsIgnoringID(IFixture fix);
}
