package view.map.key;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;

import model.map.TileType;
import model.viewer.TileViewSize;
import model.viewer.ViewerModel;
import view.map.main.TileUIHelper;
import view.util.BoxPanel;

/**
 * An element of the key.
 *
 * @author Jonathan Lovelace
 */
public final class KeyElement extends BoxPanel {
	/**
	 * UI helper for the terrain type descriptions and colors.
	 */
	private static final TileUIHelper TUI_HELPER = new TileUIHelper();
	/**
	 * Minimum buffer space between elements.
	 */
	private static final int HORIZ_BUFFER = 7;
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
	 * @param type the type this is the key element for.
	 */
	public KeyElement(final int version, final TileType type) {
		super(true);
		addGlue();
		addRigidArea(HORIZ_BUFFER);
		final BoxPanel panel = new BoxPanel(false);
		panel.addRigidArea(4);
		final JComponent tile = new KeyElementComponent(TUI_HELPER.get(version,
				type));
		tile.setMinimumSize(MIN_SIZE);
		tile.setPreferredSize(PREF_SIZE);
		final int tsize = TileViewSize.scaleZoom(ViewerModel.DEF_ZOOM_LEVEL,
				version);
		tile.setMaximumSize(new Dimension(tsize, tsize));
		panel.add(tile);
		panel.addRigidArea(4);
		final JLabel label = new JLabel(TUI_HELPER.getDescription(type));
		panel.add(label);
		panel.addRigidArea(4);
		add(panel);
		addRigidArea(HORIZ_BUFFER);
		addGlue();
		setMinimumSize(new Dimension(Math.max(tile.getMinimumSize().width,
				label.getMinimumSize().width) + HORIZ_BUFFER * 2,
				tile.getMinimumSize().height + label.getMinimumSize().height
						+ 12));
	}
}
