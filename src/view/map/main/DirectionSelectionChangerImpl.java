package view.map.main;

import model.viewer.MapModel;

/**
 * A class for moving the cursor around the single-component map UI.
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
	 * @param mapModel the map model we're to use
	 */
	public DirectionSelectionChangerImpl(final MapModel mapModel) {
		model = mapModel;
	}
	/**
	 * Move the cursor up.
	 * TODO: Support restricted view.
	 */
	@Override
	public void up() { // NOPMD
		if (model.getSelectedTile().getRow() > 0) {
			model.setSelection(model.getSelectedTile().getRow() - 1, model.getSelectedTile().getCol());
		}
	}
	/**
	 * Move the cursor left.
	 * TODO: Support restricted view.
	 */
	@Override
	public void left() {
		if (model.getSelectedTile().getCol() > 0) {
			model.setSelection(model.getSelectedTile().getRow(), model.getSelectedTile().getCol() - 1);
		}
	}
	/**
	 * Move the cursor down.
	 * TODO: Support restricted view.
	 */
	@Override
	public void down() {
		if (model.getSelectedTile().getRow() < model.getSizeRows() - 1) {
			model.setSelection(model.getSelectedTile().getRow() + 1, model.getSelectedTile().getCol());
		}
	}
	/**
	 * Move the cursor right.
	 * TODO: Support restricted view.
	 */
	@Override
	public void right() {
		if (model.getSelectedTile().getCol() < model.getSizeCols() - 1) {
			model.setSelection(model.getSelectedTile().getRow(), model.getSelectedTile().getCol() + 1);
		}
	}

}
