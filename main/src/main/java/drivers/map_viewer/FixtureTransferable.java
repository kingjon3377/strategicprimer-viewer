package drivers.map_viewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.Transferable;

import common.map.TileFixture;

/**
 * A class to transfer a TileFixture by drag-and-drop.
 */
/* package */ class FixtureTransferable implements Transferable {
	public static final DataFlavor FLAVOR = new DataFlavor(TileFixture.class, "TileFixture");

	private final TileFixture payload;

	public FixtureTransferable(final TileFixture data) {
		payload = data;
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
	public TileFixture getTransferData(final DataFlavor candidate) throws UnsupportedFlavorException {
		if (FLAVOR.equals(candidate)) {
			return payload;
		} else {
			throw new UnsupportedFlavorException(candidate);
		}
	}

	@Override
	public String toString() {
		return "FixtureTransferable transferring " + payload.getShortDescription();
	}

	@Override
	public boolean equals(final Object that) {
		return that instanceof FixtureTransferable ft && ft.payload.equals(payload);
	}

	@Override
	public int hashCode() {
		return payload.hashCode();
	}
}
