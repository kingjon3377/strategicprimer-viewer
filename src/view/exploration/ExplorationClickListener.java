package view.exploration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.SwingUtilities;

import org.eclipse.jdt.annotation.Nullable;

import model.exploration.IExplorationModel;
import model.exploration.IExplorationModel.Direction;
import model.map.IMap;
import model.map.Tile;
import model.map.TileFixture;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import util.Pair;
import util.PropertyChangeSource;
import view.map.details.FixtureList;

/**
 * The listener for clicks on tile buttons indicating movement.
 * @author Jonathan Lovelace
 *
 */
public final class ExplorationClickListener implements ActionListener, PropertyChangeSource {
	/**
	 * The exploration model.
	 */
	private final IExplorationModel model;
	/**
	 * The direction this button is from the currently selected tile.
	 */
	private final Direction direction;
	/**
	 * The list of fixtures on this tile in the main map.
	 */
	private final FixtureList list;
	/**
	 * Constructor.
	 * @param emodel the exploration model
	 * @param direct what direction this button is from the center.
	 * @param mainList the list of fixtures on this tile in the main map.
	 */
	public ExplorationClickListener(final IExplorationModel emodel,
			final Direction direct, final FixtureList mainList) {
		model = emodel;
		direction = direct;
		list = mainList;
	}
	/**
	 * @param evt the event to handle.
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				handleMove();
			}
		});
	}

	/**
	 * Handle a button press. This was refactored out of the actionPerformed
	 * method because it has to be run on the EDT to prevent concurrency issues,
	 * and putting this code in the Runnable means accessing private members
	 * from that inner class ...
	 */
	protected void handleMove() {
		try {
			final List<TileFixture> fixtures = getSelectedValuesList(list);
			model.move(direction);
			for (final Pair<IMap, String> pair : model
					.getSubordinateMaps()) {
				final IMap map = pair.first();
				final Tile tile = map.getTile(model
						.getSelectedUnitLocation());
				for (final TileFixture fix : fixtures) {
					tile.addFixture(fix);
				}
			}
		} catch (TraversalImpossibleException except) {
			pcs.firePropertyChange("point", null,
					model.getSelectedUnitLocation());
			pcs.firePropertyChange("cost",
					Integer.valueOf(0), Integer.valueOf(1));
		}
	}

	/**
	 * A reimplementation of {@link javax.swing.JList#getSelectedValuesList()} that's
	 * guaranteed not to throw an ArrayIndexOutOfBoundsException.
	 *
	 * @param list the list to operate on
	 * @return a list of its selected members
	 */
	private static List<TileFixture> getSelectedValuesList(final FixtureList list) {
		final int[] selections = list.getSelectedIndices();
		final ListModel<TileFixture> model = list.getModel();
		final List<TileFixture> retval = new ArrayList<>();
		for (int sel : selections) {
			if (sel < model.getSize()) {
				retval.add(model.getElementAt(sel));
			} else {
				retval.add(model.getElementAt(model.getSize() - 1));
			}
		}
		return retval;
	}
	/**
	 * A helper to handle notifying listeners of property changes.
	 */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	/**
	 * @param listener a new listener to listen to us
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	/**
	 * @param listener a former listener that wants to stop listening
	 */
	@Override
	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
}