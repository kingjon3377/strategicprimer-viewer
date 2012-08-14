package view.map.detailsng;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JList;

import model.map.TileFixture;
import model.viewer.FixtureListModel;
import util.PropertyChangeSource;

/**
 * A visual tree-based representation of the contents of a tile.
 *
 * @author Jonathan Lovelace
 */
public class FixtureList extends JList<TileFixture> {
	/**
	 * Constructor.
	 *
	 * @param property the property the model will be listening for
	 * @param sources objects the model should listen to
	 */
	public FixtureList(final String property,
			final PropertyChangeSource... sources) {
		super(new FixtureListModel(property, sources));
		setCellRenderer(new FixtureCellRenderer());
		setFixedCellWidth(getWidth());
		setFixedCellHeight(-1);
		addComponentListener(new ComponentListener() {
			/**
			 * Handle component-resize events.
			 * @param event ignored
			 */
			@Override
			public void componentResized(final ComponentEvent event) {
				setFixedCellHeight(-1);
				setFixedCellWidth(getWidth());
			}
			/**
			 * Ignored.
			 * @param event ignored.
			 */
			@Override
			public void componentMoved(final ComponentEvent event) {
				// Do nothing
			}
			/**
			 * Ignored.
			 * @param event ignored.
			 */
			@Override
			public void componentShown(final ComponentEvent event) {
				// Do nothing
			}
			/**
			 * Ignored.
			 * @param event ignored.
			 */
			@Override
			public void componentHidden(final ComponentEvent event) {
				// Do nothing
			}

		});
	}
}
