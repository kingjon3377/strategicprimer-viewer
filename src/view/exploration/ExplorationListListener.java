package view.exploration;

import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IDRegistrar;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import model.exploration.HuntingModel;
import model.exploration.IExplorationModel;
import model.exploration.IExplorationModel.Speed;
import model.listeners.SelectionChangeListener;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.SimpleMovement;
import model.viewer.FixtureListModel;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.LineEnd;
import util.Pair;
import util.SingletonRandom;
import view.map.details.FixtureList;

import static model.map.TileType.Ocean;

/**
 * A list-data-listener to select a random but suitable set of fixtures to be 'discovered'
 * if the tile is explored.
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
public final class ExplorationListListener implements SelectionChangeListener {
	/**
	 * The exploration model, which tells us the currently selected unit and tile.
	 */
	private final IExplorationModel model;
	/**
	 * The list this is attached to.
	 */
	private final FixtureList list;
	/**
	 * A list of animal-tracks objects, which we want to remove from the main map
	 * whenever the list's target gets changed.
	 */
	private final Collection<Pair<Point, Animal>> tracks = new ArrayList<>();
	/**
	 * A "hunting model," to get the animals to have traces of.
	 */
	private final HuntingModel huntingModel;
	/**
	 * An ID number factory for the animal tracks.
	 */
	private final IDRegistrar idf;
	/**
	 * Mutex.
	 */
	private boolean outsideCritical = true;

	/**
	 * Constructor.
	 *
	 * @param mainList         the list this is attached to
	 * @param explorationModel the exploration model
	 */
	public ExplorationListListener(final IExplorationModel explorationModel,
								   final FixtureList mainList) {
		model = explorationModel;
		list = mainList;
		huntingModel = new HuntingModel(model.getMap());
		idf = IDFactoryFiller.createFactory(model);
	}

	@Override
	public void selectedPointChanged(@Nullable final Point old, final Point newPoint) {
		SwingUtilities.invokeLater(this::randomizeSelection);
	}

	/**
	 * Select a suitable but randomized selection of fixtures. Do nothing if there is no
	 * selected unit.
	 */
	private void randomizeSelection() {
		final IUnit selUnit = model.getSelectedUnit();
		if (outsideCritical && (selUnit != null)) {
			outsideCritical = false;
			for (final Pair<Point, Animal> pair : tracks) {
				model.getMap().removeFixture(pair.first(), pair.second());
			}
			tracks.clear();
			list.clearSelection();
			final List<IntPair<TileFixture>> constants = new ArrayList<>();
			final List<IntPair<TileFixture>> possibles = new ArrayList<>();
			int i = 0;
			for (final TileFixture fix : new ListModelWrapper<>(list.getModel())) {
				// FIXME: Take speed into account
				if (SimpleMovement.shouldAlwaysNotice(selUnit, fix)) {
					constants.add(IntPair.of(i, fix));
				} else if (SimpleMovement
								   .shouldSometimesNotice(selUnit, Speed.Normal, fix)) {
					possibles.add(IntPair.of(i, fix));
				}
				i++;
			}
			final Point currentLocation = model.getSelectedUnitLocation();
			if (currentLocation.getRow() >= 0 && currentLocation.getCol() >= 0) {
				final String possibleTracks;
				if (Ocean == model.getMap().getBaseTerrain(currentLocation)) {
					possibleTracks = huntingModel.fish(currentLocation, 1).get(0);
				} else {
					possibleTracks = huntingModel.hunt(currentLocation, 1).get(0);
				}
				if (!HuntingModel.NOTHING.equals(possibleTracks)) {
					final Animal animal =
							new Animal(possibleTracks, true, false, "wild",
											  idf.createID());
					((FixtureListModel) list.getModel()).addFixture(animal);
					possibles.add(IntPair.of(i, animal));
					tracks.add(Pair.of(currentLocation, animal));
				}
			}
			Collections.shuffle(possibles);
			// TODO: Use Perception to decide how many, as in SimpleMovement.selectNoticed
			if ((possibles.size() > 1) && (SingletonRandom.RANDOM.nextDouble() < 0.1)) {
				constants.add(possibles.get(0));
				constants.add(possibles.get(1));
			} else if (!possibles.isEmpty()) {
				constants.add(possibles.get(0));
			}
			final int[] indices = new int[constants.size()];
			for (int index = 0; index < constants.size(); index++) {
				indices[index] = constants.get(index).first();
			}
			list.setSelectedIndices(indices);
			outsideCritical = true;
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ExplorationListListener";
	}

	/**
	 * Like a Pair<Integer, T>, but without the headaches induced by boxing an int into
	 * Integer.
	 *
	 * TODO: If we start using Guava, use of this class should be replaced by
	 * Multiset, or something?
	 *
	 * @param <T> the type in question.
	 * @author Jonathan Lovelace
	 */
	private static final class IntPair<@NonNull T> {
		/**
		 * The number in the pair.
		 */
		private final int number;
		/**
		 * The object in the pair.
		 */
		private final T object;

		/**
		 * Constructor. Use the factory method rather than this constructor.
		 *
		 * @param num the number in the pair
		 * @param obj the object in the pair
		 */
		protected IntPair(final int num, final T obj) {
			number = num;
			object = obj;
		}

		/**
		 * Factory method.
		 *
		 * @param num the number in the pair
		 * @param obj the object in the pair
		 * @param <I> the type of object
		 * @return the pair
		 */
		protected static <@NonNull I> IntPair<I> of(final int num, final I obj) {
			return new IntPair<>(num, obj);
		}

		/**
		 * The number in the pair.
		 * @return the number in the pair
		 */
		public int first() {
			return number;
		}

		/**
		 * The object in the pair.
		 * @return the object in the pair
		 */
		@SuppressWarnings("unused")
		public T second() {
			return object;
		}

		/**
		 * "(num, objString)".
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			final String objStr = object.toString();
			return String.format("(%d, %s)", Integer.valueOf(number), objStr);
		}
	}

	/**
	 * A wrapper around a ListModel.
	 *
	 * @param <E> the type of thing in the list model.
	 */
	private static class ListModelWrapper<E> extends AbstractList<E> {
		/**
		 * The wrapped object.
		 */
		private final ListModel<E> wrapped;

		/**
		 * Constructor.
		 * @param listModel the wrapped object
		 */
		protected ListModelWrapper(final ListModel<E> listModel) {
			wrapped = listModel;
		}

		/**
		 * Get the element at the given index.
		 * @param index an index
		 * @return the object at that index
		 */
		@Override
		public E get(final int index) {
			return wrapped.getElementAt(index);
		}

		/**
		 * Get the size of the list-model.
		 * @return the size of the list-model
		 */
		@Override
		public int size() {
			return wrapped.getSize();
		}

		/**
		 * Mostly delegate toString() to the wrapped object.
		 * @return a diagnostic String
		 */
		@SuppressWarnings("StringConcatenationMissingWhitespace")
		@Override
		public String toString() {
			return "Wrapper around the following ListModel:" + LineEnd.LINE_SEP +
						   wrapped;
		}
	}
}
