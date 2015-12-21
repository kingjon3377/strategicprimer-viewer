package view.map.key;

import model.map.TileType;
import model.viewer.TileViewSize;
import org.eclipse.jdt.annotation.Nullable;
import view.map.main.TileUIHelper;
import view.util.BoxPanel;

import javax.swing.*;
import java.awt.*;

import static model.viewer.ViewerModel.DEF_ZOOM_LEVEL;

/**
 * An element of the key.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
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
public final class KeyElement extends BoxPanel {
	/**
	 * UI helper for the terrain type descriptions and colors.
	 */
	private static final TileUIHelper TUIH = new TileUIHelper();
	/**
	 * Minimum buffer space between elements.
	 */
	private static final int HORIZ_BUF = 7;
	/**
	 * Minimum size of a colored area.
	 */
	private static final Dimension MIN_SIZE = new Dimension(4, 4);
	/**
	 * Preferred size of a colored area.
	 */
	private static final Dimension PREF_SIZE = new Dimension(8, 8);

	/**
	 * Constructor.
	 *
	 * @param version the map version
	 * @param type    the type this is the key element for.
	 */
	public KeyElement(final int version, final TileType type) {
		super(true);
		addGlue();
		addRigidArea(HORIZ_BUF);
		final BoxPanel panel = new BoxPanel(false);
		panel.addRigidArea(4);
		final int tsize = TileViewSize.scaleZoom(DEF_ZOOM_LEVEL, version);
		panel.add(new KeyElementComponent(TUIH.get(version, type), MIN_SIZE,
				                                 PREF_SIZE, new Dimension(tsize,
						                                                         tsize)));
		panel.addRigidArea(4);
		final JLabel label = new JLabel(TUIH.getDescription(type));
		panel.add(label);
		panel.addRigidArea(4);
		add(panel);
		addRigidArea(HORIZ_BUF);
		addGlue();
		final Dimension lsize = label.getMinimumSize();
		setMinimumSize(new Dimension(Math.max(MIN_SIZE.width, lsize.width)
				                             + (HORIZ_BUF * 2),
				                            MIN_SIZE.height + lsize.height + 12));
	}

	/**
	 * The main component of a KeyElement.
	 *
	 * @author Jonathan Lovelace
	 */
	private static final class KeyElementComponent extends JComponent {
		/**
		 * The color of this Component.
		 */
		private final Color color;

		/**
		 * Constructor.
		 *
		 * @param col  the color to make the component.
		 * @param min  the component's minimum size
		 * @param pref the component's preferred size
		 * @param max  the component's maximum size
		 */
		protected KeyElementComponent(final Color col, final Dimension min,
		                              final Dimension pref, final Dimension max) {
			color = col;
			setMinimumSize(min);
			setPreferredSize(pref);
			setMaximumSize(max);
		}

		/**
		 * @param pen the graphics context
		 */
		@Override
		public void paint(@Nullable final Graphics pen) {
			if (pen == null) {
				throw new IllegalArgumentException("Graphics cannot be null");
			}
			final Graphics context = pen.create();
			try {
				context.setColor(color);
				context.fillRect(0, 0, getWidth(), getHeight());
			} finally {
				context.dispose();
			}
		}
	}
}
