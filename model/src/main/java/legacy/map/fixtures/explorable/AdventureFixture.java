package legacy.map.fixtures.explorable;

import legacy.map.SubsettableFixture;

import java.util.function.Consumer;

import legacy.map.IFixture;
import legacy.map.HasMutableOwner;
import legacy.map.Player;

/**
 * A Fixture representing an adventure hook. Satisfies Subsettable because
 * players shouldn't know when another player completes an adventure on the far
 * side of the world.
 */
public class AdventureFixture implements ExplorableFixture, HasMutableOwner, SubsettableFixture {
	public AdventureFixture(final Player owner, final String briefDescription, final String fullDescription, final int id) {
		this.owner = owner;
		this.briefDescription = briefDescription;
		this.fullDescription = fullDescription;
		this.id = id;
	}

	/**
	 * A brief description of the adventure.
	 */
	private final String briefDescription;

	/**
	 * A brief description of the adventure.
	 */
	public String getBriefDescription() {
		return briefDescription;
	}

	/**
	 * A longer description of the adventure.
	 */
	private final String fullDescription;

	/**
	 * A longer description of the adventure.
	 */
	public String getFullDescription() {
		return fullDescription;
	}

	/**
	 * A unique ID number.
	 */
	private final int id;

	/**
	 * A unique ID number.
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * The filename of an image to use as an icon for this instance.
	 */
	private String image = "";

	/**
	 * The filename of an image to use as an icon for this instance.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * Set the filename of an image to use as an icon for this instance.
	 */
	@Override
	public void setImage(final String image) {
		this.image = image;
	}

	/**
	 * The player that has undertaken the adventure.
	 */
	private Player owner;

	/**
	 * The player that has undertaken the adventure.
	 */
	@Override
	public Player owner() {
		return owner;
	}

	/**
	 * Set the player that has undertaken the adventure.
	 */
	@Override
	public void setOwner(final Player owner) {
		this.owner = owner;
	}

	/**
	 * Clone the fixture.
	 */
	@Override
	public AdventureFixture copy(final CopyBehavior zero) {
		final AdventureFixture retval = new AdventureFixture(owner, briefDescription,
			fullDescription, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public String toString() {
		if (fullDescription.isEmpty()) {
			if (briefDescription.isEmpty()) {
				return "Adventure hook";
			} else {
				return briefDescription;
			}
		} else {
			return fullDescription;
		}
	}

	@Override
	public String getDefaultImage() {
		return "adventure.png";
	}

	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		if (this == fixture) {
			return true;
		} else if (fixture instanceof final AdventureFixture obj) {
			return ((owner.isIndependent() && obj.owner().isIndependent()) ||
				(owner.getPlayerId() == obj.owner().getPlayerId())) &&
				briefDescription.equals(obj.getBriefDescription()) &&
				fullDescription.equals(obj.getFullDescription());
		} else {
			return false;
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof final AdventureFixture af) {
			return id == af.getId() && equalsIgnoringID(af);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String getPlural() {
		return "Adventures";
	}

	@Override
	public String getShortDescription() {
		return briefDescription;
	}

	/**
	 * The required Perception check result for an explorer to find the adventure hook.
	 *
	 * TODO: Should probably be variable, i.e. read from XML
	 */
	@Override
	public int getDC() {
		return 30;
	}

	@Override
	public boolean isSubset(final IFixture obj, final Consumer<String> report) {
		if (obj.getId() == id) {
			if (obj instanceof final AdventureFixture af) {
				final Consumer<String> localReport =
					(str) -> report.accept(String.format("In adventure with ID #%d: %s", id, str));
				if (!briefDescription.equals(af.getBriefDescription())) {
					localReport.accept("Brief descriptions differ");
					return false;
				} else if (!fullDescription.equals(af.getFullDescription())) {
					localReport.accept("Full descriptions differ");
					return false;
				} else if (owner.getPlayerId() != af.owner().getPlayerId() &&
					af.owner().isIndependent()) {
					localReport.accept("Owners differ");
					return false;
				} else {
					return true;
				}
			} else {
				report.accept("Different kinds of fixtures for ID #" + id);
				return false;
			}
		} else {
			report.accept("ID mismatch");
			return false;
		}
	}
}
