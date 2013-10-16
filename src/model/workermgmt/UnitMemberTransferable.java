package model.workermgmt;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;

import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to transfer a UnitMember.
 *
 * @author Jonathan Lovelace
 */
public class UnitMemberTransferable implements Transferable, Serializable {
	/**
	 * A pair of a unit member and its containing unit.
	 */
	public static class UnitMemberPair implements Serializable {
		// ESCA-JAVA0096:
		/**
		 * Version UID for serialization.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor.
		 *
		 * @param theMember the first element
		 * @param theUnit the second element
		 */
		public UnitMemberPair(final UnitMember theMember, final Unit theUnit) {
			member = theMember;
			unit = theUnit;
		}

		/**
		 * The unit member.
		 */
		public final UnitMember member;
		/**
		 * The unit containing it.
		 */
		public final Unit unit;
	}

	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 *
	 * @param theData the object
	 * @param theParent its containing object
	 */
	public UnitMemberTransferable(final UnitMember theData, final Unit theParent) {
		data = new UnitMemberPair(theData, theParent);
	}

	/**
	 * The object we're transfering.
	 */
	private final UnitMemberPair data;
	/**
	 * a DataFlavor representing its class.
	 */
	public static final DataFlavor FLAVOR = new DataFlavor(
			UnitMemberPair.class, "Worker");

	/**
	 *
	 * @return the supported DataFlavors.
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { FLAVOR };
	}

	/**
	 *
	 * @param dflavor a DataFlavor
	 *
	 * @return whether it's the one we support
	 */
	@Override
	public boolean isDataFlavorSupported(@Nullable final DataFlavor dflavor) {
		return FLAVOR.equals(dflavor);
	}

	/**
	 * This now returns the source component's listened property for text
	 * flavors, as part of a hack to disallow intra-component drops.
	 *
	 * @param dflavor a DataFlavor
	 * @return our underlying data if they want it in the flavor we support
	 * @throws UnsupportedFlavorException if they want an unsupported flavor
	 * @throws IOException required by spec but not thrown
	 */
	@Override
	public Object getTransferData(@Nullable final DataFlavor dflavor)
			throws UnsupportedFlavorException, IOException {
		if (FLAVOR.equals(dflavor)) {
			return data; // NOPMD
		} else {
			throw new UnsupportedFlavorException(dflavor);
		}
	}

	/**
	 *
	 * @return a String representation of this object
	 */
	@Override
	public String toString() {
		return "UnitMemberTransferable";
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof UnitMemberTransferable
				&& data.equals(((UnitMemberTransferable) obj).data);
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return data.hashCode();
	}
}
