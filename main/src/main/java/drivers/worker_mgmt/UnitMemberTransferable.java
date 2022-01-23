package drivers.worker_mgmt;

import org.javatuples.Pair;
import java.util.Collections;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.List;
import common.map.fixtures.UnitMember;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.Transferable;
import common.map.fixtures.mobile.IUnit;

/**
 * A class to transfer a UnitMember via drag-and-drop.
 */
/* package */ class UnitMemberTransferable implements Transferable {
	public static final DataFlavor FLAVOR = new DataFlavor(List.class, "List<UnitMember>");

	private final List<Pair<UnitMember, IUnit>> payload;
	public UnitMemberTransferable(Pair<UnitMember, IUnit>... data) {
		payload = Arrays.asList(data);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { FLAVOR };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor candidate) {
		return FLAVOR.equals(candidate);
	}

	@Override
	public List<Pair<UnitMember, IUnit>> getTransferData(DataFlavor candidate)
			throws UnsupportedFlavorException {
		if (FLAVOR.equals(candidate)) {
			return Collections.unmodifiableList(payload);
		} else {
			throw new UnsupportedFlavorException(candidate);
		}
	}

	@Override
	public String toString() { // TODO: cache?
		return String.format("UnitMemberTransferable conveying %d unit(s)`", payload.size());
	}

	@Override
	public boolean equals(Object that) {
		if (that instanceof UnitMemberTransferable) {
			return payload.equals(((UnitMemberTransferable) that).payload);
		} else {
			return false;
		}
	}


	@Override
	public int hashCode() {
		return payload.hashCode();
	}
}