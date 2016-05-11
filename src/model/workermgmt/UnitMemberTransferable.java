package model.workermgmt;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import org.eclipse.jdt.annotation.Nullable;
import util.Pair;

/**
 * A class to transfer a UnitMember.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class UnitMemberTransferable implements Transferable {
	/**
	 * The object we're transfering.
	 */
	private final UnitMemberPairList data;
	/**
	 * a DataFlavor representing its class.
	 */
	public static final DataFlavor FLAVOR =
			new DataFlavor(UnitMemberPairList.class, "List<Worker>");
	/**
	 * A list of pairs of unit members and their containing units. The purpose of this
	 * class is to fix the type parameters in the type hierarchy, avoiding unchecked-cast
	 * warnings later.
	 * @author Jonathan Lovelace
	 */
	@SuppressWarnings("PublicField")
	public static final class UnitMemberPairList extends
			AbstractList<Pair<UnitMember, IUnit>> {
		/**
		 * The list we wrap.
		 */
		private final List<Pair<UnitMember, IUnit>> wrapped;
		/**
		 * Constructor.
		 * @param list the list to wrap
		 */
		public UnitMemberPairList(final List<Pair<UnitMember, IUnit>> list) {
			wrapped = new ArrayList<>(list);
		}

		/**
		 * @param index an index into the list
		 * @return the item at that index
		 */
		@Override
		public Pair<UnitMember, IUnit> get(final int index) {
			return wrapped.get(index);
		}

		/**
		 * @return the size of the list
		 */
		@Override
		public int size() {
			return wrapped.size();
		}

		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "UnitMemberPairList with " + size() + " members";
		}
	}

	/**
	 * Constructor.
	 *
	 * @param list the list of pairs of unit members and their parents to convey
	 */
	public UnitMemberTransferable(final List<Pair<UnitMember, IUnit>> list) {
		data = new UnitMemberPairList(list);
	}

	/**
	 * @return the supported DataFlavors.
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{FLAVOR};
	}

	/**
	 * @param flavor a DataFlavor
	 * @return whether it's the one we support
	 */
	@Override
	public boolean isDataFlavorSupported(@Nullable final DataFlavor flavor) {
		return FLAVOR.equals(flavor);
	}

	/**
	 * This now returns the source component's listened property for text flavors, as
	 * part
	 * of a hack to disallow intra-component drops.
	 *
	 * @param flavor a DataFlavor
	 * @return our underlying data if they want it in the flavor we support
	 * @throws UnsupportedFlavorException if they want an unsupported flavor
	 */
	@Override
	public UnitMemberPairList getTransferData(@Nullable final DataFlavor flavor)
			throws UnsupportedFlavorException {
		if (FLAVOR.equals(flavor)) {
			return data; // NOPMD
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	/**
	 * @return a String representation of this object
	 */
	@Override
	public String toString() {
		return "UnitMemberTransferable conveying " + data.size() + " member(s)";
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof UnitMemberTransferable) && data.equals(
				((UnitMemberTransferable) obj).data));
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return data.hashCode();
	}
}
