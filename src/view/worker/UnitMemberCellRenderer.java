package view.worker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.function.IntSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
import model.workermgmt.WorkerTreeModelAlt;
import model.workermgmt.WorkerTreeModelAlt.KindNode;
import org.eclipse.jdt.annotation.Nullable;
import util.ImageLoader;

import static util.TypesafeLogger.getLogger;

/**
 * A cell renderer for the worker management tree.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class UnitMemberCellRenderer implements TreeCellRenderer {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = getLogger(UnitMemberCellRenderer.class);
	/**
	 * Default renderer, for cases we don't know how to handle.
	 */
	private static final DefaultTreeCellRenderer DEFAULT =
			new DefaultTreeCellRenderer();
	/**
	 * The default background color when selected.
	 */
	private static final Color DEF_BKGD_SELECTED = DEFAULT.getBackgroundSelectionColor();
	/**
	 * The default background when not selected.
	 */
	private static final Color DEF_BKGD_NON_SEL =
			DEFAULT.getBackgroundNonSelectionColor();
	/**
	 * the default fixture icon.
	 */
	private final Icon defaultFixtureIcon = createDefaultFixtureIcon();
	/**
	 * Whether we warn on certain ominous conditions.
	 */
	private final boolean warn;
	/**
	 * How to get the current turn.
	 */
	private final IntSupplier turnSupplier;

	/**
	 * Constructor.
	 * @param turnSource how to get the current turn.
	 * @param check      whether to visually warn on certain ominous conditions
	 */
	public UnitMemberCellRenderer(final IntSupplier turnSource, final boolean check) {
		warn = check;
		turnSupplier = turnSource;
	}

	/**
	 * If the object is a DefaultMutableTreeNode, return its user object; otherwise,
	 * return the given object.
	 * @param value a node of the tree
	 * @return it, unless it's a DefaultMutableTreeNode, in which case return the
	 * associated user object
	 */
	@Nullable
	private static Object getNodeValue(final Object value) {
		if (value instanceof DefaultMutableTreeNode) {
			return ((DefaultMutableTreeNode) value).getUserObject();
		} else {
			return value;
		}
	}

	/**
	 * Convert a list of Jobs into a comma-separated list.
	 * @param iter something containing Jobs
	 * @return a comma-separated list of them, in parentheses, prepended by a space, if
	 * there are any.
	 */
	private static String jobCSL(final Iterable<IJob> iter) {
		final String retval = StreamSupport.stream(iter.spliterator(), false)
									  .filter(job -> !job.isEmpty())
									  .map(job -> String.format("%s %d", job.getName(),
											  Integer.valueOf(job.getLevel())))
									  .collect(Collectors.joining(", ", " (", ")"));
		if (" ()".equals(retval)) {
			return "";
		} else {
			return retval;
		}
	}

	/**
	 * This method exists to log and eat exceptions.
	 *
	 * @param filename the filename of an image
	 * @return the image contained in that file, or null on error
	 */
	@SuppressWarnings("StringConcatenationMissingWhitespace")
	@Nullable
	private static Icon getIconForFile(final String filename) {
		try {
			return ImageLoader.getLoader().loadIcon(filename);
		} catch (final FileNotFoundException | NoSuchFileException except) {
			LOGGER.severe(
					"image file images" + File.separatorChar + filename + " not found");
			LOGGER.log(Level.FINEST, "with stack trace", except);
			return null;
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			LOGGER.log(Level.SEVERE, "I/O error reading image", except);
			return null;
		}
	}

	/**
	 * Create the default icon for fixtures.
	 * @return the default icon for fixtures.
	 */
	private static Icon createDefaultFixtureIcon() {
		/*
		 * The margin we allow around the chit itself in the default image.
		 */
		final int imageSize = 24;
		final BufferedImage temp =
				new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D pen = temp.createGraphics();
		final Color saveColor = pen.getColor();
		pen.setColor(Color.RED);
		final double margin = 0.15;
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
	 * Get and configure the component to render a tree cell.
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
				DEFAULT.getTreeCellRendererComponent(tree, value, selected, expanded,
						leaf, row, hasFocus);
		setComponentColor(component, null);
		final Object internal = getNodeValue(value);
		if (internal instanceof HasImage) {
			((JLabel) component).setIcon(getIcon((HasImage) internal));
		}
		if (internal instanceof IWorker) {
			((JLabel) component).setText(getWorkerText((IWorker) internal));
		} else if (internal instanceof IUnit) {
			final IUnit unit = (IUnit) internal;
			((JLabel) component).setText(unit.getName());
			final String orders =
					unit.getLatestOrders(turnSupplier.getAsInt()).toLowerCase();
			if (warn && orders.contains("fixme") && unit.iterator().hasNext()) {
				setComponentColor(component, Color.PINK);
			} else if (warn && orders.contains("todo") && unit.iterator().hasNext()) {
				setComponentColor(component, Color.YELLOW);
			}
		} else if (warn && (value instanceof WorkerTreeModelAlt.KindNode)) {
			final int turn = turnSupplier.getAsInt();
			setKindColor((KindNode) value, component, turn);
		}
		return component;
	}

	/**
	 * If a "unit kind" node includes units that merit "visual warnings," apply the most
	 * severe such to it as well.
	 * @param node the node being represented
	 * @param component the component that represents it
	 * @param turn the current turn
	 */
	private static void setKindColor(final WorkerTreeModelAlt.KindNode node,
									 final Component component, final int turn) {
		boolean shouldWarn = false;
		for (final TreeNode child : node) {
			if (child instanceof WorkerTreeModelAlt.UnitNode) {
				final IUnit unit = (IUnit) getNodeValue(child);
				if (!unit.iterator().hasNext()) {
					continue;
				}
				final String orders =
						unit.getLatestOrders(turn).toLowerCase();
				if (orders.contains("fixme")) {
					setComponentColor(component, Color.PINK);
					return;
				} else if (orders.contains("todo")) {
					shouldWarn = true;
				}
			}
		}
		if (shouldWarn) {
			setComponentColor(component, Color.YELLOW);
		}
	}

	/**
	 * Get the text to represent a worker.
	 * @param worker a worker
	 * @return a String representing that worker.
	 */
	private static String getWorkerText(final IWorker worker) {
		if (!"human".equals(worker.getRace())) {
			return String.format("<html><p>%s, a %s%s</p></html>", worker.getName(),
					worker.getRace(), jobCSL(worker));
		} else {
			return String.format("<html><p>%s%s</p></html>", worker.getName(),
					jobCSL(worker));
		}
	}

	/**
	 * If the component is a DefaultTreeCellRenderer, set both of its background colors
	 * (if and if not selected) to the given color. Otherwise, do nothing.
	 * @param component a component
	 * @param color a color. If null, use defaults.
	 */
	private static void setComponentColor(final Component component,
										  @Nullable final Color color) {
		if (component instanceof DefaultTreeCellRenderer) {
			if (color == null) {
				((DefaultTreeCellRenderer) component)
						.setBackgroundSelectionColor(DEF_BKGD_SELECTED);
				((DefaultTreeCellRenderer) component)
						.setBackgroundNonSelectionColor(DEF_BKGD_NON_SEL);
			} else {
				((DefaultTreeCellRenderer) component).setBackgroundSelectionColor(color);
				((DefaultTreeCellRenderer) component)
						.setBackgroundNonSelectionColor(color);
			}
		}
	}
	/**
	 * Get the icon for a HasImage object.
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
			return defaultFixtureIcon;
		} else {
			return icon;
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "UnitMemberCellRenderer";
	}
}
