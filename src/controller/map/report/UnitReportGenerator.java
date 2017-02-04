package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.tree.MutableTreeNode;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.FortressMember;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.WorkerStats;
import model.report.ComplexReportNode;
import model.report.EmptyReportNode;
import model.report.IReportNode;
import model.report.ListReportNode;
import model.report.SectionListReportNode;
import model.report.SectionReportNode;
import model.report.SimpleReportNode;
import org.eclipse.jdt.annotation.NonNull;
import util.Pair;
import util.PatientMap;

import static model.map.fixtures.mobile.worker.WorkerStats.getModifierString;

/**
 * A report generator for units.
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
public final class UnitReportGenerator extends AbstractReportGenerator<IUnit> {
	/**
	 * A string to indicate a worker has training or experience.
	 */
	private static final String HAS_TRAINING =
			"(S)he has training or experience in the following Jobs (Skills):";
	/**
	 * Instance we use.
	 */
	private final IReportGenerator<FortressMember> memberReportGenerator =
			new FortressMemberReportGenerator(pairComparator);
	/**
	 * Instance we use.
	 */
	private final IReportGenerator<Animal> animalReportGenerator =
			new AnimalReportGenerator(pairComparator);
	/**
	 * Constructor.
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public UnitReportGenerator(final Comparator<@NonNull Pair<@NonNull Point, @NonNull
																					  IFixture>> comparator) {
		super(comparator);
	}

	/**
	 * Write the report on a Worker to a Formatter.
	 * @param ostream the Formatter to write to
	 * @param worker  a Worker.
	 * @param details whether we should give details of the worker's stats and
	 *                experience---true only if the current player owns the worker.
	 */
	private static void workerReport(final IWorker worker, final boolean details,
									   final Formatter ostream) {
		final WorkerStats stats = worker.getStats();
		ostream.format("%s, a %s.", worker.getName(), worker.getRace());
		if ((stats != null) && details) {
			ostream.format("%n<p>He or she has the following stats: ");
			//noinspection HardcodedFileSeparator
			ostream.format("%d / %d Hit Points, Strength %s, Dexterity %s, ",
					stats.getHitPoints(), stats.getMaxHitPoints(),
					getModifierString(stats.getStrength()),
					getModifierString(stats.getDexterity()));
			ostream.format("Constitution %s, Intelligence %s, Wisdom %s, ",
					getModifierString(stats.getConstitution()),
					getModifierString(stats.getIntelligence()),
					getModifierString(stats.getWisdom()));
			ostream.format("Charisma %s</p>%n",
					getModifierString(stats.getCharisma()));
		}
		if (worker.iterator().hasNext() && details) {
			ostream.format("%s%n<ul>%n", HAS_TRAINING);
			for (final IJob job : worker) {
				ostream.format("<li>%d levels in %s",
						Integer.valueOf(job.getLevel()), job.getName());
				writeSkills(job, ostream);
				ostream.format("</li>%n");
			}
			ostream.format("</ul>%n");
		}
	}

	/**
	 * Write text describing the Job's Skills to the given Formatter.
	 * @param job a Job
	 * @param formatter the Formatter to write to
	 */
	private static void writeSkills(final Iterable<ISkill> job,
									  final Formatter formatter) {
		if (job.iterator().hasNext()) {
			boolean first = true;
			for (final ISkill skill : job) {
				if (first) {
					formatter.format(" (");
					first = false;
				} else {
					formatter.format(", ");
				}
				formatter.format("%s %d", skill.getName(),
						Integer.valueOf(skill.getLevel()));
			}
			formatter.format(")");
		}
	}
	/**
	 * Produce a String describing the Job's Skills.
	 * @param job a Job
	 * @return a String describing its skills.
	 */
	private static String getSkills(final Iterable<ISkill> job) {
		final StringBuilder builder = new StringBuilder(512);
		try (final Formatter formatter = new Formatter(builder)) {
			writeSkills(job, formatter);
		}
		return builder.toString();
	}

	/**
	 * Produce the RIR sub-report on a worker.
	 * @param loc     the location of the worker in the map
	 * @param worker  a Worker.
	 * @param details whether we should give details of the worker's stats and
	 *                experience---true only if the current player owns the worker.
	 * @return a sub-report on that worker.
	 */
	private static MutableTreeNode produceWorkerRIR(final Point loc,
													final IWorker worker,
													final boolean details) {
		final IReportNode retval = new ComplexReportNode(loc, String.format("%s, a %s.",
				worker.getName(), worker.getRace()));
		final WorkerStats stats = worker.getStats();
		if ((stats != null) && details) {
			final StringBuilder builder = new StringBuilder(153);
			try (final Formatter formatter = new Formatter(builder)) {
				//noinspection HardcodedFileSeparator
				formatter.format("He or she has the following stats: %d / %d, ",
						Integer.valueOf(stats.getHitPoints()),
						Integer.valueOf(stats.getMaxHitPoints()));
				formatter.format("Strength %s, Dexterity %s, Constitution %s, ",
						getModifierString(stats.getStrength()),
						getModifierString(stats.getDexterity()),
						getModifierString(stats.getConstitution()));
				formatter.format("Intelligence %s, Wisdom %s, Charisma %s",
						getModifierString(stats.getIntelligence()),
						getModifierString(stats.getWisdom()),
						getModifierString(stats.getCharisma()));
			}
			retval.add(new SimpleReportNode(loc, builder.toString()));
		}
		if (worker.iterator().hasNext() && details) {
			final IReportNode jobs = new ListReportNode(loc, HAS_TRAINING);
			for (final IJob job : worker) {
				jobs.add(produceJobRIR(job, loc));
			}
			retval.add(jobs);
		}
		return retval;
	}

	/**
	 * Produce the RIR sub-sub-report on a Job.
	 * @param loc the location of the worker in the map
	 * @param job a Job
	 * @return a sub-report on that Job.
	 */
	private static MutableTreeNode produceJobRIR(final IJob job, final Point loc) {
		return new SimpleReportNode(loc, Integer.toString(job.getLevel()),
										   " levels in ", job.getName(), getSkills(job));
	}
	/**
	 * If the collection is nonempty, append its sub-sub-report to the stream.
	 * @param <T> the type of member in the collection
	 * @param fixtures      the set of fixtures
	 * @param collection	the collection of members in question
	 * @param heading		the heading to use
	 * @param generator		the report generator to use for members in the collection
	 * @param formatter		the Formatter to write the report to
	 */
	private static <T extends UnitMember> void produceInner(final Formatter formatter,
													 final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
													 final Collection<T> collection,
													 final String heading,
													 final Consumer<? super T> generator) {
		if (!collection.isEmpty()) {
			formatter.format("<li>%s%n<ul>%n", heading);
			for (final T item : collection) {
				formatter.format("%s", OPEN_LIST_ITEM);
				generator.accept(item);
				formatter.format("</li>%n");
				fixtures.remove(item.getID());
			}
			formatter.format("</ul>%n</li>%n");
		}
	}
	/**
	 * Produce a sub-sub-report on a unit. We assume we're already in the middle of a paragraph or bullet point.
	 *
	 * @param fixtures      the set of fixtures, so we can remove the unit and its
	 *                         members from it.
	 * @param map           ignored
	 * @param item          a unit
	 * @param loc           the unit's location
	 * @param currentPlayer the player for whom the report is being produced
	 * @param ostream	    the Formatter to write to
	 */
	@Override
	public void produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						final IMapNG map, final Player currentPlayer,
						final IUnit item, final Point loc, final Formatter ostream) {
		ostream.format("Unit of type %s, named %s, ", item.getKind(),
				item.getName());
		if (item.getOwner().isIndependent()) {
			ostream.format("independent");
		} else {
			ostream.format("owned by %s", playerNameOrYou(item.getOwner()));
		}
		final Collection<IWorker> workers = new ArrayList<>();
		final Collection<Implement> equipment = new ArrayList<>();
		final Collection<ResourcePile> resources = new ArrayList<>();
		final Collection<Animal> animals = new ArrayList<>();
		final Collection<UnitMember> others = new ArrayList<>();
		boolean hasMembers = false;
		for (final UnitMember member : item) {
			hasMembers = true;
			if (member instanceof IWorker) {
				workers.add((IWorker) member);
			} else if (member instanceof Implement) {
				equipment.add((Implement) member);
			} else if (member instanceof ResourcePile) {
				resources.add((ResourcePile) member);
			} else if (member instanceof Animal) {
				animals.add((Animal) member);
			} else {
				others.add(member);
			}
		}
		if (hasMembers) {
			ostream.format(". Members of the unit:%n<ul>%n");
		}
		produceInner(ostream, fixtures, workers, "Workers:",
				worker -> workerReport(worker, worker instanceof HasOwner &&
													   Objects.equals(currentPlayer,
															   ((HasOwner) worker)
																	   .getOwner()),
						ostream));

		produceInner(ostream, fixtures, animals, "Animals:",
				animal -> animalReportGenerator
								  .produce(fixtures, map, currentPlayer, animal,
										  loc, ostream));
		produceInner(ostream, fixtures, equipment, "Equipment:",
				member -> memberReportGenerator
								  .produce(fixtures, map, currentPlayer, member,
										  loc, ostream));
		produceInner(ostream, fixtures, resources, "Resources:",
				member -> memberReportGenerator
								  .produce(fixtures, map, currentPlayer, member,
										  loc, ostream));
		produceInner(ostream, fixtures, resources, "Others:",
				obj -> ostream.format("%s", obj.toString()));
		if (hasMembers) {
			ostream.format("</ul>%n");
		}
		produceOrders(item, ostream);
		fixtures.remove(Integer.valueOf(item.getID()));
	}

	/**
	 * Produce the sub-sub-report about a unit's orders and results.
	 * @param item the unit
	 * @param formatter the Formatter to write to
	 */
	private static void produceOrders(final IUnit item, final Formatter formatter) {
		if (!item.getAllOrders().isEmpty() || !item.getAllResults().isEmpty()) {
			formatter.format("Orders and Results:<ul>%n");
			final Collection<Integer> turns =
					new TreeSet<>(item.getAllOrders().keySet());
			turns.addAll(item.getAllResults().keySet());
			for (final Integer turn : turns) {
				formatter.format("<li>Turn %d:<ul>", turn);
				final String orders = item.getOrders(turn);
				if (!orders.isEmpty()) {
					formatter.format("<li>Orders: %s</li>%n", orders);
				}
				final String results = item.getResults(turn);
				if (!results.isEmpty()) {
					formatter.format("<li>Results: %s</li>%n", results);
				}
				formatter.format("</ul>%n</li>%n");
			}
			formatter.format("</ul>%n");
		}
	}

	/**
	 * We assume we're already in the middle of a paragraph or bullet point.
	 *
	 * @param fixtures      the set of fixtures, so we can remove the unit and its
	 *                         members
	 *                      from it.
	 * @param map           ignored
	 * @param item          a unit
	 * @param loc           the unit's location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return a sub-report on the unit
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
										  fixtures,
								  final IMapNG map, final Player currentPlayer,
								  final IUnit item, final Point loc) {
		final String simple;
		if (item.getOwner().isIndependent()) {
			simple = concat("Unit of type ", item.getKind(), ", named ",
					item.getName(), ", independent");
		} else {
			simple = concat("Unit of type ", item.getKind(), ", named ",
					item.getName(),
					", owned by " + playerNameOrYou(item.getOwner()));
		}
		fixtures.remove(Integer.valueOf(item.getID()));
		final ListReportNode workers = new ListReportNode("Workers:");
		final ListReportNode animals = new ListReportNode("Animals:");
		final ListReportNode equipment = new ListReportNode("Equipment:");
		final ListReportNode resources = new ListReportNode("Resources:");
		final ListReportNode others = new ListReportNode("Others:");
		final IReportNode retval =
				new ListReportNode(loc, concat(simple, ". Members of the unit:"));
		for (final UnitMember member : item) {
			if (member instanceof IWorker) {
				workers.add(produceWorkerRIR(loc, (IWorker) member,
						currentPlayer.equals(item.getOwner())));
			} else if (member instanceof Animal) {
				animals.add(animalReportGenerator
									.produceRIR(fixtures, map, currentPlayer,
											(Animal) member, loc));
			} else if (member instanceof Implement) {
				equipment.add(memberReportGenerator
									  .produceRIR(fixtures, map, currentPlayer,
											  (Implement) member, loc));
			} else if (member instanceof ResourcePile) {
				resources.add(memberReportGenerator
									  .produceRIR(fixtures, map, currentPlayer,
											  (ResourcePile) member, loc));
			} else {
				//noinspection ObjectAllocationInLoop
				others.add(new SimpleReportNode(loc, member.toString()));
			}
			fixtures.remove(Integer.valueOf(member.getID()));
		}
		retval.addIfNonEmpty(workers, animals, equipment, resources, others);
		final Collection<Integer> turns = new TreeSet<>(item.getAllOrders().keySet());
		turns.addAll(item.getAllResults().keySet());
		final ListReportNode ordersNode = new ListReportNode("Orders and Results:");
		for (final Integer turn : turns) {
			final ListReportNode current = new ListReportNode("Turn " + turn + ':');
			final String orders = item.getOrders(turn);
			if (!orders.isEmpty()) {
				current.add(new SimpleReportNode("Orders: ", orders));
			}
			final String results = item.getResults(turn);
			if (!results.isEmpty()) {
				current.add(new SimpleReportNode("Results: ", results));
			}
			ordersNode.addIfNonEmpty(current);
		}
		retval.addIfNonEmpty(ordersNode);
		if (retval.getChildCount() == 0) {
			return new SimpleReportNode(loc, simple);
		} else {
			return retval;
		}
	}

	/**
	 * Produce the part of the report dealing with units. All fixtures referred to in
	 * this report are removed from the collection.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @param ostream       the Formatter to write to
	 */
	@Override
	public void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
						Player currentPlayer, final Formatter ostream) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		final HeadedMap<IUnit, Point> foreign =
				new HeadedMapImpl<>("<h5>Foreign units</h5>");
		final HeadedMap<IUnit, Point> ours = new HeadedMapImpl<>("<h5>Your units</h5>");
		for (final Pair<Point, IFixture> pair : values) {
			if (pair.second() instanceof IUnit) {
				final IUnit unit = (IUnit) pair.second();
				if (currentPlayer.equals(unit.getOwner())) {
					ours.put(unit, pair.first());
				} else {
					foreign.put(unit, pair.first());
				}
			}
		}
		if (!ours.isEmpty() && !foreign.isEmpty()) {
			ostream.format("<h4>Units in the map</h4>%n<p>Any units listed above are ");
			ostream.format("not described again.)</p>%n");
			final BiConsumer<Entry<IUnit, Point>, Formatter> unitFormatter =
					(entry, formatter) -> {
						formatter.format("At %s%s", entry.getValue().toString(),
								distCalculator.distanceString(entry.getValue()));
						produce(fixtures, map, currentPlayer, entry.getKey(),
								entry.getValue(), formatter);
					};
			writeMap(ostream, ours, unitFormatter);
			writeMap(ostream, foreign, unitFormatter);
		}
	}

	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report dealing with units
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
										  fixtures,
								  final IMapNG map, final Player currentPlayer) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		final IReportNode theirs = new SectionListReportNode(5, "Foreign units");
		final IReportNode ours = new SectionListReportNode(5, "Your units");
		values.stream().filter(pair -> pair.second() instanceof IUnit).forEach(pair -> {
			final IUnit unit = (IUnit) pair.second();
			final IReportNode unitNode = produceRIR(fixtures, map,
					currentPlayer, unit, pair.first());
			unitNode.setText(concat(atPoint(pair.first()), unitNode.getText(), " ",
					distCalculator.distanceString(pair.first())));
			if (currentPlayer.equals(unit.getOwner())) {
				ours.add(unitNode);
			} else {
				theirs.add(unitNode);
			}
		});
		final MutableTreeNode textNode =
				new SimpleReportNode("(Any units reported above are not described again" +
											 ".)");
		if (ours.getChildCount() == 0) {
			if (theirs.getChildCount() == 0) {
				return EmptyReportNode.NULL_NODE;
			} else {
				theirs.addAsFirst(textNode);
				theirs.setText("Foreign units in the map:");
				return theirs;
			}
		} else if (theirs.getChildCount() == 0) {
			ours.addAsFirst(textNode);
			ours.setText("Your units in the map:");
			return ours;
		} else {
			final IReportNode retval = new SectionReportNode(4, "Units in the map");
			retval.add(textNode);
			retval.add(ours);
			retval.add(theirs);
			return retval;
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "UnitReportGenerator";
	}
}
