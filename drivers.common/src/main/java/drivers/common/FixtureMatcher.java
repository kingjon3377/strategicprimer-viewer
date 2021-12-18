package drivers.common;

import java.util.Arrays;
import java.util.function.Predicate;
import common.map.TileFixture;

/**
 * A wrapper around <code>Predicate<TileFixture></code>, used to determine Z-order of fixtures.
 */
public class FixtureMatcher {
	/**
	 * Factory method for a matcher that matches every tile fixture of the given type.
	 */
	public static FixtureMatcher trivialMatcher(Class<? extends TileFixture> type) {
		return trivialMatcher(type, type.getName() + "s");
	}

	/**
	 * Factory method for a matcher that matches every tile fixture of the given type.
	 */
	public static FixtureMatcher trivialMatcher(Class<? extends TileFixture> type, String description) {
		return new FixtureMatcher(type::isInstance, description);
	}

	/**
	 * Factory method for a matcher that matches tile fixtures of the given
	 * type that additionally match the given predicate.
	 */
	public static <FixtureType extends TileFixture> FixtureMatcher simpleMatcher(
			Class<FixtureType> type, Predicate<FixtureType> method, String description) {
		Predicate<TileFixture> predicate = (fixture) -> 
			type.isInstance(fixture) && method.test((FixtureType) fixture);
		return new FixtureMatcher(predicate, description);
	}

	/**
	 * Factory method for two matchers covering fixtures of the given type
	 * that match and that do not match the given predicate.
	 */
	public static <FixtureType extends TileFixture> Iterable<FixtureMatcher> complements(
			Class<FixtureType> cls, Predicate<FixtureType> method, String firstDescription,
			String secondDescription) {
		return Arrays.asList(FixtureMatcher.<FixtureType>simpleMatcher(cls, method, firstDescription),
			FixtureMatcher.<FixtureType>simpleMatcher(cls, method.negate(), secondDescription));
	}

	private final Predicate<TileFixture> matchesImpl;

	/**
	 * Whether this matcher matches (applies to) the given fixture.
	 */
	public boolean matches(TileFixture fixture) {
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
	public void setDisplayed(boolean displayed) {
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

	public FixtureMatcher(Predicate<TileFixture> predicate, String desc) {
		matchesImpl = predicate;
		description = desc;
	}

	@Override
	public String toString() {
		return "Matcher for " + description;
	}
}
