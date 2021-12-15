package drivers.common;

/**
 * An interface for {@link NewFixtureSource}s to listen to.
 */
public interface NewFixtureSource {
	/**
	 * Add a listener.
	 */
	void addNewFixtureListener(NewFixtureListener listener);

	/**
	 * Remove a listener.
	 */
	void removeNewFixtureListener(NewFixtureListener listener);
}
