package drivers.map_viewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.Transferable;

import legacy.map.TileFixture;

import java.util.List;
import java.util.stream.Stream;

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
		payload = Stream.of(list).map(FixtureTransferable::new).map(Transferable.class::cast).toList();
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{FLAVOR};
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
		return "CurriedFixtureTransferable with payload containing %d elements".formatted(payload.size());
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof final CurriedFixtureTransferable cft && cft.payload.equals(payload);
	}

	@Override
	public int hashCode() {
		return payload.hashCode();
	}
}
