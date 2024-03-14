package legacy.map.fixtures;

import java.util.function.Consumer;

import legacy.map.IFixture;
import legacy.map.HasImage;
import legacy.map.HasKind;
import org.jetbrains.annotations.NotNull;

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
	LegacyQuantity getQuantity();

	/**
	 * The turn on which the resource was created.
	 */
	int getCreated();

	/**
	 * If we ignore ID, a fixture is equal iff it is an IResourcePile with
	 * the same kind and contents, of the same age, with equal quantity.
	 */
	@Override
	default boolean equalsIgnoringID(final @NotNull IFixture fixture) {
		if (fixture instanceof final IResourcePile rp) {
			return rp.getKind().equals(getKind()) &&
					rp.getContents().equals(getContents()) &&
					rp.getQuantity().equals(getQuantity()) &&
					rp.getCreated() == getCreated();
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
	default boolean isSubset(final IFixture obj, final @NotNull Consumer<String> report) {
		if (obj.getId() == getId()) {
			if (obj instanceof final IResourcePile rp) {
				boolean retval = true;
				final Consumer<String> localReport =
						(str) -> report.accept(String.format("In Resource Pile, ID #%d: %s", getId(), str));
				if (!getKind().equals(rp.getKind())) {
					localReport.accept("Kinds differ");
					retval = false;
				}
				if (!getContents().equals(rp.getContents())) {
					localReport.accept("Contents differ");
					retval = false;
				}
				if (!getQuantity().isSubset(rp.getQuantity(), localReport)) {
					retval = false;
				}
				if (getCreated() != rp.getCreated() && rp.getCreated() != -1) {
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
	@NotNull
	IResourcePile copy(@NotNull CopyBehavior zero);
}
