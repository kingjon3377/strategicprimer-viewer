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

import org.eclipse.jdt.annotation.Nullable;

import util.ImageLoader;
import util.TypesafeLogger;

/**
 * A cell renderer for the worker management tree.
 *
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
	// ESCA-JAVA0138: We have to have this many params to override the
	// superclass method.
	@Override
	public Component getTreeCellRendererComponent(@Nullable final JTree tree,
			@Nullable final Object value, final boolean selected,
			final boolean expanded, final boolean leaf, final int row,
			final boolean hasFocus) {
		if (tree == null || value == null) {
			throw new IllegalStateException("Null tree or value");
		}
		final Component component = DEFAULT.getTreeCellRendererComponent(tree,
				value, selected, expanded, leaf, row, hasFocus);
		if (component == null) {
			throw new IllegalStateException(
					"Default produced null component somehow");
		}
		component.setBackground(Color.WHITE);
		final Object internal = getNodeValue(value);
		if (internal instanceof HasImage) {
			((JLabel) component).setIcon(getIcon((HasImage) internal));
		}
		if (internal instanceof Worker) {
			final Worker worker = (Worker) internal;
			// Assume at least a K in size.
			final StringBuilder builder = new StringBuilder(1024)
					.append("<html><p>");
			builder.append(worker.getName());
			if (!"human".equals(worker.getRace())) {
				builder.append(", a ").append(worker.getRace());
			}
			builder.append(jobCSL(worker));
			builder.append("</p></html>");
			((JLabel) component).setText(builder.toString());
		} else if (internal instanceof Unit) {
			final Unit unit = (Unit) internal;
			final String kind = unit.getKind();
			final String name = unit.getName();
			((JLabel) component).setText(new StringBuilder(48 + kind.length()
					+ name.length()).append("<html><p>")
					.append("Unit of type ").append(kind).append(", named ")
					.append(name).append("</p></html>").toString());
			final String orders = unit.getOrders().toLowerCase();
			if (orders.contains("fixme") && unit.iterator().hasNext()) {
				component.setBackground(Color.PINK);
				((JLabel) component).setOpaque(true);
			} else if (orders.contains("todo") && unit.iterator().hasNext()) {
				component.setBackground(Color.YELLOW);
				((JLabel) component).setOpaque(true);
			}
		}
		return component;
	}

	/**
	 * @param value a node of the tree
	 * @return it, unless it's a DefaultMutableTreeNode, in which case return
	 *         the associated user object
	 */
	@Nullable private static Object getNodeValue(final Object value) {
		return value instanceof DefaultMutableTreeNode ? ((DefaultMutableTreeNode) value)
				.getUserObject() : value;
	}

	/**
	 * @param iter something containing Jobs
	 * @return a comma-separated list of them, in parentheses, prepended by a
	 *         space, if there are any.
	 */
	private static String jobCSL(final Iterable<Job> iter) {
		if (iter.iterator().hasNext()) {
			final StringBuilder builder = new StringBuilder(100);
			builder.append(" (");
			boolean first = true;
			for (final Job job : iter) {
				if (first) {
					first = false;
				} else {
					builder.append(", ");
				}
				builder.append(job.getName()).append(' ').append(job.getLevel());
			}
			builder.append(')');
			final String retval = builder.toString();
			assert retval != null;
			return retval;
		} else {
			return "";
		}
	}
	/**
	 * @param obj a HasImage object
	 * @return an icon representing it
	 */
	private Icon getIcon(final HasImage obj) {
		// ESCA-JAVA0177:
		Icon retval;
		String image = obj.getImage();
		if (image.isEmpty()) {
			image = obj.getDefaultImage();
		}
		// FIXME: If getImage() references a file that's not there, try the
		// default image for that kind of fixture.
		try {
			retval = ImageLoader.getLoader().loadIcon(image);
		} catch (final FileNotFoundException e) { // $codepro.audit.disable logExceptions
			LOGGER.log(Level.SEVERE, "image file images/" + image
					+ " not found");
			retval = defaultFixtIcon;
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading image", e);
			retval = defaultFixtIcon;
		}
		return retval;
	}

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(UnitMemberCellRenderer.class);
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
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "UnitMemberCellRenderer";
	}
}
