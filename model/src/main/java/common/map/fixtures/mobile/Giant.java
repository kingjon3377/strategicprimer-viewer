package common.map.fixtures.mobile;

import common.map.IFixture;
import common.map.HasMutableImage;
import common.map.HasKind;

import java.util.function.Consumer;

/**
 * A giant.
 */
public class Giant implements Immortal, HasMutableImage, HasKind {
	public Giant(String kind, int id) {
		this.kind = kind;
		this.id = id;
	}

	/**
	 * The kind of giant.
	 */
	private final String kind;

	/**
	 * The kind of giant.
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
	 * @param image The filename of an image to use as an icon for this instance.
	 */
	@Override
	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public Giant copy(boolean zero) {
		Giant retval = new Giant(kind, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public String getShortDescription() {
		return (kind.isEmpty()) ? "giant" : kind + " giant";
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	@Override
	public String getDefaultImage() {
		return "giant.png";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Giant) {
			return ((Giant) obj).getKind().equals(kind) && ((Giant) obj).getId() == id;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equalsIgnoringID(IFixture fixture) {
		if (fixture instanceof Giant) {
			return ((Giant) fixture).getKind().equals(kind);
		} else {
			return false;
		}
	}

	@Override
	public boolean isSubset(IFixture obj, Consumer<String> report) {
		if (obj.getId() == id) {
			if (obj instanceof Giant) {
				if (((Giant) obj).getKind().equals(kind)) {
					return true;
				} else {
					report.accept("\tDifferent kinds of giant for ID #" + id);
					return false;
				}
			} else {
				report.accept(String.format("\tFor ID #%d, different kinds of members", id));
				return false;
			}
		} else {
			report.accept(String.format("\tCalled with different IDs, #%d and %d",
				id, obj.getId()));
			return false;
		}
	}

	@Override
	public String getPlural() {
		return "Giants";
	}

	/**
	 * The required Perception check result to find the fairy.
	 *
	 * TODO: Should this vary with kind?
	 */
	@Override
	public int getDC() {
		return 28;
	}
}