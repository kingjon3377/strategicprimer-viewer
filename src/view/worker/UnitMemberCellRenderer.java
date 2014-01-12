package view.worker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
 * The technique for making the background color fill stretch comes from <http://explodingpixels.wordpress.com/2008/06/02/making-a-jtreecellrenderer-fill-the-jtree/#comment-312>.
 *
 * @author Jonathan Lovelace
 */
public class UnitMemberCellRenderer extends DefaultTreeCellRenderer {
	/**
	 * @return the same value as our superclass except infinitely wide.
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height);
	}
	/**
	 * A reference to the tree we're drawing on.
	 */
	private final JTree mTree;
	/**
	 * Constructor.
	 * @param control the tree we're drawing on.
	 */
	public UnitMemberCellRenderer(final JTree control) {
		mTree = control;
	}
	/**
	 * @param x the new X coordinate of this component
	 * @param y the new Y coordinate of this component
	 * @param width the new width of this component
	 * @param height the new height of this component
	 */
	@Override
	public void setBounds(final int x, final int y, final int width, final int height) {
		super.setBounds(x, y, Math.min(mTree.getWidth(), width), height);
	}
	/**
	 * @param tree the tree being rendered
	 * @param value the object in the tree that's being rendered
	 * @param sel whether it's selected
	 * @param expanded whether it's an expanded node
	 * @param leaf whether it's a leaf node
	 * @param row its row in the tree
	 * @param focus whether the tree has the focus
	 * @return a component representing the cell
	 */
	// ESCA-JAVA0138: We have to have this many params to override the
	// superclass method.
	@Override
	public Component getTreeCellRendererComponent(@Nullable final JTree tree,
			@Nullable final Object value, final boolean sel,
			final boolean expanded, final boolean leaf, final int row,
			final boolean focus) {
		if (tree == null || value == null) {
			throw new IllegalStateException("Null tree or value");
		}
		super.getTreeCellRendererComponent(tree, value, sel, expanded,
				leaf, row, focus);
		setBackground(Color.WHITE);
		final Object internal = getNodeValue(value);
		if (internal instanceof HasImage) {
			setIcon(getIcon((HasImage) internal));
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
			setText(builder.toString());
		} else if (internal instanceof Unit) {
			final Unit unit = (Unit) internal;
			final String kind = unit.getKind();
			final String name = unit.getName();
			setText(new StringBuilder(48 + kind.length() + name.length())
					.append("<html><p>").append("Unit of type ").append(kind)
					.append(", named ").append(name).append("</p></html>")
					.toString());
			final String orders = unit.getOrders().toLowerCase();
			if (orders.contains("fixme")) {
				setBackground(Color.PINK);
				setOpaque(true);
			} else if (orders.contains("todo")) {
				setBackground(Color.YELLOW);
				setOpaque(true);
			}
		}
		return this;
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
