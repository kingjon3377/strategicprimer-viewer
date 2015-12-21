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
import javax.swing.tree.TreeNode;
import model.map.HasImage;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.worker.IJob;
import model.workermgmt.WorkerTreeModelAlt.KindNode;
import model.workermgmt.WorkerTreeModelAlt.UnitNode;
import org.eclipse.jdt.annotation.Nullable;
import util.ImageLoader;
import util.NullCleaner;
import util.TypesafeLogger;

/**
 * A cell renderer for the worker management tree.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class UnitMemberCellRenderer implements TreeCellRenderer {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			                                     .getLogger(UnitMemberCellRenderer
					                                                .class);
	/**
	 * the default fixture icon.
	 */
	private final Icon defaultFixtIcon = createDefaultFixtureIcon();

	/**
	 * Default renderer, for cases we don't know how to handle.
	 */
	private static final DefaultTreeCellRenderer DFLT =
			new DefaultTreeCellRenderer();
	/**
	 * The default background color when selected.
	 */
	private static final Color DEF_BKGD_SELECTED = NullCleaner
			                                               .assertNotNull(
					                                               DFLT
							                                               .getBackgroundSelectionColor());
	/**
	 * The default background when not selected.
	 */
	private static final Color DEF_BKGD_NON_SEL = NullCleaner
			                                              .assertNotNull(
					                                              DFLT
							                                              .getBackgroundNonSelectionColor());
	/**
	 * Whether we warn on certain ominous conditions.
	 */
	private final boolean warn;

	/**
	 * @param check whether to visually warn on certain ominous conditions
	 */
	public UnitMemberCellRenderer(final boolean check) {
		warn = check;
	}

	/**
	 * @param tree     the tree being rendered
	 * @param value    the object in the tree that's being rendered
	 * @param selected whether it's selected
	 * @param expanded whether it's an expanded node
	 * @param leaf     whether it's a leaf node
	 * @param row      its row in the tree
	 * @param hasFocus whether the tree has the focus
	 * @return a component representing the cell
	 */
	@Override
	public Component getTreeCellRendererComponent(@Nullable final JTree tree,
	                                              @Nullable final Object value,
	                                              final boolean selected,
	                                              final boolean expanded,
	                                              final boolean leaf, final int row,
	                                              final boolean hasFocus) {
		assert (tree != null) && (value != null) :
				"UnitMemberCellRenderer passed a null tree or value";
		final Component component =
				NullCleaner.assertNotNull(DFLT.getTreeCellRendererComponent(
						tree, value, selected, expanded, leaf, row, hasFocus));
		((DefaultTreeCellRenderer) component)
				.setBackgroundSelectionColor(DEF_BKGD_SELECTED);
		((DefaultTreeCellRenderer) component)
				.setBackgroundNonSelectionColor(DEF_BKGD_NON_SEL);
		final Object internal = getNodeValue(value);
		if (internal instanceof HasImage) {
			((JLabel) component).setIcon(getIcon((HasImage) internal));
		}
		if (internal instanceof IWorker) {
			final IWorker worker = (IWorker) internal;
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
		} else if (internal instanceof IUnit) {
			final IUnit unit = (IUnit) internal;
			((JLabel) component).setText(unit.getName());
			final String orders = unit.getOrders().toLowerCase();
			if (warn && orders.contains("fixme") && unit.iterator().hasNext()) {
				((DefaultTreeCellRenderer) component)
						.setBackgroundSelectionColor(Color.PINK);
				((DefaultTreeCellRenderer) component)
						.setBackgroundNonSelectionColor(Color.PINK);
			} else if (warn && orders.contains("todo")
					           && unit.iterator().hasNext()) {
				((DefaultTreeCellRenderer) component)
						.setBackgroundSelectionColor(Color.YELLOW);
				((DefaultTreeCellRenderer) component)
						.setBackgroundNonSelectionColor(Color.YELLOW);
			}
		} else if (warn && (value instanceof KindNode)) {
			boolean shouldWarn = false;
			boolean shouldErr = false;
			for (final TreeNode node : (KindNode) value) {
				if (node instanceof UnitNode) {
					final IUnit unit = (IUnit) NullCleaner
							                           .assertNotNull(getNodeValue
									                                          (node));
					final String orders = unit.getOrders().toLowerCase();
					if (orders.contains("fixme") && unit.iterator().hasNext()) {
						shouldErr = true;
						break;
					} else if (orders.contains("todo")
							           && unit.iterator().hasNext()) {
						shouldWarn = true;
					}
				}
			}

			if (shouldErr) {
				((DefaultTreeCellRenderer) component)
						.setBackgroundSelectionColor(Color.PINK);
				((DefaultTreeCellRenderer) component)
						.setBackgroundNonSelectionColor(Color.PINK);
			} else if (shouldWarn) {
				((DefaultTreeCellRenderer) component)
						.setBackgroundSelectionColor(Color.YELLOW);
				((DefaultTreeCellRenderer) component)
						.setBackgroundNonSelectionColor(Color.YELLOW);
			}
		}
		return component;
	}

	/**
	 * @param value a node of the tree
	 * @return it, unless it's a DefaultMutableTreeNode, in which case return the
	 * associated user object
	 */
	@Nullable
	private static Object getNodeValue(final Object value) {
		if (value instanceof DefaultMutableTreeNode) {
			return ((DefaultMutableTreeNode) value).getUserObject(); // NOPMD
		} else {
			return value;
		}
	}

	/**
	 * @param iter something containing Jobs
	 * @return a comma-separated list of them, in parentheses, prepended by a space, if
	 * there are any.
	 */
	private static String jobCSL(final Iterable<IJob> iter) {
		if (iter.iterator().hasNext()) {
			final StringBuilder builder = new StringBuilder(100);
			builder.append(" (");
			boolean first = true;
			for (final IJob job : iter) {
				if (first) {
					first = false;
				} else {
					builder.append(", ");
				}
				builder.append(job.getName()).append(' ').append(job.getLevel());
			}
			builder.append(')');
			return NullCleaner.assertNotNull(builder.toString()); // NOPMD
		} else {
			return "";
		}
	}

	/**
	 * @param obj a HasImage object
	 * @return an icon representing it
	 */
	private Icon getIcon(final HasImage obj) {
		final String image = obj.getImage();
		if (!image.isEmpty()) {
			final Icon icon = getIconForFile(image);
			if (icon != null) {
				return icon;
			}
		}
		final Icon icon = getIconForFile(obj.getDefaultImage());
		if (icon == null) {
			return defaultFixtIcon;
		} else {
			return icon;
		}
	}

	/**
	 * This method exists to log and eat exceptions.
	 *
	 * @param filename the filename of an image
	 * @return the image contained in that file, or null on error
	 */
	@Nullable
	private static Icon getIconForFile(final String filename) {
		try {
			return ImageLoader.getLoader().loadIcon(filename);
		} catch (final FileNotFoundException except) {
			LOGGER.severe("image file images/" + filename + " not found");
			LOGGER.log(Level.FINEST, "with stack trace", except);
			return null;
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error reading image", except);
			return null;
		}
	}

	/**
	 * @return the default icon for fixtures.
	 */
	private static Icon createDefaultFixtureIcon() {
		/*
		 * The margin we allow around the chit itself in the default image.
		 */
		final int imageSize = 24; // NOPMD
		final BufferedImage temp = new BufferedImage(imageSize, imageSize,
				                                            BufferedImage.TYPE_INT_ARGB);
		final Graphics2D pen = temp.createGraphics();
		final Color saveColor = pen.getColor();
		pen.setColor(Color.RED);
		final double margin = 0.15; // NOPMD
		pen.fillRoundRect((int) Math.round(imageSize * margin) + 1,
				(int) Math.round(imageSize * margin) + 1,
				(int) Math.round(imageSize * (1.0 - (margin * 2.0))),
				(int) Math.round(imageSize * (1.0 - (margin * 2.0))),
				(int) Math.round(imageSize * (margin / 2.0)),
				(int) Math.round(imageSize * (margin / 2.0)));
		pen.setColor(saveColor);
		pen.fillRoundRect(
				(int) Math.round((imageSize / 2.0) - (imageSize * margin)) + 1,
				(int) Math.round((imageSize / 2.0) - (imageSize * margin)) + 1,
				(int) Math.round(imageSize * margin * 2.0),
				(int) Math.round(imageSize * margin * 2.0),
				(int) Math.round((imageSize * margin) / 2.0),
				(int) Math.round((imageSize * margin) / 2.0));
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
