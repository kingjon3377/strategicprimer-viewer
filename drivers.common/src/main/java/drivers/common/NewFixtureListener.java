package drivers.common;

import legacy.map.TileFixture;

/**
 * An interface for things that want to accept a new user-created tile fixture.
 */
public interface NewFixtureListener {
	/**
	 * Add the new fixture.
	 */
	void addNewFixture(TileFixture fixture);
}
