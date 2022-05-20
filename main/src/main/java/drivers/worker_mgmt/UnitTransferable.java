package drivers.worker_mgmt;

import java.util.stream.Stream;
import java.util.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.Transferable;
import common.map.fixtures.mobile.IUnit;
import common.map.HasMutableKind;

/**
 * A class to transfer a Unit (to change its kind) by drag-and-drop.
 *
 * In Ceylon the type of items in the payload was {@code IUnit&HasMutableKind};
 * in Java we can't make the type as seen by the type system to be that precise, so callers
 * may have to explicitly cast transferred objects to HasMutableKind.
 *
 * TODO: Extract an interface to handle the commonly-duplicated flavor-bookkeeping aspect
 */
/* package */ class UnitTransferable implements Transferable {
	public static final DataFlavor FLAVOR = new DataFlavor(List.class, "List<IUnit>");
	private final List<IUnit> payload;
	public UnitTransferable(final IUnit... data) {
		if (!Stream.of(data).allMatch(HasMutableKind.class::isInstance)) {
			throw new IllegalArgumentException("All transferred units must have mutable kind.");
		}
		payload = List.of(data);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { FLAVOR };
	}

	@Override
	public boolean isDataFlavorSupported(final DataFlavor candidate) {
		return FLAVOR.equals(candidate);
	}

	@Override
	public List<IUnit> getTransferData(final DataFlavor candidate)
			throws UnsupportedFlavorException {
		if (FLAVOR.equals(candidate)) {
			return payload;
		} else {
			throw new UnsupportedFlavorException(candidate);
		}
	}

	@Override
	public String toString() { // TODO: cache?
		return String.format("UnitTransferable conveying %d unit(s)", payload.size());
	}

	@Override
	public boolean equals(final Object that) {
		if (that instanceof UnitTransferable) {
			return payload.equals(((UnitTransferable) that).payload);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return payload.hashCode();
	}
}
