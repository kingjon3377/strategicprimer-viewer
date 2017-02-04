package controller.map.misc;

import controller.map.drivers.SPOptions;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import model.listeners.PlayerChangeListener;
import model.map.HasName;
import model.map.Player;
import model.map.PlayerImpl;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.worker.IJob;
import model.workermgmt.IWorkerModel;
import org.eclipse.jdt.annotation.Nullable;
import util.MultiMapHelper;
import util.TypesafeLogger;

import static java.util.logging.Level.SEVERE;

/**
 * A class to export a "proto-strategy" to file.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class StrategyExporter implements PlayerChangeListener {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(StrategyExporter
																		  .class);
	/**
	 * The worker model.
	 */
	private final IWorkerModel model;
	/**
	 * The current player.
	 */
	private Player currentPlayer;

	/**
	 * Constructor.
	 *
	 * @param workerModel the driver model to draw from
	 */
	public StrategyExporter(final IWorkerModel workerModel) {
		model = workerModel;
		currentPlayer = workerModel.getMap().getCurrentPlayer();
	}

	/**
	 * The size of the string needed to represent the unit's members.
	 * @param unit a unit
	 * @return the size of string needed to represent its members
	 */
	private static int unitMemberSize(final Iterable<UnitMember> unit) {
		if (unit.iterator().hasNext()) {
			int size = 3;
			for (final UnitMember member : unit) {
				size += 2;
				size += memberStringSize(member);
			}
			return size;
		} else {
			return 0;
		}
	}

	/**
	 * Write the unit's members to a Formatter.
	 * @param unit a unit
	 * @param formatter a Formatter to write to
	 */
	private static void writeUnitMembers(final Iterable<UnitMember> unit,
										 final Formatter formatter) {
		if (unit.iterator().hasNext()) {
			boolean first = true;
			for (final UnitMember member : unit) {
				if (first) {
					formatter.format(" [");
					first = false;
				} else {
					formatter.format(", ");
				}
				writeMember(member, formatter);
			}
			formatter.format("]");
		}
	}

	/**
	 * The number of characters needed to write the given member.
	 * @param member a unit member
	 * @return the size of a string for it
	 */
	private static int memberStringSize(final UnitMember member) {
		if (member instanceof IWorker) {
			int size = ((IWorker) member).getName().length();
			size += 2;
			for (final IJob job : (IWorker) member) {
				size += 3;
				size += job.getName().length();
				size += Integer.toString(job.getLevel()).length();
			}
			return size;
		} else {
			return member.toString().length();
		}
	}

	/**
	 * Write a unit member to a Formatter.
	 * @param member a unit member
	 * @param formatter a Formatter to write it to
	 */
	private static void writeMember(final UnitMember member, final Formatter formatter) {
		if (member instanceof IWorker) {
			final IWorker worker = (IWorker) member;
			formatter.format("%s", worker.getName());
			if (worker.iterator().hasNext()) {
				formatter.format(" (");
				boolean first = true;
				for (final IJob job : worker) {
					final String fmt;
					if (first) {
						fmt = " (%s %d";
						first = false;
					} else {
						fmt = ", %s %d";
					}
					formatter.format(fmt, job.getName(),
							Integer.valueOf(job.getLevel()));
				}
				formatter.format(")");
			}
		} else {
			formatter.format("%s", member.toString());
		}
	}

	/**
	 * Create the proto-strategy.
	 * @param dismissed the list of dismissed members
	 * @param options command-line options that were passed to the app
	 * @return the proto-strategy as a String
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	public String createStrategy(final SPOptions options,
								 final Iterable<UnitMember> dismissed) {
		final String playerName = currentPlayer.getName();
		final int turn = model.getMap().getCurrentTurn();
		final List<IUnit> units = model.getUnits(currentPlayer);

		final Map<String, List<IUnit>> unitsByKind = new HashMap<>();
		for (final IUnit unit : units) {
			if (!unit.iterator().hasNext() &&
						"false".equals(options.getArgument("--print-empty"))) {
				continue;
			}
			MultiMapHelper
					.getMapValue(unitsByKind, unit.getKind(), key -> new ArrayList<>())
					.add(unit);
		}

		final Map<IUnit, String> orders = new HashMap<>();
		for (final List<IUnit> list : unitsByKind.values()) {
			for (final IUnit unit : list) {
				final String tempOrders = unit.getLatestOrders(turn);
				final String unitOrders;
				if (tempOrders.equals(unit.getOrders(turn))) {
					unitOrders = tempOrders;
				} else {
					unitOrders = "(From turn #" + unit.getOrdersTurn(tempOrders) + ") " +
										 tempOrders;
				}
				orders.put(unit, unitOrders);
			}
		}
		final String turnString = Integer.toString(turn);
		final StringBuilder builder =
				new StringBuilder(getBufferSize(dismissed, playerName, turnString,
						unitsByKind, orders));
		final Formatter formatter = new Formatter(builder);
		formatter.format("[%s%nTurn %s]%n%nInventions: TODO: any?%n%n", playerName,
				turnString);
		if (dismissed.iterator().hasNext()) {
			builder.append("Dismissed workers etc.: ");
			String separator = "";
			for (final UnitMember member : dismissed) {
				builder.append(separator);
				separator = ", ";
				if (member instanceof HasName) {
					builder.append(((HasName) member).getName());
				} else {
					builder.append(member);
				}
			}
			formatter.format("%n%n");
		}
		formatter.format("Workers:%n");
		for (final Map.Entry<String, List<IUnit>> entry : unitsByKind.entrySet()) {
			formatter.format("* %s:%n", entry.getKey());
			for (final IUnit unit : entry.getValue()) {
				formatter.format("  - %s", unit.getName());
				writeUnitMembers(unit, formatter);
				final String unitOrders = orders.get(unit);
				if (unitOrders.isEmpty()) {
					formatter.format(":%n%nTODO%n%n");
				} else {
					formatter.format(":%n%n%s%n%n", unitOrders);
				}
			}
		}
		return builder.toString();
	}

	/**
	 * Calculate the needed buffer size.
	 * @param dismissed the list of dismissed workers
	 * @param playerName the name of the current player
	 * @param turnString the current turn, as a String
	 * @param unitsByKind the collection of lists of units
	 * @param orders the units' current orders
	 * @return how big a buffer we need
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	private static int getBufferSize(final Iterable<UnitMember> dismissed,
									 final String playerName, final String turnString,
									 final Map<String, List<IUnit>> unitsByKind,
									 final Map<IUnit, String> orders) {
		int size = 58 + playerName.length() + turnString.length();
		for (final Map.Entry<String, List<IUnit>> entry : unitsByKind.entrySet()) {
			size += 4;
			size += entry.getKey().length();
			for (final IUnit unit : entry.getValue()) {
				size += 10;
				size += unit.getName().length();
				size += unitMemberSize(unit);
				size += orders.get(unit).length();
			}
		}
		for (final UnitMember member : dismissed) {
			size += 2;
			if (member instanceof HasName) {
				size += ((HasName) member).getName().length();
			} else {
				size += member.toString().length();
			}
		}
		return size;
	}

	/**
	 * Write the strategy to file.
	 *
	 * @param dismissed the list of dismissed members
	 * @param options   options passed to the driver
	 * @param file      a file (name) to write to
	 */
	public void writeStrategy(final Path file, final SPOptions options,
							  final Iterable<UnitMember> dismissed) {
		try (final BufferedWriter writer = Files.newBufferedWriter(file)) {
			writer.append(createStrategy(options, dismissed));
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			LOGGER.log(SEVERE, "I/O error exporting strategy", except);
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "StrategyExporter";
	}

	/**
	 * Handle a change in the current player.
	 * @param old       the previous current player
	 * @param newPlayer the new current player
	 */
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		currentPlayer = newPlayer;
	}
}
