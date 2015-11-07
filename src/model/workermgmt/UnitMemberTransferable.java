package model.workermgmt;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.eclipse.jdt.annotation.Nullable;

import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import util.NullCleaner;

/**
 * A class to transfer a UnitMember.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class UnitMemberTransferable implements Transferable {
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
	 * A pair of a unit member and its containing unit.
	 * @author Jonathan Lovelace
	 */
	public static class UnitMemberPair {
		/**
		 * The unit member.
		 */
		public final UnitMember member;
		/**
		 * The unit containing it.
		 */
		public final IUnit unit;
		/**
		 * Constructor.
		 *
		 * @param theMember the first element
		 * @param theUnit the second element
		 */
		public UnitMemberPair(final UnitMember theMember, final IUnit theUnit) {
			member = theMember;
			unit = theUnit;
		}

		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			final String memberStr = member.toString();
			final String unitStr = unit.toString();
			final StringBuilder builder = new StringBuilder(22
					+ memberStr.length() + unitStr.length());
			builder.append("UnitMemberPair: (");
			builder.append(memberStr);
			builder.append(", ");
			builder.append(unitStr);
			builder.append(')');
			return NullCleaner.assertNotNull(builder.toString());
		}

	}

	/**
	 * Constructor.
	 *
	 * @param theData the object
	 * @param theParent its containing object
	 */
	public UnitMemberTransferable(final UnitMember theData, final IUnit theParent) {
		data = new UnitMemberPair(theData, theParent);
	}

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
	public UnitMemberPair getTransferData(@Nullable final DataFlavor dflavor)
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
