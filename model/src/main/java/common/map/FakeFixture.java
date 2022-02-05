package common.map;

import java.util.logging.Logger;

/**
 * An interface for "fixtures" used in the UI that should not actually be added to a map.
 */
public interface FakeFixture extends TileFixture, HasImage {
	static final Logger LOGGER = Logger.getLogger(FakeFixture.class.getName());
	/**
	 * A dummy "ID number"
	 * @deprecated A fake fixture should only ever be used in a
	 * FixtureListModel, so this should never be called.
	 */
	@Override
	@Deprecated
	default int getId() {
		LOGGER.warning("A fake fixture was asked for its ID");
		return -1;
	}

	/**
	 * Whether this equals another fixture if we ignore ID.
	 * @deprecated A fake fixture should only ever be used in a
	 * FixtureListModel, so this method should never be called.
	 */
	@Override
	@Deprecated
	default boolean equalsIgnoringID(final IFixture fixture) {
		LOGGER.warning("equalsIgnoringID() called on a fake fixture");
		return equals(fixture);
	}

	/**
	 * We don't allow per-instance icons for these, so always return the empty string.
	 */
	@Override
	default String getImage() {
		return "";
	}

	/**
	 * @deprecated A fake fixture should only ever be used in a
	 * FixtureListModel, so this method should never be called.
	 */
	@Override
	@Deprecated
	default String getPlural() {
		LOGGER.warning("A fake fixture asked for its plural");
		return "You shouldn't see this text; report this.";
	}

	/**
	 * Compare to another fixture.
	 * @deprecated A fake fixture should only ever be used in a
	 * FixtureListModel, so this method should never be called.
	 */
	@Override
	@Deprecated
	default int compareTo(final TileFixture fixture) {
		LOGGER.warning("compare() called on a fake fixture");
		return TileFixture.super.compareTo(fixture);
	}
}
