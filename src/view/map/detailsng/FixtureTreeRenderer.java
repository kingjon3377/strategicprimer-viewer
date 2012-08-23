package view.map.detailsng;

import java.awt.Component;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import util.ImageLoader;
import view.map.details.Chit;

import model.map.HasImage;
import model.map.TileFixture;
import model.map.fixtures.RiverFixture;
import model.viewer.FixtureTreeNode;
import model.viewer.TileTreeNode;

/**
 * Renderer for tree cells.
 * @author Jonathan Lovelace
 */
public class FixtureTreeRenderer extends DefaultTreeCellRenderer {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(FixtureTreeRenderer.class.getName());
	/**
	 * Render a cell.
	 * @param tree The tree we're rendering in
	 * @param value The value in the tree we're rendering now
	 * @param sel Whether or not it's selected
	 * @param expanded Whether or not it's expanded
	 * @param leaf Whether or not it's a leaf
	 * @param row The current row
	 * @param focused Whether or not it has the focus.
	 * @return a component to render the node.
	 */
	// ESCA-JAVA0138:
	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object value,
			final boolean sel, final boolean expanded, final boolean leaf, final int row,
			final boolean focused) {
		final Component component = super.getTreeCellRendererComponent(tree,
				value, sel, expanded, leaf, row, focused);
		if (component instanceof JLabel) {
			if (value instanceof TileTreeNode) {
				((JLabel) component).setText("<html><p>On this tile:</p></html>");
			} else if (value instanceof FixtureTreeNode) {
				final TileFixture fixture = ((FixtureTreeNode) value).getFixture();
				((JLabel) component).setText("<html><p>" + fixture.toString() + "</p></html>");
				if (fixture instanceof HasImage) {
					((JLabel) component).setIcon(getIcon((HasImage) fixture));
				} else {
					((JLabel) component).setIcon(defaultFixtIcon);
				}
			}
		} else {
			LOGGER.warning("Component wasn't a JLabel, so skipping custom rendering");
		}
		return component;
	}
	/**
	 * @param obj a HasImage object
	 * @return an icon representing it
	 */
	private Icon getIcon(final HasImage obj) {
		// ESCA-JAVA0177:
		Icon retval;
		try {
			retval = ImageLoader.getLoader().loadIcon(obj.getImage());
		} catch (final FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "image file images/" + (obj.getImage())
					+ " not found");
			retval = defaultFixtIcon;
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading image");
			retval = defaultFixtIcon;
		}
		return retval;
	}
	/**
	 * the default fixture icon.
	 */
	private final Icon defaultFixtIcon = createDefaultFixtureIcon();

	/**
	 * @return the default icon for fixtures.
	 */
	private static Icon createDefaultFixtureIcon() {
		// TODO: If we ever get rid of Chit, copy its method to here.
		return new ImageIcon(Chit.createDefaultImage(new RiverFixture()));

	}
}
