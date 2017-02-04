package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.mobile.Animal;
import model.report.EmptyReportNode;
import model.report.IReportNode;
import model.report.ListReportNode;
import model.report.SectionListReportNode;
import model.report.SimpleReportNode;
import org.eclipse.jdt.annotation.NonNull;
import util.MultiMapHelper;
import util.Pair;
import util.PatientMap;

/**
 * A report generator for sightings of animals.
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
public final class AnimalReportGenerator extends AbstractReportGenerator<Animal> {
	/**
	 * Constructor.
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public AnimalReportGenerator(final Comparator<@NonNull Pair<@NonNull Point, @NonNull
																						IFixture>> comparator) {
		super(comparator);
	}
	/**
	 * Produce the sub-report on sightings of animals.
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
		final Map<String, Collection<Point>> items = new HashMap<>();
		for (final Pair<Point, IFixture> pair : values) {
			if (pair.second() instanceof Animal) {
				final Animal animal = (Animal) pair.second();
				final String desc;
				if (animal.isTraces()) {
					desc = "tracks or traces of " + animal.getKind();
				} else if (animal.isTalking()) {
					desc = "talking " + animal.getKind();
				} else {
					desc = animal.getKind();
				}
				MultiMapHelper
						.getMapValue(items, desc, AbstractReportGenerator::pointsListAt)
						.add(pair.first());
				if (animal.getID() > 0) {
					fixtures.remove(Integer.valueOf(animal.getID()));
				} else {
					fixtures.entrySet().stream()
							.filter(entry -> entry.getValue().equals(pair))
							.map(Map.Entry::getKey).forEach(fixtures::remove);
				}
			}
		}
		ostream.format("<h4>Animal sightings or encounters</h4>%n<ul>%n");
		for (final Map.Entry<String, Collection<Point>> entry : items.entrySet()) {
			ostream.format("<li>%s: %s</li>%n", entry.getKey(),
					entry.getValue().toString());
		}
		ostream.format("</ul>%n");
	}

	/**
	 * Produce the sub-report on sightings of animals.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the report
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
										  fixtures,
								  final IMapNG map, final Player currentPlayer) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		final Map<String, IReportNode> items = new HashMap<>();
		for (final Pair<Point, IFixture> pair : values) {
			if (pair.second() instanceof Animal) {
				final Animal animal = (Animal) pair.second();
				final String animalKind = animal.getKind();
				MultiMapHelper.getMapValue(items, animalKind, ListReportNode::new)
						.add(produceRIR(fixtures, map, currentPlayer, animal,
						pair.first()));
				fixtures.remove(Integer.valueOf(animal.getID()));
			}
		}
		if (items.isEmpty()) {
			return EmptyReportNode.NULL_NODE;
		} else {
			final IReportNode retval =
					new SectionListReportNode(4, "Animal sightings or encounters");
			for (final Map.Entry<String, IReportNode> entry : items.entrySet()) {
				retval.add(entry.getValue());
			}
			return retval;
		}
	}

	/**
	 * Produce the sub-report about an individual Animal.
	 * @param fixtures      the set of fixtures
	 * @param currentPlayer the player for whom the report is being produced
	 * @param map           ignored
	 * @param item          an animal
	 * @param loc           its location
	 * @param ostream	    the Formatter to write to
	 */
	@Override
	public void produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						final IMapNG map, final Player currentPlayer,
						final Animal item, final Point loc, final Formatter ostream) {
		ostream.format("%s", atPoint(loc));
		if (item.isTraces()) {
			ostream.format("tracks or traces of ");
		} else if (item.isTalking()) {
			ostream.format("talking ");
		}
		ostream.format("%s %s", item.getKind(), distCalculator.distanceString(loc));
	}

	/**
	 * Produce the sub-report about an individual Animal.
	 * @param fixtures      the set of fixtures
	 * @param currentPlayer the player for whom the report is being produced
	 * @param map           ignored
	 * @param item          an animal
	 * @param loc           its location
	 * @return a sub-report on the animal
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
											   fixtures,
									   final IMapNG map, final Player currentPlayer,
									   final Animal item, final Point loc) {
		final String tracesOrTalking;
		if (item.isTraces()) {
			tracesOrTalking = "tracks or traces of ";
		} else if (item.isTalking()) {
			tracesOrTalking = "talking ";
		} else {
			tracesOrTalking = "";
		}
		return new SimpleReportNode(loc, atPoint(loc), tracesOrTalking,
										   item.getKind(), " ",
										   distCalculator.distanceString(loc));
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "AnimalReportGenerator";
	}
}
