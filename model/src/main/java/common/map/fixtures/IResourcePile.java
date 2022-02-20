package common.map.fixtures;

import java.util.function.Consumer;

import common.map.IFixture;
import common.map.HasImage;
import common.map.HasKind;

/**
 * A quantity of some kind of resource.
 *
 * TODO: More members?
 */
public interface IResourcePile extends UnitMember, FortressMember, HasKind, HasImage {
	/**
	 * What specific kind of thing is in the resource pile.
	 */
	String getContents();

	/**
	 * How much of that thing is in the pile, including units.
	 */
	Quantity getQuantity();

	/**
	 * The turn on which the resource was created.
	 */
	int getCreated();

	/**
	 * If we ignore ID, a fixture is equal iff it is an IResourcePile with
	 * the same kind and contents, of the same age, with equal quantity.
	 */
	@Override
	default boolean equalsIgnoringID(final IFixture fixture) {
		if (fixture instanceof IResourcePile) {
			return ((IResourcePile) fixture).getKind().equals(getKind()) &&
				((IResourcePile) fixture).getContents().equals(getContents()) &&
				((IResourcePile) fixture).getQuantity().equals(getQuantity()) &&
				((IResourcePile) fixture).getCreated() == getCreated();
		} else {
			return false;
		}
	}

	/**
	 * A fixture is a subset iff it is an IResourcePile of the same kind,
	 * contents, and age, with the same ID, and its quantity is a subset of
	 * ours.
	 */
	@Override
	default boolean isSubset(final IFixture obj, final Consumer<String> report) {
		if (obj.getId() == getId()) {
			if (obj instanceof IResourcePile) {
				boolean retval = true;
				final Consumer<String> localReport =
					(str) -> report.accept(String.format("In Resource Pile, ID #%d: %s", getId(), str));
				if (!getKind().equals(((IResourcePile) obj).getKind())) {
					localReport.accept("Kinds differ");
					retval = false;
				}
				if (!getContents().equals(((IResourcePile) obj).getContents())) {
					localReport.accept("Contents differ");
					retval = false;
				}
				if (!getQuantity().isSubset(((IResourcePile) obj).getQuantity(), localReport)) {
					retval = false;
				}
				if (getCreated() != ((IResourcePile) obj).getCreated() &&
						((IResourcePile) obj).getCreated() != -1) {
					localReport.accept("Age differs");
					retval = false;
				}
				return retval;
			} else {
				report.accept("Different fixture types given for ID #" + getId());
				return false;
			}
		} else {
			report.accept("IDs differ");
			return false;
		}
	}

	/**
	 * Clone the object.
	 */
	@Override
	IResourcePile copy(CopyBehavior zero);
}
