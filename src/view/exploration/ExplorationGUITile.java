package view.exploration;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import model.exploration.ExplorationModel;
import model.exploration.IExplorationModel.Direction;
import model.map.IMap;
import model.map.Point;
import model.map.Tile;
import model.map.TileType;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import util.Pair;
import util.PropertyChangeSource;
import util.PropertyChangeSupportSource;
import view.map.details.FixtureList;
/**
 * A component depicting a tile on the main map and the first secondary map, and its contents in each.
 * @author Jonathan Lovelace
 *
 */
public class ExplorationGUITile extends JPanel implements PropertyChangeListener, PropertyChangeSource {
	/**
	 * The exploration model.
	 */
	private final ExplorationModel model;
	/**
	 * The direction this tile is from the selected tile.
	 */
	private final Direction dir;
	/**
	 * The list of fixtures on the main map's tile.
	 */
	protected final PropertyChangeSupportSource mainPCS = new PropertyChangeSupportSource(this);
	/**
	 * The list of fixtures on the secondary map's tile.
	 */
	protected final PropertyChangeSupportSource secPCS = new PropertyChangeSupportSource(this);
	/**
	 * The first list's panel.
	 */
	protected final JPanel one = new JPanel(new BorderLayout());
	/**
	 * The button that visually represents the tile.
	 */
	protected final DualTileButton button = new DualTileButton();
	/**
	 * The second list's panel.
	 */
	protected final JPanel three = new JPanel(new BorderLayout());
	/**
	 * Constructor.
	 * @param emodel the exploration model behind the GUI this is part of
	 * @param direction indicates which tile surrounding the selected tile this will display.
	 */
	public ExplorationGUITile(final ExplorationModel emodel, final Direction direction) {
		model = emodel;
		dir = direction;
		model.addPropertyChangeListener(this);
		final ExplorationGUITile outer = this;
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				try {
					emodel.move(direction);
				} catch (TraversalImpossibleException except) {
					outer.propertyChange(new PropertyChangeEvent(this, "point", null, emodel.getSelectedUnitLocation()));
				}
			}
		});

		one.add(new JLabel("Fixtures in main map"), BorderLayout.NORTH);
		one.add(new JScrollPane(new FixtureList(this, mainPCS)), BorderLayout.CENTER);
		three.add(new JLabel("Fixtures in first secondary map"), BorderLayout.NORTH);
		three.add(new JScrollPane(new FixtureList(this, secPCS)), BorderLayout.CENTER);

		final BoxLayout layout = new BoxLayout(this, BoxLayout.LINE_AXIS);
		setLayout(layout);
		add(one);
		add(button);
		add(three);
		addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(final ComponentEvent evt) {
				// Do nothing
			}
			@Override
			public void componentResized(final ComponentEvent evt) {
				final int width = (outer.getWidth() - button.getWidth()) / 2;
//				final int width = outer.getWidth() / 3;
				final int height = outer.getHeight();
				one.setSize(width, height);
				three.setSize(width, height);
				outer.revalidate();
			}
			@Override
			public void componentMoved(final ComponentEvent evt) {
				// Do nothing
			}

			@Override
			public void componentHidden(final ComponentEvent evt) {
				// Do nothing.
			}
		});
		setMinimumSize(new Dimension(70, 50));
		setPreferredSize(new Dimension(200, 100));
		setMaximumSize(new Dimension(400, 200));
	}
	/**
	 * @param maximumSize the new maximum size
	 */
	@Override
	public final void setMaximumSize(final Dimension maximumSize) {
		super.setMaximumSize(maximumSize);
		final Dimension max = new Dimension(
				(maximumSize.width - button.getMaximumSize().width) / 2,
				maximumSize.height);
		one.setMaximumSize(max);
		three.setMaximumSize(max);
		revalidate();
	}
	/**
	 * @param minimumSize the new minimum size
	 */
	@Override
	public final void setMinimumSize(final Dimension minimumSize) {
		super.setMinimumSize(minimumSize);
		final Dimension min = new Dimension(
				(minimumSize.width - button.getMinimumSize().width) / 2,
				minimumSize.height);
		one.setMinimumSize(min);
		three.setMinimumSize(min);
		revalidate();
	}
	/**
	 * @param preferredSize the new preferred size
	 */
	@Override
	public final void setPreferredSize(final Dimension preferredSize) {
		super.setPreferredSize(preferredSize);
		final Dimension pref = new Dimension((preferredSize.width - button.getPreferredSize().width) / 2, preferredSize.height);
		one.setPreferredSize(pref);
		three.setPreferredSize(pref);
		revalidate();
	}
	/**
	 * Handle change in selected location.
	 * @param evt the event to handle
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("point".equalsIgnoreCase(evt.getPropertyName())) {
			final Point point = model.getDestination(model.getSelectedUnitLocation(), dir);
			final Tile tileOne = model.getMap().getTile(point);
			final Iterator<Pair<IMap, String>> subs = model.getSubordinateMaps().iterator();
			// ESCA-JAVA0177:
			final Tile tileTwo; // NOPMD
			if (subs.hasNext()) {
				tileTwo = subs.next().first().getTile(point);
			} else {
				tileTwo = new Tile(TileType.NotVisible);
			}
			button.setTiles(tileOne, tileTwo);
			mainPCS.firePropertyChange("tile", null, tileOne);
			secPCS.firePropertyChange("tile", null, tileTwo);
		}
	}
}
