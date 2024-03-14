package legacy.map.fixtures;

import legacy.map.IFixture;
import legacy.map.HasPopulation;
import legacy.map.HasMutableImage;
import legacy.map.HasKind;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A piece of equipment.
 *
 * TODO: More members?
 */
public class Implement implements UnitMember, FortressMember, HasKind, HasMutableImage, HasPopulation<@NotNull Implement> {
	public Implement(final String kind, final int id, final int count) {
		this.kind = kind;
		this.id = id;
		this.count = count;
	}

	public Implement(final String kind, final int id) {
		this(kind, id, 1);
	}

	@Override
	public @NotNull String getPlural() {
		return "Equipment";
	}

	/**
	 * The "kind" of the implement.
	 */
	private final String kind;

	/**
	 * The "kind" of the implement.
	 */
	@Override
	public @NotNull String getKind() {
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
	 * How many of this kind of equipment are in this stack.
	 */
	private final int count;

	/**
	 * How many of this kind of equipment are in this stack.
	 */
	public int getCount() {
		return count;
	}

	@Override
	public int getPopulation() {
		return count;
	}

	/**
	 * The filename of an image to use as an icon for this instance.
	 */
	private String image = "";

	/**
	 * The filename of an image to use as an icon for this instance.
	 */
	@Override
	public @NotNull String getImage() {
		return image;
	}

	/**
	 * Set the filename of an image to use as an icon for this instance.
	 */
	@Override
	public void setImage(final @NotNull String image) {
		this.image = image;
	}

	/**
	 * If we ignore ID, a fixture is equal iff itis an Implement of the same kind.
	 */
	@Override
	public boolean equalsIgnoringID(final @NotNull IFixture fixture) {
		if (this == fixture) {
			return true;
		} else if (fixture instanceof final Implement that) {
			return kind.equals(that.kind) && that.getCount() == count;
		} else {
			return false;
		}
	}

	/**
	 * A fixture is a subset iff it is equal.
	 */
	@Override
	public boolean isSubset(final IFixture obj, final @NotNull Consumer<String> report) {
		if (this == obj) {
			return true;
		} else if (obj.getId() == id) {
			if (obj instanceof final Implement that) {
				if (kind.equals(that.getKind())) {
					if (that.getCount() <= count) {
						return true;
					} else {
						report.accept(String.format("In Implement ID #%d:\tHas higher count than we do", id));
						return false;
					}
				} else {
					report.accept(String.format("In Implement ID #%d:\tKinds differ", id));
					return false;
				}
			} else {
				report.accept(String.format("Different fixture types given for ID #%d", id));
				return false;
			}
		} else {
			report.accept("IDs differ");
			return false;
		}
	}

	@Override
	public @NotNull Implement copy(final @NotNull CopyBehavior zero) {
		return new Implement(kind, id, count);
	}

	@Override
	public @NotNull Implement reduced(final int newPopulation, final int newId) {
		return new Implement(kind, newId, newPopulation);
	}

	@Override
	public @NotNull Implement combined(final @NotNull Implement addend) {
		return new Implement(kind, id, Math.max(0, count) + Math.max(0, addend.getCount()));
	}

	@Override
	public @NotNull String getDefaultImage() {
		return "implement.png";
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof final Implement that) {
			return that.getId() == id && kind.equals(that.getKind()) &&
					that.getCount() == count;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		if (count == 1) {
			return "An implement of kind " + kind;
		} else {
			return String.format("A group of %d implements of kind %s", count, kind);
		}
	}
}
