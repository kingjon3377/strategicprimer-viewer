package legacy.map.fixtures.explorable;

import legacy.map.SubsettableFixture;

import java.util.function.Consumer;
import java.util.function.Function;

import legacy.map.IFixture;
import legacy.map.HasOwner;
import legacy.map.Player;
import org.javatuples.Pair;

import static lovelace.util.MatchingValue.matchingValue;

/**
 * A Fixture representing an adventure hook. Satisfies Subsettable because
 * players shouldn't know when another player completes an adventure on the far
 * side of the world.
 */
public interface AdventureFixture extends ExplorableFixture, SubsettableFixture, HasOwner {
	/**
	 * A brief description of the adventure.
	 */
	String getBriefDescription();

	/**
	 * A longer description of the adventure.
	 */
	String getFullDescription();

	/**
	 * A unique ID number.
	 */
	@Override
	int getId();

	/**
	 * The filename of an image to use as an icon for this instance.
	 */
	@Override
	String getImage();

	/**
	 * The player that has undertaken the adventure.
	 */
	@Override
	Player owner();

	/**
	 * Clone the fixture.
	 */
	@Override
	AdventureFixture copy(CopyBehavior zero);

	@Override
	default String getDefaultImage() {
		return "adventure.png";
	}

	@Override
	default boolean equalsIgnoringID(final IFixture fixture) {
		if (this == fixture) {
			return true;
		} else if (fixture instanceof final AdventureFixture obj) {
			return ((owner().isIndependent() && obj.owner().isIndependent()) ||
					(owner().getPlayerId() == obj.owner().getPlayerId())) &&
					getBriefDescription().equals(obj.getBriefDescription()) &&
					getFullDescription().equals(obj.getFullDescription());
		} else {
			return false;
		}
	}

	@Override
	default String getPlural() {
		return "Adventures";
	}

	/// The required Perception check result for an explorer to find the adventure hook.
	///
	/// TODO: Should probably be variable, i.e. read from XML
	@SuppressWarnings("MagicNumber")
	@Override
	default int getDC() { return 30; }

	default boolean ownerSubset(final AdventureFixture fix) {
		return fix.owner().isIndependent() || fix.owner().getPlayerId() == owner().getPlayerId();
	}

	Function<IFixture, AdventureFixture> CAST = AdventureFixture.class::cast;

	@Override
	default boolean isSubset(final IFixture obj, final Consumer<String> report) {
		if (obj.getId() == getId()) {
			if (obj instanceof final AdventureFixture af) {
				final Consumer<String> localReport =
						(str) -> report.accept("In adventure with ID #%d: %s".formatted(getId(), str));
				return passesAllPredicates(localReport, af, Pair.with("Brief descriptions differ", matchingValue(this,
								CAST.andThen(AdventureFixture::getBriefDescription))),
						Pair.with("Full descriptions differ", matchingValue(this,
								CAST.andThen(AdventureFixture::getFullDescription))),
						Pair.with("Owners differ", matchingValue(this, CAST.andThen(this::ownerSubset))));
			} else {
				report.accept("Different kinds of fixtures for ID #" + getId());
				return false;
			}
		} else {
			report.accept("ID mismatch");
			return false;
		}
	}
}
