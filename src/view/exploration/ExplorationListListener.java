package view.exploration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import model.exploration.IExplorationModel;
import model.map.TileFixture;
import model.map.fixtures.mobile.SimpleMovement;
import model.map.fixtures.mobile.Unit;

import org.eclipse.jdt.annotation.Nullable;

import view.map.details.FixtureList;

/**
 * A list-data-listener to select a random but suitable set of fixtures to be
 * 'discovered' if the tile is explored.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ExplorationListListener implements ListDataListener {
	/**
	 * The exploration model, which tells us the currently selected unit and
	 * tile.
	 */
	private final IExplorationModel model;
	/**
	 * The list this is attached to.
	 */
	private final FixtureList list;

	/**
	 * Constructor.
	 *
	 * @param mainList the list this is attached to
	 * @param emodel the exploration model
	 */
	ExplorationListListener(final IExplorationModel emodel,
			final FixtureList mainList) {
		model = emodel;
		list = mainList;
	}

	/**
	 * @param evt an event indicating items were removed from the list
	 */
	@Override
	public void intervalRemoved(@Nullable final ListDataEvent evt) {
		randomizeSelection();
	}

	/**
	 * @param evt an event indicating items were added to the list
	 */
	@Override
	public void intervalAdded(@Nullable final ListDataEvent evt) {
		randomizeSelection();
	}

	/**
	 * @param evt an event indicating items were changed in the list
	 */
	@Override
	public void contentsChanged(@Nullable final ListDataEvent evt) {
		randomizeSelection();
	}
	/**
	 * Like a Pair<Integer, T>, but without the headaches induced by boxing an int into Integer.
	 * @param <T> the type in question.
	 * @author Jonathan Lovelace
	 */
	private static class IntPair<T> {
		/**
		 * Factory method.
		 * @param number the number in the pair
		 * @param object the object in the pair
		 * @param <I> the type of object
		 * @return the pair
		 */
		static <I> IntPair<I> of(final int number, final I object) {
			return new IntPair<>(number, object);
		}
		/**
		 * Constructor. Use the factory method rather than this constructor.
		 * @param num the number in the pair
		 * @param obj the object in the pair
		 */
		IntPair(final int num, final T obj) {
			number = num;
			object = obj;
		}
		/**
		 * The number in the pair.
		 */
		private final int number;
		/**
		 * The object in the pair.
		 */
		private final T object;
		/**
		 * @return the number in the pair
		 */
		public int first() {
			return number;
		}
		/**
		 * @return the object in the pair
		 */
		@SuppressWarnings("unused")
		public T second() {
			return object;
		}
	}
	/**
	 * Select a suitable but randomized selection of fixtures. Do nothing if
	 * there is no selected unit.
	 */
	private void randomizeSelection() {
		final Unit selUnit = model.getSelectedUnit();
		if (selUnit != null) {
			list.clearSelection();
			final List<IntPair<TileFixture>> constants = new ArrayList<>();
			final List<IntPair<TileFixture>> possibles = new ArrayList<>();
			for (int i = 0; i < list.getModel().getSize(); i++) {
				final TileFixture fix = list.getModel().getElementAt(i);
				if (fix == null) {
					continue;
				} else if (SimpleMovement.shouldAlwaysNotice(selUnit, fix)) {
					constants.add(IntPair.of(i, fix));
				} else if (SimpleMovement.mightNotice(selUnit, fix)) {
					possibles.add(IntPair.of(i, fix));
				}
			}
			Collections.shuffle(possibles);
			if (!possibles.isEmpty()) {
				constants.add(possibles.get(0));
			}
			final int[] indices = new int[constants.size()];
			for (int i = 0; i < constants.size(); i++) {
				indices[i] = constants.get(i).first();
			}
			list.setSelectedIndices(indices);
		}
	}
}
