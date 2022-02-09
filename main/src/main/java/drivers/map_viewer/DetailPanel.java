package drivers.map_viewer;

import static drivers.map_viewer.TileViewSize.scaleZoom;
import static drivers.map_viewer.ImageLoader.ColorHelper;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JComponent;

import java.io.IOException;
import static lovelace.util.BoxPanel.BoxAxis;
import lovelace.util.BorderedPanel;
import lovelace.util.BoxPanel;
import static lovelace.util.FunctionalSplitPane.horizontalSplit;
import lovelace.util.FormattedLabel;
import common.map.HasPortrait;
import common.map.HasOwner;
import common.map.TileFixture;
import common.map.TileType;
import common.map.Point;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Color;
import drivers.common.VersionChangeListener;
import drivers.common.IFixtureEditingModel;
import drivers.common.SelectionChangeListener;
import common.idreg.IDFactoryFiller;
import java.util.Comparator;

import common.map.fixtures.mobile.IUnit;

import worker.common.IFixtureEditHelper;

/**
 * A panel to show the details of a tile, using a list rather than sub-panels with chits for its fixtures.
 *
 * TODO: Separate controller functionality from presentation
 */
/* package */ class DetailPanel extends JSplitPane implements VersionChangeListener, SelectionChangeListener {
	protected static final Logger LOGGER = Logger.getLogger(DetailPanel.class.getName());
	private final IFixtureEditingModel model;
	private final Comparator<TileFixture> sortOrder;


	private final KeyPanel keyPanel;
	private final FormattedLabel header = new FormattedLabel(
		"<html><body><p>Contents of the tile at %s:</p></body></html>", Point.INVALID_POINT);

	public DetailPanel(final int version, final IFixtureEditingModel model, final Comparator<TileFixture> sortOrder) {
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		this.model = model;
		this.sortOrder = sortOrder;
		keyPanel = new KeyPanel(version);
		keyPanel.changeVersion(-1, version);

		final IFixtureEditHelper feh = new FixtureEditHelper(model);

		final FixtureList fixtureListObject = new FixtureList(this,
			new FixtureListModel(model.getMap()::getFixtures, model.getMap()::getBaseTerrain,
				model.getMap()::getRivers, model.getMap()::isMountainous, (point) -> null,
				null, null, null, null, null, null, sortOrder), // TODO: implementations instead of null?
			feh, new IDFactoryFiller().createIDFactory(model.getMap()),
			model.getMap().getPlayers());

		delegate = fixtureListObject;
		portrait = new PortraitComponent(fixtureListObject);
		fixtureListObject.addListSelectionListener(portrait);

		final JPanel listPanel = BorderedPanel.verticalPanel(header, new JScrollPane(fixtureListObject),
			null);
		setLeftComponent(horizontalSplit(listPanel, portrait));
		setRightComponent(keyPanel);
		setResizeWeight(0.9);
		setDividerLocation(0.9);
	}

	private static JComponent keyElement(final int version, @Nullable final TileType type) {
		final BoxPanel retval = new BoxPanel(BoxAxis.LineAxis);
		retval.addGlue();
		retval.addRigidArea(7);

		final BoxPanel panel = new BoxPanel(BoxAxis.PageAxis);
		panel.addRigidArea(4);
		final int tileSize = scaleZoom(ViewerModel.DEFAULT_ZOOM_LEVEL, version);
		final Color color = Optional.ofNullable(ColorHelper.get(version, type)).orElse(Color.white);
		panel.add(new KeyElementComponent(color, new Dimension(4, 4), new Dimension(8, 8),
			new Dimension(tileSize, tileSize)));
		panel.addRigidArea(4);
		final JLabel label = new JLabel(ColorHelper.getDescription(type));
		panel.add(label);
		panel.addRigidArea(4);
		retval.add(panel);
		retval.addRigidArea(7);
		retval.addGlue();
		retval.setMinimumSize(new Dimension(Math.max(4, (int) label.getMinimumSize().getWidth()) + 14,
			16 + (int) label.getMinimumSize().getHeight()));
		return retval;
	}

	private static class KeyPanel extends JPanel implements VersionChangeListener {
		public KeyPanel(final int version) {
			super(new GridLayout(0, 4));
			final Dimension size = new Dimension((int) keyElement(version, null)
					.getMinimumSize().getWidth() * 4,
				(int) getMinimumSize().getHeight());
			setMinimumSize(size);
			setPreferredSize(size);
		}

		@Override
		public void changeVersion(final int old, final int newVersion) {
			removeAll();
			for (final TileType type : TileType.getValuesForVersion(newVersion)) {
				add(keyElement(newVersion, type));
			}
		}
	}

	private final SelectionChangeListener delegate;

	@Override
	public void changeVersion(final int old, final int newVersion) {
		keyPanel.changeVersion(old, newVersion);
	}

	@Override
	public void selectedPointChanged(@Nullable final Point old, final Point newPoint) {
		delegate.selectedPointChanged(old, newPoint);
		header.setArguments(newPoint);
	}

	@Override
	public void selectedUnitChanged(@Nullable final IUnit old, @Nullable final IUnit newUnit) {
		delegate.selectedUnitChanged(old, newUnit);
	}

	@Override
	public void interactionPointChanged() {}

	@Override
	public void cursorPointChanged(@Nullable final Point old, final Point newCursor) {}

	private final PortraitComponent portrait;

	private static class PortraitComponent extends JComponent implements ListSelectionListener {
		private static final long serialVersionUID = 1L;
		private final JList<TileFixture> fixtureListObject;

		public PortraitComponent(final JList<TileFixture> fixtureList) {
			fixtureListObject = fixtureList;
		}

		@Nullable
		private Image portrait = null;

		@Override
		public void paintComponent(final Graphics pen) {
			super.paintComponent(pen);
			if (portrait != null) {
				pen.drawImage(portrait, 0, 0, getWidth(), getHeight(), this);
			}
		}

		@Override
		public void valueChanged(final ListSelectionEvent event) {
			final List<TileFixture> selections =
				fixtureListObject.getSelectedValuesList();
			portrait = null;
			if (selections.size() == 1) {
				final TileFixture selectedValue = selections.get(0);
				if (selectedValue instanceof HasPortrait) {
					final String portraitName = ((HasPortrait) selectedValue).getPortrait();
					if (!portraitName.isEmpty()) {
						try {
							portrait = ImageLoader.loadImage(portraitName);
							repaint();
							return;
						} catch (final IOException except) {
							LOGGER.log(Level.WARNING,
								"I/O error loading portrait", except);
						}
					}
					if (selectedValue instanceof HasOwner) {
						final String playerPortraitName = ((HasOwner) selectedValue).
							getOwner().getPortrait();
						if (!playerPortraitName.isEmpty()) {
							try {
								portrait = ImageLoader
									.loadImage(playerPortraitName);
							} catch (final IOException except) {
								LOGGER.log(Level.WARNING,
									"I/O error loading player portrait",
									except);
							}
						}
					}
				}
				repaint();
			}
		}
	}
}
