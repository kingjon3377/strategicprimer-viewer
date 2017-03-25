import model.map.fixtures.mobile {
    IUnit
}
import strategicprimer.viewer.model.map.fixtures.towns {
    Fortress
}
import java.awt {
    Dimension,
    Component,
    Frame
}
import strategicprimer.viewer.report.nodes {
    IReportNode,
    SimpleReportNode
}
import ceylon.file {
    parsePath
}
import strategicprimer.viewer.report {
    createAbbreviatedReportIR
}
import javax.swing {
    JPanel,
    JTree,
    JScrollPane,
    ToolTipManager,
    JComponent,
    KeyStroke,
    JLabel,
    SwingUtilities,
    JFileChooser
}
import strategicprimer.viewer.drivers {
    SPFrame,
    MenuBroker,
    SPOptions,
    SPDialog,
    createIDFactory,
    FileChooser
}
import model.map {
    PointFactory,
    Player,
    Point
}
import strategicprimer.viewer.model {
    DistanceComparator
}
import strategicprimer.viewer.model.map {
    IMapNG
}
import javax.swing.tree {
    TreePath,
    DefaultMutableTreeNode,
    DefaultTreeCellRenderer,
    DefaultTreeModel
}
import java.awt.event {
    WindowEvent,
    MouseAdapter,
    MouseEvent,
    WindowAdapter,
    ActionEvent,
    KeyEvent
}
import model.listeners {
    PlayerChangeListener,
    UnitMemberListener,
    NewUnitSource
}
import java.lang {
    Thread
}
import com.bric.window {
    WindowList
}
import strategicprimer.viewer.drivers.map_viewer {
    newUnitDialog,
    IViewerFrame,
    viewerFrame,
    ViewerModel,
    IViewerModel
}
import lovelace.util.jvm {
    platform,
    listenedButton,
    createHotKey,
    FormattedLabel,
    ActionWrapper,
    horizontalSplit,
    BorderedPanel,
    verticalSplit
}
"A window to let the player manage units."
SPFrame&PlayerChangeListener workerMgmtFrame(SPOptions options,
        IWorkerModel model, MenuBroker menuHandler) {
    Point findHQ() {
        variable Point retval = PointFactory.invalidPoint;
        for (location in model.map.locations) {
            for (fixture in model.map.getOtherFixtures(location)) {
                if (is Fortress fixture, fixture.owner == model.map.currentPlayer) {
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
    Boolean anythingEqual(Anything one, Anything two) {
        if (exists one) {
            if (exists two) {
                return one == two;
            } else {
                return false;
            }
        } else if (exists two) {
            return false;
        } else {
            return true;
        }
    }
    IViewerModel getViewerModel() {
        for (frame in WindowList.getFrames(false, true, true)) {
            if (is IViewerFrame frame, anythingEqual(frame.model.mapFile, model.mapFile)) {
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
        DistanceComparator calculator = DistanceComparator(findHQ());
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
                if (exists selPath = report.getPathForLocation(event.x, event.y)) {
                    value node = selPath.lastPathComponent;
                    if (platform.hotKeyPressed(event), is IReportNode node) {
                        Point point = node.point;
                        if (point.valid) {
                            IViewerModel viewerModel = getViewerModel();
                            SwingUtilities.invokeLater(() =>
                                viewerModel.selection = point);
                        }
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
                newUnitDialog(mainMap.currentPlayer,
                    createIDFactory(mainMap));
        IWorkerTreeModel treeModel = WorkerTreeModelAlt(mainMap.currentPlayer, model);
        JTree tree = workerTree(treeModel, mainMap.players, () => mainMap.currentTurn,
            true);
        newUnitFrame.addNewUnitListener(treeModel);
        Integer keyMask = platform.shortcutMask;
        createHotKey(tree, "openUnits", ActionWrapper(
                    (ActionEvent event) => tree.requestFocusInWindow()),
            JComponent.whenInFocusedWindow,
            KeyStroke.getKeyStroke(KeyEvent.vkU, keyMask));
        FormattedLabel playerLabel = FormattedLabel("Units belonging to %s: (%sU)",
            mainMap.currentPlayer.name, platform.shortcutDescription);
        value ordersPanelObj = ordersPanel(mainMap.currentTurn, mainMap.currentPlayer,
                    (Player player, String kind) => model.getUnits(player, kind),
                    (IUnit unit, Integer turn) => unit.getLatestOrders(turn),
                    (IUnit unit, Integer turn, String orders) => unit.setOrders(turn, orders));
        tree.addTreeSelectionListener(ordersPanelObj);
        DefaultTreeModel reportModel = DefaultTreeModel(SimpleReportNode(
            "Please wait, loading report ..."));
        void reportGeneratorThread() {
            log.info("About to generate report");
            IReportNode report = createAbbreviatedReportIR(mainMap,
                mainMap.currentPlayer);
            log.info("Finished generating report");
            SwingUtilities.invokeLater(() => reportModel.setRoot(report));
        }
        Thread(reportGeneratorThread).start();
        value resultsPanel = ordersPanel(mainMap.currentTurn, mainMap.currentPlayer,
                    (Player player, String kind) => model.getUnits(player, kind),
                    (IUnit unit, Integer turn) => unit.getResults(turn), null);
        tree.addTreeSelectionListener(resultsPanel);
        JPanel&UnitMemberListener mdp = memberDetailPanel(resultsPanel);
        StrategyExporter strategyExporter = StrategyExporter(model, options);
        BorderedPanel lowerLeft = BorderedPanel.verticalPanel(
            listenedButton("Add New Unit", (event) => newUnitFrame.setVisible(true)),
            ordersPanelObj, listenedButton("Export a proto-strategy",
                        (ActionEvent event) => FileChooser.save(null, JFileChooser(".", null))
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
        model.addMapChangeListener(() => Thread(reportGeneratorThread).start());
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
    }
    retval.jMenuBar = workerMenu(menuHandler.actionPerformed, retval, model);
    retval.pack();
    return retval;
}
