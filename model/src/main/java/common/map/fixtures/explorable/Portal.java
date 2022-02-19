package common.map.fixtures.explorable;

import common.map.SubsettableFixture;
import java.util.function.Consumer;
import common.map.IFixture;
import common.map.Point;

/**
 * A fixture representing a portal to another world.
 */
public class Portal implements ExplorableFixture, SubsettableFixture {

	public Portal(final String destinationWorld, final Point destinationCoordinates, final int id) {
		this.destinationWorld = destinationWorld;
		this.destinationCoordinates = destinationCoordinates;
		this.id = id;
	}

	/**
	 * A string identifying the world the portal connects to.
	 *
	 * TODO: Should this be mutable?
	 */
	private final String destinationWorld;

	/**
	 * A string identifying the world the portal connects to.
	 *
	 * TODO: Should this be mutable?
	 */
	public String getDestinationWorld() {
		return destinationWorld;
	}

	/**
	 * The coordinates in that world that the portal connects to. If
	 * invalid, the coordinate needs to be generated, presumably randomly,
	 * before any unit traverses the portal.
	 *
	 * TODO: Use Null instead of an invalid Point?
	 * TODO: "Combine with destinationWorld in a Tuple?
	 */
	private Point destinationCoordinates;

	/**
	 * The coordinates in that world that the portal connects to. If
	 * invalid, the coordinate needs to be generated, presumably randomly,
	 * before any unit traverses the portal.
	 *
	 * TODO: Use Null instead of an invalid Point?
	 * TODO: "Combine with destinationWorld in a Tuple?
	 */
	public Point getDestinationCoordinates() {
		return destinationCoordinates;
	}

	/**
	 * Set the coordinates in that world that the portal connects to. If
	 * invalid, the coordinate needs to be generated, presumably randomly,
	 * before any unit traverses the portal.
	 *
	 * TODO: Use Null instead of an invalid Point?
	 * TODO: "Combine with destinationWorld in a Tuple?
	 */
	public void setDestinationCoordinates(final Point destinationCoordinates) {
		this.destinationCoordinates = destinationCoordinates;
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

	@Override
	public Portal copy(final boolean zero) {
		final Portal retval = new Portal((zero) ? "unknown" : destinationWorld,
			(zero) ? Point.INVALID_POINT : destinationCoordinates, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public String getShortDescription() {
		return "A portal to another world";
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	@Override
	public String getDefaultImage() {
		return "portal.png";
	}

	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		if (this == fixture) {
			return true;
		} else if (fixture instanceof Portal) {
			return destinationWorld.equals(((Portal) fixture).getDestinationWorld()) &&
				destinationCoordinates.equals(((Portal) fixture).getDestinationCoordinates());
		} else {
			return false;
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Portal) {
			return ((Portal) obj).getId() == id && equalsIgnoringID((Portal) obj);
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
		return "Portals";
	}

	/**
	 * TODO: Test this
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Consumer<String> report) {
		if (obj.getId() == id) {
			if (obj instanceof Portal) {
				final Consumer<String> localReport =
					(str) -> report.accept(String.format("In portal with ID #%d: %s", id, str));
				if (!destinationWorld.equals(((Portal) obj).getDestinationWorld()) &&
						!"unknown".equals(((Portal) obj).getDestinationWorld())) {
					localReport.accept("Different destination world");
					return false;
				} else if (((Portal) obj).getDestinationCoordinates().isValid() &&
						!destinationCoordinates.equals(((Portal) obj).getDestinationCoordinates())) {
					localReport.accept("Different destination coordinates");
					return false;
				} else {
					return true;
				}
			} else {
				report.accept("Different kinds of fixtures for ID #" + id);
				return false;
			}
		} else {
			report.accept("Called with different ID #s");
			return false;
		}
	}

	/**
	 * The required Perception check result for an explorer to find the portal.
	 *
	 * TODO: This should probably be variable, i.e. read from XML
	 */
	@Override
	public int getDC() {
		return 35;
	}
}
