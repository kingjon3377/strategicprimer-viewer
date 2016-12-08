package controller.map.report.tabular;

import java.io.IOException;
import model.map.DistanceComparator;
import model.map.HasKind;
import model.map.IFixture;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.terrain.Forest;
import util.Pair;
import util.PatientMap;

/**
 * A tabular report generator for crops---forests, groves, orchards, fields, meadows,
 * and shrubs.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class CropTabularReportGenerator implements ITableGenerator<TileFixture> {
	/**
	 * The base point to use for distance calculations.
	 */
	private final Point base;

	/**
	 * Constructor.
	 *
	 * @param distBase the point to use as the base for distance calculations
	 */
	public CropTabularReportGenerator(final Point distBase) {
		base = distBase;
	}

	/**
	 * @param obj an object
	 * @return whether this report generator covers it
	 */
	@Override
	public boolean applies(final IFixture obj) {
		return (obj instanceof Forest) || (obj instanceof Shrub) ||
					   (obj instanceof Meadow) ||
					   (obj instanceof Grove);
	}

	/**
	 * @param ostream  the stream to write the row to
	 * @param fixtures the set of fixtures
	 * @param item     the fixture to base the line on
	 * @param loc      its location
	 * @throws IOException on I/O error writing to the stream
	 */
	@Override
	public boolean produce(final Appendable ostream,
						   final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						   final TileFixture item, final Point loc) throws IOException {
		final String kind;
		final String cultivation;
		final String status;
		final String crop;
		if (item instanceof Forest) {
			final Forest forest = (Forest) item;
			if (forest.isRows()) {
				kind = "rows";
			} else {
				kind = "forest";
			}
			cultivation = "---";
			status = "---";
			crop = forest.getKind();
		} else if (item instanceof Shrub) {
			kind = "shrub";
			cultivation = "---";
			status = "---";
			crop = ((Shrub) item).getKind();
		} else if (item instanceof Meadow) {
			final Meadow meadow = (Meadow) item;
			if (meadow.isField()) {
				kind = "field";
			} else {
				kind = "meadow";
			}
			if (meadow.isCultivated()) {
				cultivation = "cultivated";
			} else {
				cultivation = "wild";
			}
			status = meadow.getStatus().toString();
			crop = meadow.getKind();
		} else if (item instanceof Grove) {
			final Grove grove = (Grove) item;
			if (grove.isOrchard()) {
				kind = "orchard";
			} else {
				kind = "grove";
			}
			if (grove.isCultivated()) {
				cultivation = "cultivated";
			} else {
				cultivation = "wild";
			}
			status = "---";
			crop = grove.getKind();
		} else {
			return false;
		}
		writeField(ostream, distanceString(loc, base));
		writeFieldDelimiter(ostream);
		writeField(ostream, loc.toString());
		writeFieldDelimiter(ostream);
		writeField(ostream, kind);
		writeFieldDelimiter(ostream);
		writeField(ostream, cultivation);
		writeFieldDelimiter(ostream);
		writeField(ostream, status);
		writeFieldDelimiter(ostream);
		writeField(ostream, crop);
		ostream.append(getRowDelimiter());
		return true;
	}

	@Override
	public String headerRow() {
		return "Distance,Location,Kind,Cultivation,Status,Crop";
	}

	@SuppressWarnings("QuestionableName")
	@Override
	public int comparePairs(final Pair<Point, TileFixture> one,
							final Pair<Point, TileFixture> two) {
		final TileFixture first = one.second();
		final TileFixture second = two.second();
		if (!applies(first) || !applies(second)) {
			throw new IllegalArgumentException("Unhandleable argument");
		}
		final int cropCmp =
				((HasKind) first).getKind().compareTo(((HasKind) second).getKind());
		if (cropCmp == 0) {
			final int cmp =
					new DistanceComparator(base).compare(one.first(), two.first());
			if (cmp == 0) {
				final int kindCmp = Integer.compare(first.getClass().hashCode(),
						second.getClass().hashCode());
				if (kindCmp == 0) {
					return Integer.compare(first.hashCode(), second.hashCode());
				} else {
					return kindCmp;
				}
			} else {
				return cmp;
			}
		} else {
			return cropCmp;
		}
	}

	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "CropTabularReportGenerator";
	}
}