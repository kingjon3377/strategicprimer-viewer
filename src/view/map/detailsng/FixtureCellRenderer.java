package view.map.detailsng;

import java.awt.Component;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.View;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import model.map.HasImage;
import model.map.TileFixture;
import model.map.fixtures.RiverFixture;
import model.viewer.FixtureTreeNode;
import model.viewer.TileTreeNode;
import util.ImageLoader;
import view.map.details.Chit;

/**
 * A tree-cell-renderer for FixtureTrees.
 *
 * @author Jonathan Lovelace
 *
 */
public class FixtureCellRenderer implements ListCellRenderer<TileFixture>, TreeCellRenderer, TableCellRenderer {
	/**
	 * Default list renderer, for cases we don't know how to handle.
	 */
	private static final DefaultListCellRenderer LIST_DEFAULT = new DefaultListCellRenderer();

	/**
	 * @param list the list being rendered
	 * @param value the object in the list that's being rendered
	 * @param index the index of the item that's being rendered
	 * @param isSelected whether the node is selected
	 * @param cellHasFocus whether the tree has the focus
	 * @return a component representing the cell
	 */
	// ESCA-JAVA0138:
	@Override
	public Component getListCellRendererComponent(
			final JList<? extends TileFixture> list, final TileFixture value,
			final int index, final boolean isSelected,
			final boolean cellHasFocus) {
		final Component component = LIST_DEFAULT.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);
			((JLabel) component).setText("<html><p>" + value.toString() + "</p></html>");
			if (value instanceof HasImage) {
				((JLabel) component).setIcon(getIcon((HasImage) value));
			} else {
				((JLabel) component).setIcon(defaultFixtIcon);
			}
		component.setMaximumSize(new Dimension(component.getMaximumSize().width, component.getMaximumSize().height * 2));
		setComponentPreferredSize((JComponent) component, list.getWidth());
		return component;
	}
	/**
	 * Default tree renderer.
	 */
	private static final DefaultTreeCellRenderer TREE_DEFAULT = new DefaultTreeCellRenderer();
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
		final Component component = TREE_DEFAULT.getTreeCellRendererComponent(tree,
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
			setComponentPreferredSize((JComponent) component, tree.getWidth());
		} else {
			LOGGER.warning("Component wasn't a JLabel, so skipping custom rendering");
		}
		return component;
	}
	/**
	 * Default table cell renderer.
	 */
	private static final DefaultTableCellRenderer TABLE_DEFAULT = new DefaultTableCellRenderer();
	/**
	 * Render a table cell.
	 * @param table the table
	 * @param value the value being rendered
	 * @param isSelected whether it's selected
	 * @param hasFocus whether it has focus
	 * @param row its row
	 * @param column its column
	 * @return a component to render it
	 */
	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value,
			final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		final Component component = TABLE_DEFAULT.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (component instanceof JLabel) {
			if (value instanceof TileFixture) {
				final TileFixture fixture = (TileFixture) value;
				((JLabel) component).setText("<html><p>" + fixture.toString() + "</p></html>");
				if (fixture instanceof HasImage) {
					((JLabel) component).setIcon(getIcon((HasImage) fixture));
				} else {
					((JLabel) component).setIcon(defaultFixtIcon);
				}
			} else {
				LOGGER.warning("Non-TileFixture item");
			}
		} else {
			LOGGER.warning("Component wasn't a JLabel, so skipping custom rendering");
		}
		setComponentPreferredSize((JComponent) component, table.getWidth());
		table.setRowHeight(row, component.getPreferredSize().height);
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
	 * Logger.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(FixtureCellRenderer.class.getName());
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
	/**
	 * Set a component's height given a fixed width.
	 * Adapted from http://blog.nobel-joergensen.com/2009/01/18/changing-preferred-size-of-a-html-jlabel/
	 * @param component the component we're laying out
	 * @param width the width we're working within
	 */
	private static void setComponentPreferredSize(final JComponent component, final int width) {
		final View view = (View) component.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
		view.setSize(width, 0);
		final int wid = (int) Math.ceil(view.getPreferredSpan(View.X_AXIS));
		final int height = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
		component.setPreferredSize(new Dimension(wid, height));
	}
}
