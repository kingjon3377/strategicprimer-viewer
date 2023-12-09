package legacy.map;

import legacy.map.fixtures.UnitMember;

/**
 * A supertype for both {@link TileFixture} and any {@link
 * UnitMember UnitMembers} (etc.) that shouldn't be
 * {@link TileFixture TileFixtures}s, so we don't have to special-case them for
 * things like searching.
 *
 * TODO: Add a method for generic "identity": most implementations will probably just check type and ID, but units will
 * probably check name and kind, for instance. This is looser than equals(), since adding members to a unit shouldn't
 * break identity, but not *too* vague.
 */
public interface IFixture {
	/**
	 * A plural phrase describing all members of the kind of fixture.
	 */
	String getPlural();

	/**
	 * The fixture's ID number. For most fixtures this should be unique in the map.
	 */
	int getId();

	/**
	 * Whether the fixture is equal to another if we ignore its ID (and DC for events).
	 */
	boolean equalsIgnoringID(IFixture fixture);

	enum CopyBehavior {
		/**
		 * Keep all information, e.g. when copying between maps for the same player.
		 */
		KEEP,
		/**
		 * "Zero out" (omit) any "sensitive" information in the copy.
		 */
		ZERO
	}

	/**
	 * Clone the fixture, optionally "sanitizing" it in a way that should
	 * not break subset checking.
	 *
	 * @param zero Whether to "zero out" (omit) sensitive information in the copy.
	 */
	IFixture copy(CopyBehavior zero);
}
