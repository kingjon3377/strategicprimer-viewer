package view.map.main;

import model.viewer.MapModel;

/**
 * A class for moving the cursor around the single-component map UI.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class DirectionSelectionChangerImpl implements DirectionSelectionChanger {
	/**
	 * The map model we'll be referring to.
	 */
	private final MapModel model;

	/**
	 * Constructor.
	 * 
	 * @param mapModel
	 *            the map model we're to use
	 */
	public DirectionSelectionChangerImpl(final MapModel mapModel) {
		model = mapModel;
	}

	/**
	 * Move the cursor up.
	 */
	@Override
	public void up() { // NOPMD
		if (model.getSelectedTile().getLocation().row() > 0) {
			model.setSelection(model.getSelectedTile().getLocation().row() - 1, model
					.getSelectedTile().getLocation().col());
		}
	}

	/**
	 * Move the cursor left.
	 */
	@Override
	public void left() {
		if (model.getSelectedTile().getLocation().col() > 0) {
			model.setSelection(model.getSelectedTile().getLocation().row(), model
					.getSelectedTile().getLocation().col() - 1);
		}
	}

	/**
	 * Move the cursor down.
	 */
	@Override
	public void down() {
		if (model.getSelectedTile().getLocation().row() < model.getSizeRows() - 1) {
			model.setSelection(model.getSelectedTile().getLocation().row() + 1, model
					.getSelectedTile().getLocation().col());
		}
	}

	/**
	 * Move the cursor right.
	 */
	@Override
	public void right() {
		if (model.getSelectedTile().getLocation().col() < model.getSizeCols() - 1) {
			model.setSelection(model.getSelectedTile().getLocation().row(), model
					.getSelectedTile().getLocation().col() + 1);
		}
	}

	/**
	 * 
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "DirectionSelectionChangerImpl";
	}
}
