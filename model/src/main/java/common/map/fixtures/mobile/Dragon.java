package common.map.fixtures.mobile;

import common.map.IFixture;
import common.map.HasMutableImage;
import common.map.HasKind;

import java.util.function.Consumer;

/**
 * A dragon.
 */
public class Dragon implements Immortal, HasMutableImage, HasKind {
	public Dragon(String kind, int id) {
		this.kind = kind;
		this.id = id;
	}

	/**
	 * What kind of dragon.
	 */
	private final String kind;

	/**
	 * What kind of dragon.
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * ID number.
	 */
	private final int id;

	/**
	 * ID number.
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
	public Dragon copy(boolean zero) {
		Dragon retval = new Dragon(kind, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public String getShortDescription() {
		return (kind.isEmpty()) ? "dragon" : kind + " dragon";
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	/**
	 * @author https://openclipart.org/detail/166560/fire-dragon-by-olku
	 */
	@Override
	public String getDefaultImage() {
		return "dragon.png";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Dragon) {
			return ((Dragon) obj).getKind().equals(kind) && ((Dragon) obj).getId() == id;
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
		if (fixture instanceof Dragon) {
			return ((Dragon) fixture).getKind().equals(kind);
		} else {
			return false;
		}
	}

	@Override
	public boolean isSubset(IFixture obj, Consumer<String> report) {
		if (obj.getId() == id) {
			if (obj instanceof Dragon) {
				if (((Dragon) obj).getKind().equals(kind)) {
					return true;
				} else {
					report.accept("\tDifferent kinds of dragon for ID #" + id);
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
		return "Dragons";
	}

	@Override
	public int getDC() {
		return 20;
	}
}
