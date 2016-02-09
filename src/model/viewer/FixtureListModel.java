package model.viewer;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.DefaultListModel;
import model.listeners.SelectionChangeListener;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.Point;
import model.map.PointFactory;
import model.map.River;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.Ground;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.terrain.Forest;
import model.misc.IDriverModel;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A model for a FixtureList.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
 * TODO: tests
 *
 * @author Jonathan Lovelace
 */
public final class FixtureListModel extends DefaultListModel<@NonNull TileFixture>
		implements SelectionChangeListener {
	/**
	 * The driver model, which we use to get the population at a location.
	 */
	private final IDriverModel dmodel;
	/**
	 * The current point.
	 */
	private Point point = PointFactory.point(-1, -1);

	/**
	 * @param model the driver model to use
	 */
	public FixtureListModel(final IDriverModel model) {
		dmodel = model;
	}

	/**
	 * @param old      the formerly selected location
	 * @param newPoint the newly selected location
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old,
									 final Point newPoint) {
		clear();
		final IMapNG map = dmodel.getMap();
		final TileType base = map.getBaseTerrain(newPoint);
		if (TileType.NotVisible != base) {
			addElement(new TileTypeFixture(base));
		}
		final Iterable<River> rivers = map.getRivers(newPoint);
		if (rivers.iterator().hasNext()) {
			if (rivers instanceof TileFixture) {
				addElement((TileFixture) rivers);
			} else {
				final RiverFixture rfixt = new RiverFixture();
				rivers.forEach(rfixt::addRiver);
				addElement(rfixt);
			}
		}
		final Ground ground = map.getGround(newPoint);
		if (ground != null) {
			addElement(ground);
		}
		final Forest forest = map.getForest(newPoint);
		if (forest != null) {
			addElement(forest);
		}
		map.streamOtherFixtures(newPoint).forEach(this::addElement);
		point = newPoint;
	}

	/**
	 * Add a tile fixture to the current tile.
	 *
	 * @param fix the fixture to add.
	 */
	public void addFixture(final TileFixture fix) {
		final IMutableMapNG map = dmodel.getMap();
		if ((fix instanceof Ground) && (map.getGround(point) == null)) {
			map.setGround(point, (Ground) fix);
			selectedPointChanged(null, point);
		} else if ((fix instanceof Forest) && (map.getForest(point) == null)) {
			map.setForest(point, (Forest) fix);
			selectedPointChanged(null, point);
		} else if (fix instanceof TileTypeFixture) {
			if (map.getBaseTerrain(point) != ((TileTypeFixture) fix).getTileType()) {
				map.setBaseTerrain(point, ((TileTypeFixture) fix).getTileType());
				selectedPointChanged(null, point);
			}
		} else {
			// FIXME: Make addFixture() on IMutableMapNG boolean so we can just
			// add the fixture to the list model if the add operation isn't
			// redundant, rather than regenerating it every time.
			map.addFixture(point, fix);
			selectedPointChanged(null, point);
		}
	}

	/**
	 * Remove the specified items from the tile and the list.
	 *
	 * @param list the list of items to remove. If null, none are removed.
	 */
	public void removeAll(@Nullable final Iterable<TileFixture> list) {
		if (list != null) {
			final IMutableMapNG map = dmodel.getMap();
			for (final TileFixture fix : list) {
				if (fix instanceof TileTypeFixture) {
					map.setBaseTerrain(point, TileType.NotVisible);
					removeElement(fix);
				} else if ((fix instanceof Ground)
								   &&
								   fix.equals(
										   map.getGround(
												   point))) {
					map.setGround(point, null);
					removeElement(fix);
				} else if ((fix instanceof Forest)
								   &&
								   fix.equals(
										   map.getForest(
												   point))) {
					map.setForest(point, null);
					removeElement(fix);
				} else if (fix instanceof RiverFixture) {
					for (final River river : (RiverFixture) fix) {
						map.removeRivers(point, river);
					}
					removeElement(fix);
				} else {
					map.removeFixture(point, fix);
					removeElement(fix);
				}
				removeElement(fix);
			}
		}
	}

	/**
	 * A FixtureListModel is equal only to another FixtureListModel representing the same
	 * location in the same map.
	 *
	 * @param obj an object
	 * @return whether we're equal to it
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof FixtureListModel)
										 && ((FixtureListModel) obj).dmodel.equals
																					(dmodel)
										 && ((FixtureListModel) obj).point.equals(point));
	}

	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return dmodel.hashCode() | point.hashCode();
	}
	/**
	 * Prevent serialization.
	 * @param out ignored
	 * @throws IOException always
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * Prevent serialization
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	private void readObject(ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
