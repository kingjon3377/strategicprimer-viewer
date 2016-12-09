package model.map.fixtures.mobile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Logger;
import model.map.HasMutableImage;
import model.map.HasMutableKind;
import model.map.HasMutableName;
import model.map.HasMutableOwner;
import model.map.IFixture;
import model.map.Player;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.ProxyWorker;
import org.eclipse.jdt.annotation.Nullable;
import util.EmptyIterator;
import util.LineEnd;
import util.TypesafeLogger;

import static util.NullCleaner.assertNotNull;

/**
 * A proxy for units in multiple maps.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ProxyUnit
		implements IUnit, ProxyFor<IUnit>, HasMutableKind, HasMutableImage,
						   HasMutableName, HasMutableOwner {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(ProxyUnit.class);
	/**
	 * Whether we are proxying parallel units in different maps.
	 */
	private final boolean parallel;
	/**
	 * The units we're a proxy for.
	 */
	private final Collection<IUnit> proxied = new ArrayList<>();
	/**
	 * The ID # of the units we are a proxy for.
	 */
	private final int id;
	/**
	 * The kind of the units we are a proxy for, if we're not proxying parallel units of
	 * the same ID.
	 */
	private String kind;

	/**
	 * Constructor.
	 *
	 * @param idNum the ID number of the units we are a proxy for.
	 */
	public ProxyUnit(final int idNum) {
		id = idNum;
		parallel = true;
		kind = "";
	}

	/**
	 * Constructor.
	 *
	 * @param uKind the kind of the units we are a proxy for.
	 */
	public ProxyUnit(final String uKind) {
		id = -1;
		parallel = false;
		kind = uKind;
	}

	/**
	 * TODO: This is probably highly inefficient, and is likely to get called often, I
	 * think
	 *
	 * @return the units' orders for all turns
	 */
	@Override
	public NavigableMap<Integer, String> getAllOrders() {
		final NavigableMap<Integer, String> retval = new TreeMap<>();
		final Collection<Integer> toRemove = new ArrayList<>();
		for (final IUnit unit : proxied) {
			for (final Map.Entry<Integer, String> entry : unit.getAllOrders()
																  .entrySet()) {
				if (retval.containsKey(entry.getKey())) {
					if (!retval.get(entry.getKey()).equals(entry.getValue())) {
						retval.put(entry.getKey(), "");
						toRemove.add(entry.getKey());
					}
				} else {
					retval.put(entry.getKey(), entry.getValue());
				}
			}
		}
		toRemove.forEach(retval::remove);
		return retval;
	}

	/**
	 * TODO: This is probably highly inefficient
	 *
	 * @return the units' orders for all turns
	 */
	@Override
	public NavigableMap<Integer, String> getAllResults() {
		final NavigableMap<Integer, String> retval = new TreeMap<>();
		final Collection<Integer> toRemove = new ArrayList<>();
		for (final IUnit unit : proxied) {
			for (final Map.Entry<Integer, String> entry : unit.getAllResults()
																  .entrySet()) {
				if (retval.containsKey(entry.getKey())) {
					if (!retval.get(entry.getKey()).equals(entry.getValue())) {
						retval.put(entry.getKey(), "");
						toRemove.add(entry.getKey());
					}
				} else {
					retval.put(entry.getKey(), entry.getValue());
				}
			}
		}
		toRemove.forEach(retval::remove);
		return retval;
	}

	/**
	 * @param item a unit to start proxying
	 */
	@SuppressWarnings("ObjectEquality")
	@Override
	public void addProxied(final IUnit item) {
		if (item == this) {
			return;
		} else if (parallel && (item.getID() != id)) {
			throw new IllegalArgumentException("Expected unit with ID #" + id);
		} else if (!parallel && !kind.equals(item.getKind())) {
			throw new IllegalArgumentException("Expected unit of kind " + kind);
		} else {
			proxied.add(item);
		}
	}

	/**
	 * @param zero whether to "zero out" sensitive information
	 * @return a copy of this proxy
	 */
	@Override
	public IUnit copy(final boolean zero) {
		final ProxyUnit retval;
		if (parallel) {
			retval = new ProxyUnit(id);
		} else {
			retval = new ProxyUnit(kind);
		}
		for (final IUnit unit : proxied) {
			retval.addProxied(unit.copy(zero));
		}
		return retval;
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
			return "a(n) " + getKind() + " unit belonging to you";
		} else if (getOwner().isIndependent()) {
			return "an independent " + getKind() + " unit";
		} else {
			return "a(n) " + getKind() + " unit belonging to " + getOwner().getName();
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
	 * @param fix a fixture
	 * @return the result of a comparison with it
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		LOGGER.warning("compareTo called on ProxyUnit");
		return IUnit.super.compareTo(fix);
	}

	/**
	 * @return the name of an image to represent the unit
	 */
	@Override
	public String getDefaultImage() {
		String retval = "";
		for (final IUnit unit : proxied) {
			if (retval.isEmpty()) {
				retval = unit.getDefaultImage();
			} else if (!retval.equals(unit.getDefaultImage())) {
				return "unit.png";
			}
		}
		return retval;
	}

	/**
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		@Nullable String image = null;
		for (final IUnit unit : proxied) {
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
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		LOGGER.warning("setImage() called on a ProxyUnit");
		for (final IUnit unit : proxied) {
			if (unit instanceof HasMutableImage) {
				((HasMutableImage) unit).setImage(img);
			} else {
				LOGGER.warning("setImage() skipped unit with immutable image");
			}
		}
	}

	/**
	 * @return the kind of the units
	 */
	@Override
	public String getKind() {
		if (parallel) {
			@Nullable String localKind = null;
			for (final IUnit unit : proxied) {
				if (localKind == null) {
					localKind = unit.getKind();
				} else if (!localKind.equals(unit.getKind())) {
					return "proxied";
				}
			}
			if (localKind == null) {
				return "proxied";
			} else {
				return localKind;
			}
		} else {
			return kind;
		}
	}

	/**
	 * TODO: If there's already a ProxyUnit for that kind, these should join it ...
	 *
	 * @param nKind the new kind of the proxied units
	 */
	@Override
	public void setKind(final String nKind) {
		if (!parallel) {
			kind = nKind;
		}
		for (final IUnit unit : proxied) {
			if (unit instanceof HasMutableKind) {
				((HasMutableKind) unit).setKind(nKind);
			} else {
				LOGGER.severe("ProxyUnit.setKind skipped unit with immutable kind");
			}
		}
	}

	/**
	 * @return an iterator over (proxies for) unit members
	 */
	@Override
	public Iterator<UnitMember> iterator() {
		if (!parallel) {
			return new EmptyIterator<>();
		} // else
		final Map<Integer, UnitMember> map = new TreeMap<>();
		for (final IUnit unit : proxied) {
			for (final UnitMember member : unit) {
				// Warning suppressed because the type in the map is really
				// a UnitMember&ProxyFor<IWorker|UnitMember>
				@SuppressWarnings("unchecked")
				final ProxyFor<? extends UnitMember> proxy;
				final Integer memberID =
						assertNotNull(Integer.valueOf(member.getID()));
				if (map.containsKey(memberID)) {
					//noinspection unchecked
					proxy = (ProxyFor<? extends UnitMember>) assertNotNull(
							map.get(memberID));
					if (proxy instanceof ProxyWorker) {
						if (member instanceof IWorker) {
							((ProxyWorker) proxy).addProxied((IWorker) member);
						} else {
							LOGGER.warning(
									"Proxy is a ProxyWorker but member isn't a " +
											"worker");

						}
					} else {
						//noinspection unchecked
						((ProxyFor<UnitMember>) proxy).addProxied(member);
					}
				} else {
					if (member instanceof IWorker) {
						//noinspection ObjectAllocationInLoop
						proxy = new ProxyWorker((IWorker) member);
					} else {
						//noinspection ObjectAllocationInLoop
						proxy = new ProxyMember(member);
					}
					map.put(memberID, (UnitMember) proxy);
				}
			}
		}
		return assertNotNull(map.values().iterator());
	}

	/**
	 * @return the name of the units (or "proxied" if they don't agree)
	 */
	@Override
	public String getName() {
		@Nullable String name = null;
		for (final IUnit unit : proxied) {
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
	 * @param newName the new name for the units
	 */
	@Override
	public void setName(final String newName) {
		for (final IUnit unit : proxied) {
			if (unit instanceof HasMutableName) {
				((HasMutableName) unit).setName(newName);
			} else {
				LOGGER.severe("ProxyUnit.setName skipped unit with immutable name");
			}
		}
	}

	/**
	 * @return the owner of the proxied units, or a dummy value if the proxied units are
	 * not all owned by the same player
	 */
	@Override
	public Player getOwner() {
		@Nullable Player retval = null;
		for (final IUnit unit : proxied) {
			if (retval == null) {
				retval = unit.getOwner();
			} else if (!retval.equals(unit.getOwner())) {
				return new Player(-1, "proxied");
			}
		}
		if (retval == null) {
			return new Player(-1, "proxied");
		} else {
			return retval;
		}
	}

	/**
	 * @param player the new owner for the units
	 */
	@Override
	public void setOwner(final Player player) {
		for (final IUnit unit : proxied) {
			if (unit instanceof HasMutableOwner) {
				((HasMutableOwner) unit).setOwner(player);
			} else {
				LOGGER.severe("ProxyUnit.setOwner skipped unit with immutable owner");
			}
		}
	}

	/**
	 * @param obj     ignored
	 * @param ostream the stream to write to
	 * @param context the context to write before writing our results
	 * @return false
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context) {
		ostream.format("%sCalled isSubset() in ProxyUnit%n", context);
		return IUnit.super.isSubset(obj, ostream, context + "\tIn proxy unit:");
	}

	/**
	 * @param turn the turn to get orders for
	 * @return the orders shared by the units, or the empty string if their orders are
	 * different.
	 */
	@Override
	public String getOrders(final int turn) {
		@Nullable String orders = null;
		for (final IUnit unit : proxied) {
			if (orders == null) {
				orders = unit.getOrders(turn);
			} else if (orders.isEmpty()) {
				continue;
			} else if (!orders.equals(unit.getOrders(turn))) {
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
	 * @param turn the turn to get results for
	 * @return the results shared by the units, or the empty string if their results are
	 * different.
	 */
	@Override
	public String getResults(final int turn) {
		@Nullable String results = null;
		for (final IUnit unit : proxied) {
			if (results == null) {
				results = unit.getResults(turn);
			} else if (results.isEmpty()) {
				continue;
			} else if (!results.equals(unit.getResults(turn))) {
				return "";
			}
		}
		if (results == null) {
			return "";
		} else {
			return results;
		}
	}

	/**
	 * @param turn the turn to set orders for
	 * @param newOrders The units' new orders for that turn
	 */
	@Override
	public void setOrders(final int turn, final String newOrders) {
		for (final IUnit unit : proxied) {
			unit.setOrders(turn, newOrders);
		}
	}

	/**
	 * @param turn       a turn
	 * @param newResults The units' new results for that turn
	 */
	@Override
	public void setResults(final int turn, final String newResults) {
		for (final IUnit unit : proxied) {
			unit.setResults(turn, newResults);
		}
	}

	/**
	 * @return a "verbose" description of the unit
	 */
	@Override
	public String verbose() {
		if (parallel) {
			final Optional<IUnit> first = proxied.stream().findFirst();
			return first.map(unitMembers ->
									 "A proxy for units in several maps, such as the " +
											 "following:" +
											 LineEnd.LINE_SEP + unitMembers.verbose())
						   .orElse("A proxy for units in several maps, but no units " +
										   "yet");
		} else {
			return "A proxy for units of kind " + kind;
		}
	}

	/**
	 * Add a member to a unit.
	 *
	 * @param member the member to add
	 */
	@Override
	public void addMember(final UnitMember member) {
		if (parallel) {
			for (final IUnit unit : proxied) {
				boolean shouldAdd = true;
				for (final UnitMember item : unit) {
					if (member.equals(item)) {
						shouldAdd = false;
						break;
					}
				}
				if (shouldAdd) {
					unit.addMember(member.copy(false));
				}
			}
		} else {
			LOGGER.severe("addMember() called on proxy for all units of one kind");
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
		if (parallel) {
			for (final IUnit unit : proxied) {
				for (final UnitMember item : unit) {
					if (member.equals(item)) {
						unit.removeMember(item);
						break;
					}
				}
			}
		} else {
			LOGGER.severe("removeMember() called on proxy for all units of one kind");
		}
	}

	/**
	 * @return the proxied units
	 */
	@Override
	public Iterable<IUnit> getProxied() {
		return new ArrayList<>(proxied);
	}

	/**
	 * @return a string representation of this class
	 */
	@Override
	public String toString() {
		if (parallel) {
			return assertNotNull(
					String.format("ProxyUnit for ID #%d", Integer.valueOf(id)));
		} else {
			return "ProxyUnit for units of kind " + kind;
		}
	}

	/**
	 * @param obj an object
	 * @return whether it's the same as this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof ProxyUnit) &&
										 (parallel == ((ProxyUnit) obj).parallel) &&
										 (id == ((ProxyUnit) obj).id) &&
										 kind.equals(((ProxyUnit) obj).kind) &&
										 proxied.equals(((ProxyUnit) obj).getProxied()));
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		final Iterator<IUnit> iter = proxied.iterator();
		if (iter.hasNext()) {
			return iter.next().hashCode();
		} else {
			return -1;
		}
	}

	/**
	 * @return Whether this should be considered (if true) a proxy for multiple
	 * representations of the same Unit, e.g. in different maps, or (if false) a proxy
	 * for
	 * different related Units.
	 */
	@Override
	public boolean isParallel() {
		return parallel;
	}

	/**
	 * A proxy for non-worker unit members.
	 */
	private static final class ProxyMember implements UnitMember, ProxyFor<UnitMember> {
		/**
		 * The proxied unit members.
		 */
		private final Collection<UnitMember> proxiedMembers = new ArrayList<>();

		/**
		 * No-arg no-op constructor for use in copy().
		 */
		private ProxyMember() {
			// do nothing
		}

		/**
		 * @param member the first member to proxy
		 */
		protected ProxyMember(final UnitMember member) {
			proxiedMembers.add(member);
		}

		/**
		 * @param zero whether to "zero out" sensitive information
		 * @return a copy of this proxy
		 */
		@SuppressWarnings("MethodReturnOfConcreteClass")
		@Override
		public ProxyMember copy(final boolean zero) {
			final ProxyMember retval = new ProxyMember();
			for (final UnitMember member : proxiedMembers) {
				retval.addProxied(member.copy(zero));
			}
			return retval;
		}

		/**
		 * @return the ID number of the first proxied unit member (since they should all
		 * have the same, in the only usage of this class)
		 */
		@Override
		public int getID() {
			final Iterator<UnitMember> iter = proxiedMembers.iterator();
			if (iter.hasNext()) {
				return iter.next().getID();
			} else {
				return -1;
			}
		}

		/**
		 * @param fix a fixture
		 * @return whether it equals this one
		 */
		@Override
		public boolean equalsIgnoringID(final IFixture fix) {
			return (fix instanceof ProxyMember) &&
						   ((ProxyMember) fix).proxiedMembers.equals(proxiedMembers);
		}

		/**
		 * @param obj     ignored
		 * @param ostream the stream to write to
		 * @param context the context to write before we write our error
		 * @return false
		 * @throws IOException never, required by interface
		 */
		@Override
		public boolean isSubset(final IFixture obj, final Formatter ostream,
								final String context) {
			ostream.format("%sisSubset called on ProxyMember%n", context);
			return false;
		}

		/**
		 * Add an item to be proxied.
		 *
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
			return new ArrayList<>(proxiedMembers);
		}

		/**
		 * @return a string representation of the proxied member
		 */
		@Override
		public String toString() {
			final Iterator<UnitMember> iter = proxiedMembers.iterator();
			if (iter.hasNext()) {
				return assertNotNull(iter.next().toString());
			} else {
				return "a proxy for no unit members";
			}
		}

		/**
		 * @return Whether this should be considered (if true) a proxy for multiple
		 * representations of the same UnitMember, e.g. in different maps, or (if
		 * false) a
		 * proxy for different related UnitMembers.
		 */
		@Override
		public boolean isParallel() {
			return true;
		}
	}
}
