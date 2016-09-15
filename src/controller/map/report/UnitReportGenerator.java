package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.WorkerStats;
import model.report.ComplexReportNode;
import model.report.EmptyReportNode;
import model.report.IReportNode;
import model.report.ListReportNode;
import model.report.SectionListReportNode;
import model.report.SectionReportNode;
import model.report.SimpleReportNode;
import org.eclipse.jdt.annotation.NonNull;
import util.LineEnd;
import util.NullCleaner;
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
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public UnitReportGenerator(final Comparator<@NonNull Pair<@NonNull Point, @NonNull
																					IFixture>> comparator) {
		super(comparator);
	}

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
	 * A string to indicate a worker has training or experience.
	 */
	private static final String HAS_TRAINING =
			"(S)he has training or experience in the following Jobs (Skills):";

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
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer, final IUnit item,
						  final Point loc) {
		final StringBuilder builder =
				new StringBuilder(52 + item.getKind().length() + item.getName().length() +
										item.getOwner().getName().length());
		builder.append("Unit of type ");
		builder.append(item.getKind());
		builder.append(", named ");
		builder.append(item.getName());
		if (item.getOwner().isIndependent()) {
			builder.append(", independent");
		} else {
			builder.append(", owned by ");
			builder.append(playerNameOrYou(item.getOwner()));
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
			builder.append(". Members of the unit:").append(LineEnd.LINE_SEP)
					.append(OPEN_LIST);
		}
		if (!workers.isEmpty()) {
			builder.append(OPEN_LIST_ITEM).append("Workers:").append(LineEnd.LINE_SEP)
					.append(OPEN_LIST);
			for (final IWorker worker : workers) {
				builder.append(OPEN_LIST_ITEM).append(workerReport(worker,
						worker instanceof HasOwner &&
								currentPlayer.equals(((HasOwner) worker).getOwner())))
						.append(CLOSE_LIST_ITEM);
				fixtures.remove(worker.getID());
			}
			builder.append(CLOSE_LIST).append(CLOSE_LIST_ITEM);
		}
		if (!animals.isEmpty()) {
			builder.append(OPEN_LIST_ITEM).append("Animals:").append(LineEnd.LINE_SEP)
					.append(OPEN_LIST);
			for (final Animal animal : animals) {
				builder.append(OPEN_LIST_ITEM).append(animalReportGenerator
															  .produce(fixtures, map,
																	  currentPlayer,
																	  animal, loc));
				fixtures.remove(animal.getID());
			}
			builder.append(CLOSE_LIST).append(CLOSE_LIST_ITEM);
		}
		if (!equipment.isEmpty()) {
			builder.append(OPEN_LIST_ITEM).append("Equipment:").append(LineEnd.LINE_SEP)
					.append(OPEN_LIST);
			for (final Implement implement : equipment) {
				builder.append(OPEN_LIST_ITEM).append(memberReportGenerator
															  .produce(fixtures, map,
																	  currentPlayer,
																	  implement, loc))
						.append(CLOSE_LIST_ITEM);
				fixtures.remove(implement.getID());
			}
			builder.append(CLOSE_LIST).append(CLOSE_LIST_ITEM);
		}
		if (!resources.isEmpty()) {
			builder.append(OPEN_LIST_ITEM).append("Resources:").append(LineEnd.LINE_SEP)
					.append(OPEN_LIST);
			for (final ResourcePile resource : resources) {
				builder.append(OPEN_LIST_ITEM).append(memberReportGenerator
															  .produce(fixtures, map,
																	  currentPlayer,
																	  resource, loc))
						.append(CLOSE_LIST_ITEM);
				fixtures.remove(resource.getID());
			}
			builder.append(CLOSE_LIST).append(CLOSE_LIST_ITEM);
		}
		if (!others.isEmpty()) {
			builder.append(OPEN_LIST_ITEM).append("Others:").append(LineEnd.LINE_SEP)
					.append(OPEN_LIST);
			for (final UnitMember member : others) {
				builder.append(OPEN_LIST_ITEM).append(member);
				fixtures.remove(member.getID());
			}
			builder.append(CLOSE_LIST).append(CLOSE_LIST_ITEM);
		}
		if (hasMembers) {
			builder.append(CLOSE_LIST);
		}
		fixtures.remove(Integer.valueOf(item.getID()));
		return NullCleaner.assertNotNull(builder.toString());
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
		final ListReportNode workers = new ListReportNode("Workers:");
		final ListReportNode animals = new ListReportNode("Animals:");
		final ListReportNode equipment = new ListReportNode("Equipment:");
		final ListReportNode resources = new ListReportNode("Resources:");
		final ListReportNode others = new ListReportNode("Others:");
		fixtures.remove(Integer.valueOf(item.getID()));
		if (item.iterator().hasNext()) {
			final IReportNode retval = new ListReportNode(loc,
																		concat(simple,
																				". Members of the unit:"));
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
			if (workers.getChildCount() != 0) {
				retval.add(workers);
			}
			if (animals.getChildCount() != 0) {
				retval.add(animals);
			}
			if (equipment.getChildCount() != 0) {
				retval.add(equipment);
			}
			if (resources.getChildCount() != 0) {
				retval.add(resources);
			}
			if (others.getChildCount() != 0) {
				retval.add(others);
			}
			return retval;
		} else {
			return new SimpleReportNode(loc, simple);
		}
	}

	/**
	 * @param worker  a Worker.
	 * @param details whether we should give details of the worker's stats and
	 *                experience---true only if the current player owns the worker.
	 * @return a sub-report on that worker.
	 */
	private static String workerReport(final IWorker worker, final boolean details) {
		final StringBuilder builder = new StringBuilder(2048);
		builder.append(worker.getName());
		builder.append(", a ");
		builder.append(worker.getRace());
		builder.append(". ");
		final WorkerStats stats = worker.getStats();
		if ((stats != null) && details) {
			builder.append("<p>He or she has the following stats: ");
			//noinspection HardcodedFileSeparator
			builder.append(stats.getHitPoints()).append(" / ")
					.append(stats.getMaxHitPoints()).append(" Hit Points");
			builder.append(", Strength ").append(
					getModifierString(stats.getStrength()));
			builder.append(", Dexterity ").append(
					getModifierString(stats.getDexterity()));
			builder.append(", Constitution ").append(
					getModifierString(stats.getConstitution()));
			builder.append(", Intelligence ").append(
					getModifierString(stats.getIntelligence()));
			builder.append(", Wisdom: ").append(
					getModifierString(stats.getWisdom()));
			builder.append(", Charisma: ").append(
					getModifierString(stats.getCharisma()));
			builder.append("</p>");
		}
		if (worker.iterator().hasNext() && details) {
			builder.append(HAS_TRAINING).append(LineEnd.LINE_SEP).append(OPEN_LIST);
			for (final IJob job : worker) {
				if (job instanceof Job) {
					builder.append(OPEN_LIST_ITEM);
					builder.append(job.getLevel());
					builder.append(" levels in ");
					builder.append(job.getName());
					builder.append(getSkills(job));
					builder.append(CLOSE_LIST_ITEM);
				}
			}
			builder.append(CLOSE_LIST);
		}
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * @param job a Job
	 * @return a String describing its skills.
	 */
	private static String getSkills(final Iterable<ISkill> job) {
		final StringBuilder builder = new StringBuilder(512);
		if (job.iterator().hasNext()) {
			boolean first = true;
			for (final ISkill skill : job) {
				if (first) {
					builder.append(" (");
					first = false;
				} else {
					builder.append(", ");
				}
				builder.append(skill.getName());
				builder.append(' ');
				builder.append(skill.getLevel());
			}
			builder.append(')');
		}
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * @param loc     the location of the worker in the map
	 * @param worker  a Worker.
	 * @param details whether we should give details of the worker's stats and
	 *                experience---true only if the current player owns the worker.
	 * @return a sub-report on that worker.
	 */
	private static MutableTreeNode produceWorkerRIR(final Point loc,
													final IWorker worker,
													final boolean details) {
		final IReportNode retval = new ComplexReportNode(loc,
																worker.getName() +
																		", a " +
																		worker.getRace() +
																		". ");
		final WorkerStats stats = worker.getStats();
		if ((stats != null) && details) {
			//noinspection HardcodedFileSeparator
			retval.add(new SimpleReportNode(loc, "He or she has the following stats: ",
												Integer.toString(stats.getHitPoints()),
												" / ", Integer.toString(
					stats.getMaxHitPoints()), " Hit Points, Strength ",
												getModifierString(stats.getStrength()),
												", Dexterity ", getModifierString(
					stats.getDexterity()), ", Constitution ", getModifierString(
					stats.getConstitution()), ", Intelligence ", getModifierString(
					stats.getIntelligence()), ", Wisdom ",
												getModifierString(stats.getWisdom()),
												", Charisma ", getModifierString(
					stats.getCharisma())));
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
	 * @param loc the location of the worker in the map
	 * @param job a Job
	 * @return a sub-report on that Job.
	 */
	private static MutableTreeNode produceJobRIR(final IJob job, final Point loc) {
		return new SimpleReportNode(loc, Integer.toString(job.getLevel()),
										" levels in ", job.getName(), getSkills(job));
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
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer) {
		// This can get big; we'll say 8K.
		final StringBuilder builder =
				new StringBuilder(8192).append("<h4>Units in the map</h4>");
		builder.append(LineEnd.LINE_SEP);
		builder.append("<p>(Any units listed above are not described again.)</p>");
		builder.append(LineEnd.LINE_SEP);
		final StringBuilder ours =
				new StringBuilder(8192).append("<h5>Your units</h5>");
		ours.append(LineEnd.LINE_SEP);
		ours.append(OPEN_LIST);
		final StringBuilder foreign =
				new StringBuilder(8192).append("<h5>Foreign units</h5>");
		foreign.append(LineEnd.LINE_SEP);
		foreign.append(OPEN_LIST);
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		Collections.sort(values, pairComparator);
		boolean anyForeign = false;
		boolean anyOurs = false;
		for (final Pair<Point, IFixture> pair : values) {
			if (pair.second() instanceof IUnit) {
				final IUnit unit = (IUnit) pair.second();
				if (currentPlayer.equals(unit.getOwner())) {
					anyOurs = true;
					ours.append(OPEN_LIST_ITEM)
							.append(atPoint(pair.first()))
							.append(' ')
							.append(distCalculator.distanceString(pair.first()))
							.append(produce(fixtures, map, currentPlayer,
									unit, pair.first()))
							.append(CLOSE_LIST_ITEM);
				} else {
					anyForeign = true;
					foreign.append(OPEN_LIST_ITEM)
							.append(atPoint(pair.first()))
							.append(' ')
							.append(distCalculator.distanceString(pair.first()))
							.append(produce(fixtures, map, currentPlayer,
									unit, pair.first()))
							.append(CLOSE_LIST_ITEM);
				}
			}
		}
		foreign.append(CLOSE_LIST);
		ours.append(CLOSE_LIST);
		if (anyOurs) {
			builder.append(ours);
		}
		if (anyForeign) {
			builder.append(foreign);
		}
		if (anyOurs || anyForeign) {
			return NullCleaner.assertNotNull(builder.toString());
		} else {
			return "";
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
		final IReportNode retval =
				new SectionReportNode(4, "Units in the map");
		retval.add(
				new SimpleReportNode("(Any units reported above are not described again.)"));
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		Collections.sort(values, pairComparator);
		final IReportNode theirs = new SectionListReportNode(5, "Foreign units");
		final IReportNode ours = new SectionListReportNode(5, "Your units");
		values.stream().filter(pair -> pair.second() instanceof Unit).forEach(pair -> {
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
		if (ours.getChildCount() != 0) {
			retval.add(ours);
		}
		if (theirs.getChildCount() != 0) {
			retval.add(theirs);
		}
		if (retval.getChildCount() == 1) { // 1, not 0, because of "any units ..."
			return EmptyReportNode.NULL_NODE;
		} else {
			return retval;
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "UnitReportGenerator";
	}
}
