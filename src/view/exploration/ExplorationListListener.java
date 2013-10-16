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

import util.Pair;
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
	 * Select a suitable but randomized selection of fixtures. Do nothing if
	 * there is no selected unit.
	 */
	private void randomizeSelection() {
		final Unit selUnit = model.getSelectedUnit();
		if (selUnit != null) {
			list.clearSelection();
			final List<Pair<Integer, TileFixture>> constants = new ArrayList<>();
			final List<Pair<Integer, TileFixture>> possibles = new ArrayList<>();
			for (int i = 0; i < list.getModel().getSize(); i++) {
				final TileFixture fix = list.getModel().getElementAt(i);
				if (SimpleMovement.shouldAlwaysNotice(selUnit, fix)) {
					constants.add(Pair.of(Integer.valueOf(i), fix));
				} else if (SimpleMovement.mightNotice(selUnit, fix)) {
					possibles.add(Pair.of(Integer.valueOf(i), fix));
				}
			}
			Collections.shuffle(possibles);
			if (!possibles.isEmpty()) {
				constants.add(possibles.get(0));
			}
			final int[] indices = new int[constants.size()];
			for (int i = 0; i < constants.size(); i++) {
				indices[i] = constants.get(i).first().intValue();
			}
			list.setSelectedIndices(indices);
		}
	}
}
