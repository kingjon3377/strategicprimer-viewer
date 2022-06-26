package common.map.fixtures.resources;

import common.map.IFixture;
import common.map.HasPopulation;

import java.util.function.Consumer;

/**
 * A {@link common.map.TileFixture} to represent shrubs,
 * or their aquatic equivalents, on a tile.
 */
public class Shrub implements HarvestableFixture, HasPopulation<Shrub> {
	public Shrub(final String kind, final int id, final int population) {
		this.kind = kind;
		this.id = id;
		this.population = population;
	}

	public Shrub(final String kind, final int id) {
		this(kind, id, -1);
	}

	/**
	 * What kind of shrub this is
	 */
	private final String kind;

	/**
	 * What kind of shrub this is
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * The ID number.
	 */
	private final int id;

	/**
	 * The ID number.
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
	 * The filename of an image to use as an icon for this instance.
	 */
	@Override
	public void setImage(final String image) {
		this.image = image;
	}

	/**
	 * How many individual plants are in this planting of this shrub, or on this tile.
	 */
	private final int population;

	/**
	 * How many individual plants are in this planting of this shrub, or on this tile.
	 */
	@Override
	public int getPopulation() {
		return population;
	}

	@Override
	public Shrub copy(final CopyBehavior zero) {
		final Shrub retval = new Shrub(kind, id, (zero == CopyBehavior.ZERO) ? -1 : population);
		retval.setImage(image);
		return retval;
	}

	@Override
	public Shrub reduced(final int newPopulation, final int newId) {
		return new Shrub(kind, newId, newPopulation);
	}

	@Override
	public Shrub combined(final Shrub addend) {
		return new Shrub(kind, id,
			Math.max(0, population) + Math.max(0, addend.getPopulation()));
	}

	@Override
	public String getDefaultImage() {
		return "shrub.png";
	}

	@Override
	public String toString() {
		return kind;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Shrub it) {
			return it.getId() == id &&
				it.getKind().equals(kind) &&
				population == it.getPopulation();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		if (fixture instanceof Shrub it) {
			return kind.equals(it.getKind()) && population == it.getPopulation();
		} else {
			return false;
		}
	}

	@Override
	public String getPlural() {
		return "Shrubs";
	}

	@Override
	public String getShortDescription() {
		if (population < 1) {
			return kind;
		} else {
			return String.format("%d %s", population, kind);
		}
	}

	@Override
	public boolean isSubset(final IFixture other, final Consumer<String> report) {
		if (other.getId() != id) {
			report.accept("Different IDs");
			return false;
		} else if (other instanceof Shrub it) {
			if (!it.getKind().equals(kind)) {
				report.accept(String.format("In shrub with ID #%d:\tKinds differ", id));
				return false;
			} else if (it.getPopulation() > population) {
				report.accept(String.format("In shrub %s (#%d):\tHas higher count than we do", kind, id));
				return false;
			} else {
				return true;
			}
		} else {
			report.accept("Different types for ID #" + id);
			return false;
		}
	}

	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * TODO: Should this vary, either loading from XML or by kind?
	 */
	@Override
	public int getDC() {
		return 15;
	}
}
