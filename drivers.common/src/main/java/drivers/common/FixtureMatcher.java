package drivers.common;

import java.util.Arrays;
import java.util.function.Predicate;

import legacy.map.TileFixture;

/**
 * A wrapper around {@code Predicate<TileFixture>}, used to determine Z-order of fixtures.
 */
public final class FixtureMatcher {
	/**
	 * Factory method for a matcher that matches every tile fixture of the given type.
	 */
	public static FixtureMatcher trivialMatcher(final Class<? extends TileFixture> type) {
		return trivialMatcher(type, type.getSimpleName() + "s");
	}

	/**
	 * Factory method for a matcher that matches every tile fixture of the given type.
	 */
	public static FixtureMatcher trivialMatcher(final Class<? extends TileFixture> type, final String description) {
		return new FixtureMatcher(type::isInstance, description);
	}

	/**
	 * Factory method for a matcher that matches tile fixtures of the given
	 * type that additionally match the given predicate.
	 */
	public static <FixtureType extends TileFixture> FixtureMatcher simpleMatcher(
			final Class<FixtureType> type, final Predicate<FixtureType> method, final String description) {
		final Predicate<TileFixture> predicate = (fixture) ->
				type.isInstance(fixture) && method.test(type.cast(fixture));
		return new FixtureMatcher(predicate, description);
	}

	/**
	 * Factory method for two matchers covering fixtures of the given type
	 * that match and that do not match the given predicate.
	 */
	public static <FixtureType extends TileFixture> Iterable<FixtureMatcher> complements(
			final Class<FixtureType> cls, final Predicate<FixtureType> method, final String firstDescription,
			final String secondDescription) {
		return Arrays.asList(simpleMatcher(cls, method, firstDescription),
				simpleMatcher(cls, method.negate(), secondDescription));
	}

	private final Predicate<TileFixture> matchesImpl;

	/**
	 * Whether this matcher matches (applies to) the given fixture.
	 */
	public boolean matches(final TileFixture fixture) {
		return matchesImpl.test(fixture);
	}

	/**
	 * Whether fixtures that this matcher matches should be displayed.
	 */
	private boolean displayed = true;

	/**
	 * Whether fixtures that this matcher matches should be displayed.
	 */
	public boolean isDisplayed() {
		return displayed;
	}

	/**
	 * Whether fixtures that this matcher matches should be displayed.
	 */
	public void setDisplayed(final boolean displayed) {
		this.displayed = displayed;
	}

	/**
	 * A description of fixtures this matcher matches, to help the user
	 * decide whether to enable or disable it.
	 */
	private final String description;

	/**
	 * A description of fixtures this matcher matches, to help the user
	 * decide whether to enable or disable it.
	 */
	public String getDescription() {
		return description;
	}

	public FixtureMatcher(final Predicate<TileFixture> predicate, final String desc) {
		matchesImpl = predicate;
		description = desc;
	}

	@Override
	public String toString() {
		return "Matcher for " + description;
	}
}
