package view.map.detailsng;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import model.map.HasImage;
import model.map.TileFixture;
import model.map.fixtures.RiverFixture;
import model.viewer.FixtureNode;
import model.viewer.TileNode;
import util.ImageLoader;
import view.map.details.SimpleChit;
/**
 * A tree-cell-renderer for FixtureTrees.
 * @author Jonathan Lovelace
 *
 */
public class FixtureCellRenderer implements TreeCellRenderer {
	/**
	 * A mapping from Class to renderers.
	 */
	private final Map<Class<? extends FixtureNode>, Component> cache = new HashMap<Class<? extends FixtureNode>, Component>();
	/**
	 * Default renderer, for cases we don't know how to handle.
	 */
	private static final TreeCellRenderer DEFAULT = new DefaultTreeCellRenderer();
	/**
	 * @param tree the tree being rendered
	 * @param value the object in the tree that's being rendered
	 * @param selected whether the node is selected
	 * @param expanded whether the node is expanded
	 * @param leaf whther the node is a leaf
	 * @param row the row in the tree
	 * @param hasFocus whether the tree has the focus
	 * @return a component representing the cell
	 */
	// ESCA-JAVA0138:
	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object value,
			final boolean selected, final boolean expanded, final boolean leaf, final int row,
			final boolean hasFocus) {
		// ESCA-JAVA0177:
		final Component component; // NOPMD
		if (value instanceof TileNode) {
			component = new JLabel(((TileNode) value).getTileString());
		} else if (value instanceof FixtureNode) {
			final FixtureNode node = (FixtureNode) value;
			if (!cache.containsKey(value.getClass())) {
				final TileFixture tempFix = node.getFixture();
				if (tempFix instanceof HasImage) {
					final HasImage fix = (HasImage) tempFix;
					cache.put(node.getClass(), new JLabel(fix.toString(),
							getIcon(fix), SwingConstants.LEADING));
				} else {
					cache.put(node.getClass(), new JLabel(tempFix.toString(),
							defaultFixtIcon, SwingConstants.LEADING));
				}
		}
			component = cache.get(value.getClass());
		} else {
			component = DEFAULT.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
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
			retval = ImageLoader.getLoader().loadIcon("images/" + obj.getImage());
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "image file images/" + (obj.getImage()) + " not found");
			retval = defaultFixtIcon;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading image");
			retval = defaultFixtIcon;
		}
		return retval;
	}
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(FixtureCellRenderer.class.getName());
	/**
	 * the default fixture icon.
	 */
	private final Icon defaultFixtIcon = createDefaultFixtureIcon();
	/**
	 * @return the default icon for fixtures.
	 */
	private static Icon createDefaultFixtureIcon() {
		// TODO: If we ever get rid of Chit, copy its method to here.
		final BufferedImage image = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
		final SimpleChit chit = new SimpleChit(new RiverFixture(), null);
		final Graphics2D pen = image.createGraphics();
		chit.paint(pen);
		pen.dispose();
		return new ImageIcon(image);
		
	}
}
