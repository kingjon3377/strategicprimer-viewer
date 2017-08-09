import ceylon.file {
    parsePath
}

import com.pump.window {
    WindowList
}

import java.awt {
    Dimension,
    Component,
    Frame
}
import java.awt.event {
    WindowEvent,
    MouseAdapter,
    MouseEvent,
    WindowAdapter,
    ActionEvent,
    KeyEvent
}
import java.lang {
    Thread
}

import javax.swing {
    JPanel,
    JTree,
    JScrollPane,
    ToolTipManager,
    JComponent,
    KeyStroke,
    JLabel,
    SwingUtilities
}
import javax.swing.tree {
    TreePath,
    DefaultMutableTreeNode,
    DefaultTreeCellRenderer,
    DefaultTreeModel
}

import lovelace.util.jvm {
    platform,
    listenedButton,
    createHotKey,
    FormattedLabel,
    horizontalSplit,
    BorderedPanel,
    verticalSplit
}

import strategicprimer.drivers.common {
    MapChangeListener,
    SPOptions,
    PlayerChangeListener
}
import strategicprimer.drivers.worker.common {
    IWorkerModel,
    IWorkerTreeModel
}
import strategicprimer.model {
    DistanceComparator
}
import strategicprimer.model.idreg {
    createIDFactory
}
import strategicprimer.model.map {
    Point,
    Player,
    IMapNG,
    invalidPoint
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit
}
import strategicprimer.model.map.fixtures.towns {
    Fortress
}
import strategicprimer.report {
    createAbbreviatedReportIR,
    IReportNode,
    simpleReportNode
}
import strategicprimer.viewer.drivers {
    MenuBroker,
    FileChooser,
    filteredFileChooser
}
import strategicprimer.viewer.drivers.map_viewer {
    newUnitDialog,
    IViewerFrame,
    viewerFrame,
    ViewerModel,
    IViewerModel
}
import strategicprimer.model.xmlio {
    readMap
}
import java.nio.file {
    JPath=Path
}
import lovelace.util.common {
    anythingEqual
}
import strategicprimer.drivers.gui.common {
	SPFrame,
	SPDialog
}
"A window to let the player manage units."
SPFrame&PlayerChangeListener workerMgmtFrame(SPOptions options,
        IWorkerModel model, MenuBroker menuHandler) {
    Point findHQ() {
        variable Point retval = invalidPoint;
        for (location in model.map.locations) {
//            for (fixture in model.map.fixtures[location]) { // TODO: syntax sugar once compiler bug fixed
            for (fixture in model.map.fixtures.get(location)) { // TODO: syntax sugar once compiler bug fixed
                if (is Fortress fixture,
                        fixture.owner.playerId == model.currentPlayer.playerId) {
                    if ("HQ" == fixture.name) {
                        return location;
                    } else if (location.valid, !retval.valid) {
                        retval = location;
                    }
                }
            }
        }
        return retval;
    }
    IViewerModel getViewerModel() {
        for (frame in WindowList.getFrames(false, true, true)) {
            if (is IViewerFrame frame,
                    anythingEqual(frame.model.mapFile, model.mapFile)) {
                frame.toFront();
                if (frame.extendedState == Frame.iconified) {
                    frame.extendedState = Frame.normal;
                }
                return frame.model;
            }
        } else {
            SPFrame&IViewerFrame frame = viewerFrame(ViewerModel(model.map,
                model.mapFile), menuHandler.actionPerformed);
            SwingUtilities.invokeLater(() => frame.setVisible(true));
            return frame.model;
        }
    }
    JTree createReportTree(DefaultTreeModel reportModel) {
        JTree report = JTree(reportModel);
        report.rootVisible = true;
        assert (is DefaultMutableTreeNode root = reportModel.root);
        report.expandPath(TreePath(root.path));
        DistanceComparator calculator = DistanceComparator(findHQ(), model.mapDimensions);
        object renderer extends DefaultTreeCellRenderer() {
            shared actual Component getTreeCellRendererComponent(JTree tree, Object val,
                    Boolean selected, Boolean expanded, Boolean leaf, Integer row,
                    Boolean hasFocus) {
                assert (is JComponent retval =
                        super.getTreeCellRendererComponent(tree, val, selected, expanded,
                            leaf, row, hasFocus));
                if (is IReportNode val) {
                    Point point = val.point;
                    if (point.valid) {
                        retval.toolTipText = calculator.distanceString(point);
                    } else {
                        retval.toolTipText = null;
                    }
                }
                return retval;
            }
        }
        report.cellRenderer = renderer;
        ToolTipManager.sharedInstance().registerComponent(report);
        object reportMouseHandler extends MouseAdapter() {
            shared actual void mousePressed(MouseEvent event) {
                if (exists selPath = report.getPathForLocation(event.x, event.y),
                        platform.hotKeyPressed(event),
                        is IReportNode node = selPath.lastPathComponent) {
                    Point point = node.point;
                    if (point.valid) {
                        IViewerModel viewerModel = getViewerModel();
                        SwingUtilities.invokeLater(() => viewerModel.selection = point);
                    }
                }
            }
        }
        report.addMouseListener(reportMouseHandler);
        return report;
    }
    object retval extends SPFrame("Worker Management", model.mapFile,
        Dimension(640, 480))
            satisfies PlayerChangeListener {
        IMapNG mainMap = model.map;
        SPDialog&NewUnitSource&PlayerChangeListener newUnitFrame =
                newUnitDialog(model.currentPlayer,
                    createIDFactory(mainMap));
        IWorkerTreeModel treeModel = WorkerTreeModelAlt(model);
        value tree = workerTree(treeModel, mainMap.players, () => mainMap.currentTurn,
            true);
        newUnitFrame.addNewUnitListener(treeModel);
        Integer keyMask = platform.shortcutMask;
        createHotKey(tree, "openUnits",
                    (ActionEvent event) => tree.requestFocusInWindow(),
            JComponent.whenInFocusedWindow,
            KeyStroke.getKeyStroke(KeyEvent.vkU, keyMask));
        FormattedLabel playerLabel = FormattedLabel("Units belonging to %s: (%sU)",
            model.currentPlayer.name, platform.shortcutDescription);
        value ordersPanelObj = ordersPanel(mainMap.currentTurn, model.currentPlayer,
                    (Player player, String kind) => model.getUnits(player, kind),
                    (IUnit unit, Integer turn) => unit.getLatestOrders(turn),
                    (IUnit unit, Integer turn, String orders) => unit
                        .setOrders(turn, orders));
        tree.addTreeSelectionListener(ordersPanelObj);
        DefaultTreeModel reportModel = DefaultTreeModel(simpleReportNode(
            "Please wait, loading report ..."));
        void reportGeneratorThread() {
            log.info("About to generate report");
            IReportNode report = createAbbreviatedReportIR(mainMap,
                model.currentPlayer);
            log.info("Finished generating report");
            SwingUtilities.invokeLater(() => reportModel.setRoot(report));
        }
        Thread(reportGeneratorThread).start();
        value resultsPanel = ordersPanel(mainMap.currentTurn, model.currentPlayer,
                    (Player player, String kind) => model.getUnits(player, kind),
                    (IUnit unit, Integer turn) => unit.getResults(turn), null);
        tree.addTreeSelectionListener(resultsPanel);
        JPanel&UnitMemberListener mdp = memberDetailPanel(resultsPanel);
        tree.addUnitMemberListener(mdp);
        StrategyExporter strategyExporter = StrategyExporter(model, options);
        BorderedPanel lowerLeft = BorderedPanel.verticalPanel(
            listenedButton("Add New Unit", (event) => newUnitFrame.setVisible(true)),
            ordersPanelObj, listenedButton("Export a proto-strategy",
                        (ActionEvent event) => FileChooser.save(null,
                            filteredFileChooser(false, ".", null))
                    .call((file) => strategyExporter.writeStrategy(
                    parsePath(file.string).resource, treeModel.dismissed))));
        contentPane = horizontalSplit(0.5, 0.5, verticalSplit(2.0 / 3.0, 2.0 / 3.0,
            BorderedPanel.verticalPanel(playerLabel, JScrollPane(tree), null), lowerLeft),
            verticalSplit(0.6, 0.6, BorderedPanel.verticalPanel(
                JLabel("The contents of the world you know about, for reference:"),
                JScrollPane(createReportTree(reportModel)), null),
                mdp));
        TreeExpansionOrderListener expander = TreeExpansionHandler(tree);
        menuHandler.register((event) => expander.expandAll(), "expand all");
        menuHandler.register((event) => expander.collapseAll(), "collapse all");
        menuHandler.register((event) => expander.expandSome(2), "expand unit kinds");
        expander.expandAll();
        object closeListener extends WindowAdapter() {
            shared actual void windowClosed(WindowEvent event) => newUnitFrame.dispose();
        }
        addWindowListener(closeListener);
        model.addMapChangeListener(object satisfies MapChangeListener {
            shared actual void mapChanged() => Thread(reportGeneratorThread).start();
        });
        object reportUpdater satisfies PlayerChangeListener {
            shared actual void playerChanged(Player? old, Player newPlayer) =>
                    Thread(reportGeneratorThread).start();
        }
        {PlayerChangeListener+} pcListeners = { newUnitFrame, treeModel, ordersPanelObj,
            reportUpdater, resultsPanel };
        shared actual void playerChanged(Player? old, Player newPlayer) {
            for (listener in pcListeners) {
                listener.playerChanged(old, newPlayer);
            }
            playerLabel.setArgs(newPlayer.name, platform.shortcutDescription);
        }
        shared actual String windowName = "Worker Management";
        shared actual void acceptDroppedFile(JPath file) =>
                model.addSubordinateMap(readMap(file), file);
        shared actual Boolean supportsDroppedFiles = true;
    }
    retval.jMenuBar = workerMenu(menuHandler.actionPerformed, retval, model);
    retval.pack();
    return retval;
}
