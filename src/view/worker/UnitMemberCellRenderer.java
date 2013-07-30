package view.worker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import model.map.HasImage;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import util.ImageLoader;
/**
 * A cell renderer for the worker management tree.
 * @author Jonathan Lovelace
 */
public class UnitMemberCellRenderer implements TreeCellRenderer {
	/**
	 * Default renderer, for cases we don't know how to handle.
	 */
	private static final DefaultTreeCellRenderer DEFAULT = new DefaultTreeCellRenderer();
	/**
	 * @param tree the tree being rendered
	 * @param value the object in the tree that's being rendered
	 * @param selected whether it's selected
	 * @param expanded whether it's an expanded node
	 * @param leaf whether it's a leaf node
	 * @param row its row in the tree
	 * @param hasFocus whether the tree has the focus
	 * @return a component representing the cell
	 */
	// ESCA-JAVA0138: We have to have this many params to override the superclass method.
	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object value,
			final boolean selected, final boolean expanded, final boolean leaf, final int row,
			final boolean hasFocus) {
		final Component component = DEFAULT.getTreeCellRendererComponent(tree,
				value, selected, expanded, leaf, row, hasFocus);
		final Object internal = value instanceof DefaultMutableTreeNode ? ((DefaultMutableTreeNode) value)
				.getUserObject() : value;
		if (internal instanceof HasImage) {
			((JLabel) component).setIcon(getIcon((HasImage) internal));
		}
		if (internal instanceof Worker) {
			final Worker worker = (Worker) internal;
			final StringBuilder builder = new StringBuilder("<html><p>");
			builder.append(worker.getName());
			if (!"human".equals(worker.getRace())) {
				builder.append(", a ").append(worker.getRace());
			}
			if (worker.iterator().hasNext()) {
				builder.append(" (");
				boolean notFirst = false;
				for (Job job : worker) {
					if (notFirst) {
						builder.append(", ");
					} else {
						notFirst = true;
					}
					builder.append(job.getName()).append(' ').append(job.getLevel());
				}
				builder.append(')');
			}
			builder.append("</p></html>");
			((JLabel) component).setText(builder.toString());
		} else if (internal instanceof Unit) {
			final Unit unit = (Unit) internal;
			((JLabel) component).setText(new StringBuilder("<html><p>")
					.append("Unit of type ").append(unit.getKind())
					.append(", named ").append(unit.getName())
					.append("</p></html>").toString());
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
		} catch (final FileNotFoundException e) { // $codepro.audit.disable logExceptions
			LOGGER.log(Level.SEVERE, "image file images/" + (obj.getImage())
					+ " not found");
			retval = defaultFixtIcon;
		} catch (final IOException e) { // $codepro.audit.disable logExceptions
			LOGGER.log(Level.SEVERE, "I/O error reading image");
			retval = defaultFixtIcon;
		}
		return retval;
	}
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(UnitMemberCellRenderer.class.getName());
	/**
	 * the default fixture icon.
	 */
	private final Icon defaultFixtIcon = createDefaultFixtureIcon();

	/**
	 * @return the default icon for fixtures.
	 */
	private static Icon createDefaultFixtureIcon() {
		/**
		 * The margin we allow around the chit itself in the default image.
		 */
		final double margin = 0.15; // NOPMD
		final int imageSize = 24; // NOPMD
		final BufferedImage temp = new BufferedImage(imageSize, imageSize,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D pen = temp.createGraphics();
			final Color saveColor = pen.getColor();
			pen.setColor(Color.RED);
			pen.fillRoundRect((int) Math.round(imageSize * margin) + 1,
					(int) Math.round(imageSize * margin) + 1,
					(int) Math.round(imageSize * (1.0 - margin * 2.0)),
					(int) Math.round(imageSize * (1.0 - margin * 2.0)),
					(int) Math.round(imageSize * (margin / 2.0)),
					(int) Math.round(imageSize * (margin / 2.0)));
			pen.setColor(saveColor);
			pen.fillRoundRect(
					((int) Math.round(imageSize / 2.0 - imageSize * margin)) + 1,
					((int) Math.round(imageSize / 2.0 - imageSize * margin)) + 1,
					(int) Math.round(imageSize * margin * 2.0),
					(int) Math.round(imageSize * margin * 2.0),
					(int) Math.round(imageSize * margin / 2.0),
					(int) Math.round(imageSize * margin / 2.0));
		return new ImageIcon(temp);

	}
}
