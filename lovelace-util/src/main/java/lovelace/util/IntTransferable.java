package lovelace.util;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Objects;

/**
 * A {@link Transferable} implementation transferring a single integer.
 */
public final class IntTransferable implements Transferable {
	private final DataFlavor flavor;
	private final int payload;

	/**
	 * @param flavor  the {@link DataFlavor flavor} we should advertise support for
	 * @param payload the integer to transfer
	 */
	public IntTransferable(final DataFlavor flavor, final int payload) {
		this.flavor = flavor;
		this.payload = payload;
	}

	/**
	 * A single-element array containing the provided {@link #flavor}.
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{flavor};
	}

	/**
	 * Only the provided {@link #flavor} is supported.
	 */
	@Override
	public boolean isDataFlavorSupported(final DataFlavor possibility) {
		return Objects.equals(flavor, possibility);
	}

	/**
	 * If "wantedFlavor" is equal to {@link #flavor}, return {@link
	 * #payload}; otherwise throw an {@link UnsupportedFlavorException}.
	 */
	@Override
	public Integer getTransferData(final DataFlavor wantedFlavor) throws UnsupportedFlavorException {
		if (Objects.equals(flavor, wantedFlavor)) {
			return payload;
		} else {
			throw new UnsupportedFlavorException(wantedFlavor);
		}
	}

	/**
	 * A simple diagnostic string.
	 */
	@Override
	public String toString() {
		return "IntTransferable carrying " + payload;
	}

	/**
	 * Use the {@link #payload} as the hash value.
	 */
	@Override
	public int hashCode() {
		return payload;
	}

	/**
	 * An object is equal iff it is an IntTransferable with the same {@link #flavor} and {@link #payload}.
	 */
	@Override
	public boolean equals(final Object that) {
		return that instanceof final IntTransferable t && t.payload == payload && Objects.equals(t.flavor, flavor);
	}
}
