package view.worker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import model.listeners.PlayerChangeListener;
import model.map.HasName;
import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.worker.IJob;
import model.workermgmt.IWorkerModel;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;
import util.TypesafeLogger;

import static java.util.logging.Level.SEVERE;

/**
 * A class to export a "proto-strategy" to file.
 *
 * FIXME: This belongs in the controller.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
public final class StrategyExporter implements PlayerChangeListener {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(StrategyExporter.class);
	/**
	 * The system's line-separator. TODO: Make a central location for this.
	 */
	private static final String LINE_SEP = System.lineSeparator();
	/**
	 * The current player.
	 */
	private Player currentPlayer;
	/**
	 * The worker model.
	 */
	private final IWorkerModel model;

	/**
	 * Constructor.
	 *
	 * @param workerModel    the driver model to draw from
	 */
	public StrategyExporter(final IWorkerModel workerModel) {
		model = workerModel;
		currentPlayer = workerModel.getMap().getCurrentPlayer();
	}

	/**
	 * @return the proto-strategy as a String
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	public String createStrategy(final Iterable<UnitMember> dismissed) {
		final String playerName = currentPlayer.getName();
		final String turn = Integer.toString(model.getMap().getCurrentTurn());
		final List<IUnit> units = model.getUnits(currentPlayer);

		final Map<String, List<IUnit>> unitsByKind = new HashMap<>();
		for (final IUnit unit : units) {
			if (!unit.iterator().hasNext()) {
				// FIXME: This should be exposed as a user option. Sometimes
				// users *want* empty units printed.
				continue;
			}
			final List<IUnit> list;
			if (unitsByKind.containsKey(unit.getKind())) {
				list = NullCleaner.assertNotNull(unitsByKind.get(unit.getKind()));
			} else {
				//noinspection ObjectAllocationInLoop
				list = new ArrayList<>();
				unitsByKind.put(unit.getKind(), list);
			}
			list.add(unit);
		}

		int size = 58 + playerName.length() + turn.length();
		for (final Map.Entry<String, List<IUnit>> entry : unitsByKind.entrySet()) {
			size += 4;
			size += entry.getKey().length();
			for (final IUnit unit : entry.getValue()) {
				size += 10;
				size += unit.getName().length();
				size += unitMemberSize(unit);
				size += unit.getOrders().length();
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
		final StringBuilder builder = new StringBuilder(size);
		builder.append('[');
		builder.append(playerName);
		builder.append(LINE_SEP);
		builder.append("Turn ");
		builder.append(turn);
		builder.append(']');
		builder.append(LINE_SEP);
		builder.append(LINE_SEP);
		builder.append("Inventions: TODO: any?");
		builder.append(LINE_SEP);
		builder.append(LINE_SEP);
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
			builder.append(LINE_SEP);
			builder.append(LINE_SEP);
		}
		builder.append("Workers:");
		builder.append(LINE_SEP);
		for (final Map.Entry<String, List<IUnit>> entry : unitsByKind.entrySet()) {
			builder.append("* ");
			builder.append(entry.getKey());
			builder.append(':');
			builder.append(LINE_SEP);
			for (final IUnit unit : entry.getValue()) {
				builder.append("  - ");
				builder.append(unit.getName());
				builder.append(unitMembers(unit));
				builder.append(':');
				builder.append(LINE_SEP);
				builder.append(LINE_SEP);
				final String orders = unit.getOrders().trim();
				if (orders.isEmpty()) {
					builder.append("TODO");
				} else {
					builder.append(orders);
				}
				builder.append(LINE_SEP);
				builder.append(LINE_SEP);
			}
		}
		return NullCleaner.assertNotNull(builder.toString());
	}
	/**
	 * Write the strategy to file.
	 * @param file a file (name) to write to
	 */
	public void writeStrategy(final Path file, final Iterable<UnitMember> dismissed) {
		try (final BufferedWriter writer = Files.newBufferedWriter(file)) {
			//noinspection resource
			writer.append(createStrategy(dismissed));
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			LOGGER.log(SEVERE, "I/O error exporting strategy", except);
		}
	}
	/**
	 * @param unit a unit
	 * @return the size of string needed to represent its members
	 */
	private static int unitMemberSize(final Iterable<UnitMember> unit) {
		if (unit.iterator().hasNext()) {
			int size = 3;
			for (final UnitMember member : unit) {
				size += 2;
				size += memberStringSize(NullCleaner.assertNotNull(member));
			}
			return size;
		} else {
			return 0;
		}
	}

	/**
	 * @param unit a unit
	 * @return a String representing its members
	 */
	private static String unitMembers(final Iterable<UnitMember> unit) {
		if (unit.iterator().hasNext()) {
			// Assume at least two K.
			final StringBuilder builder = new StringBuilder(2048).append(" [");
			boolean first = true;
			for (final UnitMember member : unit) {
				if (first) {
					first = false;
				} else {
					builder.append(", ");
				}
				builder.append(memberString(member));
			}
			builder.append(']');
			return NullCleaner.assertNotNull(builder.toString());
		} else {
			return "";
		}
	}

	/**
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
	 * @param member a unit member
	 * @return a suitable string for it
	 */
	private static String memberString(final UnitMember member) {
		if (member instanceof IWorker) {
			final IWorker worker = (IWorker) member;
			// To save calculations, assume a half-K every time.
			final StringBuilder builder =
					new StringBuilder(512).append(worker.getName());
			if (worker.iterator().hasNext()) {
				builder.append(" (");
				boolean first = true;
				for (final IJob job : worker) {
					if (first) {
						first = false;
					} else {
						builder.append(", ");
					}
					builder.append(job.getName());
					builder.append(' ');
					builder.append(job.getLevel());
				}
				builder.append(')');
			}
			return NullCleaner.assertNotNull(builder.toString());
		} else {
			return NullCleaner.assertNotNull(member.toString());
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "StrategyExporter";
	}

	/**
	 * @param old       the previous current player
	 * @param newPlayer the new current player
	 */
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		currentPlayer = newPlayer;
	}
}
