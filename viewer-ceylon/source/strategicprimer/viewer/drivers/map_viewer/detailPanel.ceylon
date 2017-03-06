import model.misc {
    IDriverModel
}
import model.viewer {
    TileViewSize,
    ViewerModel
}
import javax.swing {
    JPanel,
    JLabel,
    JSplitPane,
    SwingList=JList,
    JScrollPane,
    JComponent
}
import ceylon.interop.java {
    CeylonIterable,
    CeylonList
}
import java.io {
    IOException
}
import lovelace.util.jvm {
    BoxAxis,
    FormattedLabel,
    boxPanel,
    BorderedPanel,
    BoxPanel,
    horizontalSplit
}
import model.map {
    HasPortrait,
    TileType,
    TileFixture,
    Point
}
import javax.swing.event {
    ListSelectionListener,
    ListSelectionEvent
}
import model.listeners {
    VersionChangeListener,
    SelectionChangeListener
}
import java.awt {
    Image,
    Graphics,
    GridLayout,
    Dimension
}
"A panel to show the details of a tile, using a list rather than sub-panels with chits
 for its fixtures."
JComponent&VersionChangeListener&SelectionChangeListener detailPanel(
        variable Integer version, IDriverModel model) {
    JComponent keyElement(Integer version, TileType type) {
        JPanel&BoxPanel retval = boxPanel(BoxAxis.lineAxis);
        retval.addGlue();
        retval.addRigidArea(7);
        JPanel&BoxPanel panel = boxPanel(BoxAxis.pageAxis);
        panel.addRigidArea(4);
        Integer tileSize = TileViewSize.scaleZoom(ViewerModel.defZoomLevel, version);
        panel.add(KeyElementComponent(colorHelper.get(version, type), Dimension(4, 4),
            Dimension(8, 8), Dimension(tileSize, tileSize)));
        panel.addRigidArea(4);
        JLabel label = JLabel(colorHelper.getDescription(type));
        panel.add(label);
        panel.addRigidArea(4);
        retval.add(panel);
        retval.addRigidArea(7);
        retval.addGlue();
        retval.minimumSize = Dimension(largest(4, label.minimumSize.width.integer) + 14,
            16 + label.minimumSize.height.integer);
        return retval;
    }
    object keyPanel extends JPanel(GridLayout(0, 4)) satisfies VersionChangeListener {
        minimumSize = Dimension(
            (keyElement(version, TileType.notVisible).minimumSize.width * 4).integer,
            minimumSize.height.integer);
        preferredSize = minimumSize;
        shared actual void changeVersion(Integer old, Integer newVersion) {
            removeAll();
            for (type in TileType.valuesForVersion(newVersion)) {
                add(keyElement(version, type));
            }
        }
    }
    keyPanel.changeVersion(-1, version);
    FormattedLabel header = FormattedLabel(
        "<html><body><p>Contents of the tile at (%d, %d):</p></body></html>", -1, -1);
    object retval extends JSplitPane(JSplitPane.horizontalSplit, true)
            satisfies VersionChangeListener&SelectionChangeListener {
        shared late SelectionChangeListener delegate;
        shared actual void changeVersion(Integer old, Integer newVersion) =>
                keyPanel.changeVersion(old, newVersion);
        shared actual void selectedPointChanged(Point? old, Point newPoint) {
            delegate.selectedPointChanged(old, newPoint);
            header.setArgs(newPoint.row, newPoint.col);
        }
    }
    SwingList<TileFixture>&SelectionChangeListener fixtureListObject =
            fixtureList(retval, FixtureListModel(model.map, false),
                CeylonIterable(model.map.players()));
    retval.delegate = fixtureListObject;
    object portrait extends JComponent() satisfies ListSelectionListener {
        variable Image? portrait = null;
        shared actual void paintComponent(Graphics pen) {
            super.paintComponent(pen);
            if (exists local = portrait) {
                pen.drawImage(local, 0, 0, width, height, this);
            }
        }
        shared actual void valueChanged(ListSelectionEvent event) {
            List<TileFixture> selections =
                    CeylonList(fixtureListObject.selectedValuesList);
            portrait = null;
            if (!selections.empty, selections.size == 1) {
                if (is HasPortrait selectedValue = selections.first) {
                    String portraitName = selectedValue.portrait;
                    if (!portraitName.empty) {
                        try {
                            portrait = loadImage(portraitName);
                        } catch (IOException except) {
                            log.warn("I/O error loading portrait", except);
                        }
                    }
                }
            }
        }
    }
    fixtureListObject.addListSelectionListener(portrait);
    JPanel listPanel = BorderedPanel.verticalPanel(header, JScrollPane(fixtureListObject),
        null);
    retval.leftComponent = horizontalSplit(0.5, 0.5, listPanel, portrait);
    retval.rightComponent = keyPanel;
    retval.resizeWeight = 0.9;
    retval.setDividerLocation(0.9);
    return retval;
}
