package model.map.fixtures.mobile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.Nullable;

import model.map.IFixture;
import model.map.Player;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.ProxyWorker;
import util.NullCleaner;
/**
 * A proxy for units in multiple maps.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
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
 *
 */
public final class ProxyUnit implements IUnit, ProxyFor<IUnit> {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = NullCleaner.assertNotNull(Logger.getLogger(ProxyUnit.class.getName()));
	/**
	 * Constructor.
	 * @param idNum the ID number of the units we are a proxy for.
	 */
	public ProxyUnit(final int idNum) {
		id = idNum;
	}
	/**
	 * The units we're a proxy for.
	 */
	private final List<IUnit> proxied = new ArrayList<>();
	/**
	 * @param unit a unit to start proxying
	 */
	@Override
	public void addProxied(final IUnit unit) {
		if (unit == this) {
			return;
		} else if (unit.getID() != id) {
			throw new IllegalArgumentException("Expected unit with ID #" + id);
		} else {
			proxied.add(unit);
		}
	}
	/**
	 * @return a copy of this proxy
	 * @param zero whether to "zero out" sensitive information
	 */
	@Override
	public IUnit copy(final boolean zero) {
		final ProxyUnit retval = new ProxyUnit(id);
		for (IUnit unit : proxied) {
			retval.addProxied(unit.copy(zero));
		}
		return retval;
	}
	/**
	 * The ID # of the units we are a proxy for.
	 */
	private final int id;
	/**
	 * This should never be actually called
	 * @return a Z-value for the fixture
	 */
	@Override
	public int getZValue() {
		LOGGER.warning("getZValue called on a ProxyUnit");
		return -10;
	}
	/**
	 * @return "Units"
	 */
	@Override
	public String plural() {
		return "Units";
	}
	/**
	 * @return a short description
	 */
	@Override
	public String shortDesc() {
		if (getOwner().isCurrent()) {
			return "a(n) " + getKind() + " unit belonging to you"; // NOPMD
		} else if (getOwner().isIndependent()) {
			return "an independent " + getKind() + " unit";
		} else {
			return "a(n) " + getKind() + " unit belonging to "
					+ getOwner().getName();
		}
	}
	/**
	 * @return the ID number of the units we proxy for
	 */
	@Override
	public int getID() {
		return id;
	}
	/**
	 * FIXME: implement.
	 *
	 * @param fix a fixture
	 * @return whether it equals this one except for ID #
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		LOGGER.warning("equalsIgnoringID called on a ProxyUnit");
		throw new IllegalStateException("FIXME: implement equalsIgnoringID()");
	}

	/**
	 * @param fix
	 *            a fixture
	 * @return the result of a comparison with it
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		LOGGER.warning("compareTo called on ProxyUnit");
		return fix.hashCode() - hashCode();
	}
	/**
	 * TODO: pass through to proxied units.
	 * @return the name of an image to represent the unit
	 */
	@Override
	public String getDefaultImage() {
		for (IUnit unit : proxied) {
			return unit.getDefaultImage();
		}
		return "unit.png";
	}
	/**
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public final void setImage(final String img) {
		LOGGER.warning("setImage() called on a ProxyUnit");
		for (IUnit unit : proxied) {
			unit.setImage(img);
		}
	}

	/**
	 * TODO: pass through to proxied units.
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		@Nullable String image = null;
		for (IUnit unit : proxied) {
			if (image == null) {
				image = unit.getImage();
			} else if (!image.equals(unit.getImage())) {
				return "";
			}
		}
		if (image == null) {
			return "";
		} else {
			return image;
		}
	}
	/**
	 * @return the kind of the units
	 */
	@Override
	public String getKind() {
		@Nullable String kind = null;
		for (IUnit unit : proxied) {
			if (kind == null) {
				kind = unit.getKind();
			} else if (!kind.equals(unit.getKind())) {
				return "proxied";
			}
		}
		if (kind == null) {
			return "proxied";
		} else {
			return kind;
		}
	}
	/**
	 * @param nKind the new kind of the proxied units
	 */
	@Override
	public void setKind(final String nKind) {
		for (IUnit unit : proxied) {
			unit.setKind(nKind);
		}
	}
	/**
	 * @return an iterator over (proxies for) unit members
	 */
	@Override
	public Iterator<UnitMember> iterator() {
		final Map<Integer, UnitMember> map = new TreeMap<>();
		for (IUnit unit : proxied) {
			for (UnitMember member : unit) {
				// Warning suppressed because the type in the map is really
				// a UnitMember&ProxyFor<IWorker|UnitMember>
				@SuppressWarnings("unchecked")
				@Nullable
				ProxyFor<? extends UnitMember> proxy;
				Integer memberID = NullCleaner.assertNotNull(Integer.valueOf(member.getID()));
				if (map.containsKey(memberID)) {
					proxy = (ProxyFor<? extends UnitMember>) map.get(memberID);
					if (proxy instanceof ProxyWorker) {
						if (member instanceof IWorker) {
							((ProxyWorker) proxy).addProxied((IWorker) member);
						} else {
							LOGGER.warning("Proxy is a ProxyWorker but member isn't a worker");
							continue;
						}
					} else {
						((ProxyMember) proxy).addProxied(member);
					}
				} else {
					if (member instanceof IWorker) {
						proxy = new ProxyWorker((IWorker) member);
					} else {
						proxy = new ProxyMember(member);
					}
					map.put(memberID, (UnitMember) proxy);
				}
			}
		}
		return NullCleaner.assertNotNull(map.values().iterator());
	}
	/**
	 * @return the name of the units (or "proxied" if they don't agree)
	 */
	@Override
	public String getName() {
		@Nullable String name = null;
		for (IUnit unit : proxied) {
			if (name == null) {
				name = unit.getName();
			} else if (!name.equals(unit.getName())) {
				return "proxied";
			}
		}
		if (name == null) {
			return "proxied";
		} else {
			return name;
		}
	}
	/**
	 * @param nomen the new name for the units
	 */
	@Override
	public void setName(final String nomen) {
		for (IUnit unit : proxied) {
			unit.setName(nomen);
		}
	}
	/**
	 * TODO: handle this like we handle 'name' and 'kind'.
	 * @return the owner of the first unit
	 */
	@Override
	public Player getOwner() {
		for (IUnit unit : proxied) {
			return unit.getOwner();
		}
		return new Player(-1, "proxied");
	}
	/**
	 * @param player the new owner for the units
	 */
	@Override
	public void setOwner(final Player player) {
		for (IUnit unit : proxied) {
			unit.setOwner(player);
		}
	}
	/**
	 * TODO: implement properly?
	 * @param obj ignored
	 * @param ostream the stream to write to
	 * @param context the context to write before writing our results
	 * @return false
	 * @throws IOException never, required by interface
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Appendable ostream,
			final String context)
			throws IOException {
		ostream.append(context);
		ostream.append("Called isSubset() in ProxyUnit");
		return false;
	}

	/**
	 * @return the orders shared by the units, or the empty string if their
	 *         orders are different.
	 */
	@Override
	public String getOrders() {
		@Nullable String orders = null;
		for (IUnit unit : proxied) {
			if (orders == null) {
				orders = unit.getOrders();
			} else if (orders.isEmpty()) {
				continue;
			} else if (!orders.equals(unit.getOrders())) {
				return "";
			}
		}
		if (orders == null) {
			return "";
		} else {
			return orders;
		}
	}
	/**
	 * @param newOrders The units' new orders
	 */
	@Override
	public void setOrders(final String newOrders) {
		for (IUnit unit : proxied) {
			unit.setOrders(newOrders);
		}
	}
	/**
	 * FIXME: Implement properly.
	 * @return a "verbose" description of the unit
	 */
	@Override
	public String verbose() {
		return "A proxy for units in several maps";
	}
	/**
	 * Add a member to a unit.
	 *
	 * FIXME: This shouldn't add the *same* object to multiple proxied units!
	 *
	 * @param member the member to add
	 */
	@Override
	public void addMember(final UnitMember member) {
		for (IUnit unit : proxied) {
			boolean shouldAdd = true;
			for (UnitMember item : unit) {
				if (member.equals(item)) {
					shouldAdd = false;
					break;
				}
			}
			if (shouldAdd) {
				unit.addMember(member);
			}
		}
	}
	/**
	 * Remove a member from the units.
	 *
	 * FIXME: Is this really right?
	 *
	 * @param member the member to remove
	 */
	@Override
	public void removeMember(final UnitMember member) {
		for (IUnit unit : proxied) {
			for (UnitMember item : unit) {
				if (member.equals(item)) {
					unit.removeMember(item);
					break;
				}
			}
		}
	}
	/**
	 * @return the proxied units
	 */
	@Override
	public Iterable<IUnit> getProxied() {
		return proxied;
	}
	/**
	 * A proxy for non-worker unit members.
	 */
	private static final class ProxyMember implements UnitMember, ProxyFor<UnitMember> {
		/**
		 * The proxied unit members.
		 */
		private final List<UnitMember> proxiedMembers = new ArrayList<>();
		/**
		 * No-arg no-op constructor for use in copy().
		 */
		private ProxyMember() {
			// do nothing
		}
		/**
		 * @param member the first member to proxy
		 */
		public ProxyMember(final UnitMember member) {
			proxiedMembers.add(member);
		}

		/**
		 * @return a copy of this proxy
		 * @param zero whether to "zero out" sensitive information
		 */
		@Override
		public ProxyMember copy(final boolean zero) {
			final ProxyMember retval = new ProxyMember();
			for (UnitMember member : proxiedMembers) {
				retval.addProxied(member.copy(zero));
			}
			return retval;
		}
		/**
		 * @return the ID number of the first proxied unit member (since they
		 *         should all have the same, in the only usage of this class)
		 */
		@Override
		public int getID() {
			for (UnitMember member : proxiedMembers) {
				return member.getID();
			}
			return -1;
		}
		/**
		 * @param fix a fixture
		 * @return whether it equals this one
		 */
		@Override
		public boolean equalsIgnoringID(final IFixture fix) {
			return fix instanceof ProxyMember
					&& ((ProxyMember) fix).proxiedMembers
							.equals(proxiedMembers);
		}
		/**
		 * @param obj ignored
		 * @param ostream the stream to write to
		 * @param context the context to write before we write our error
		 * @return false
		 * @throws IOException never, required by interface
		 */
		@Override
		public boolean isSubset(final IFixture obj, final Appendable ostream,
				final String context) throws IOException {
			ostream.append(context);
			ostream.append("isSubset called on ProxyMember");
			return false;
		}
		/**
		 * Add an item to be proxied.
		 * @param item the item to add
		 */
		@Override
		public void addProxied(final UnitMember item) {
			proxiedMembers.add(item);
		}
		/**
		 * @return the proxied members
		 */
		@Override
		public Iterable<UnitMember> getProxied() {
			return proxiedMembers;
		}
		/**
		 * @return a string representation of the proxied member
		 */
		@Override
		public String toString() {
			for (UnitMember member : proxiedMembers) {
				return NullCleaner.assertNotNull(member.toString());
			}
			return "a proxy for no unit members";
		}
	}
	/**
	 * @return a string representation of this class
	 */
	@Override
	public String toString() {
		return NullCleaner.assertNotNull(String.format("ProxyUnit for ID #%d", Integer.valueOf(id)));
	}
}
