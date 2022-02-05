package drivers.map_viewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.Transferable;

import common.map.TileFixture;

import java.util.List;
import java.util.Collections;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/**
 * A class to transfer a list of TileFixtures by drag-and-drop.
 *
 * TODO: Generalize, rename to CurriedTransferable, move to lovelace.util
 */
/* package */ class CurriedFixtureTransferable implements Transferable {
	public static final DataFlavor FLAVOR =
		new DataFlavor(CurriedFixtureTransferable.class, "CurriedTransferable");

	private final List<Transferable> payload;

	public CurriedFixtureTransferable(final TileFixture... list) {
		payload = Collections.unmodifiableList(Stream.of(list)
			.map(FixtureTransferable::new).collect(Collectors.toList()));
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
	public List<Transferable> getTransferData(final DataFlavor candidate) throws UnsupportedFlavorException {
		if (FLAVOR.equals(candidate)) {
			return payload;
		} else {
			throw new UnsupportedFlavorException(candidate);
		}
	}

	@Override
	public String toString() {
		return String.format("CurriedFixtureTransferable with payload containing %d elements",
			payload.size());
	}

	@Override
	public int hashCode() {
		return payload.hashCode();
	}
}
