import ceylon.file {
    parsePath
}

import com.pump.window {
    WindowList
}

import java.awt {
    Dimension,
    Component,
    Frame,
    Toolkit
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
    horizontalSplit,
    BorderedPanel,
    verticalSplit,
    createAccelerator,
    InterpolatedLabel
}

import strategicprimer.drivers.common {
    MapChangeListener,
    SPOptions,
    PlayerChangeListener,
    IDriverModel,
    IWorkerModel
}
import strategicprimer.drivers.worker.common {
    IWorkerTreeModel
}
import strategicprimer.model.common {
    DistanceComparator
}
import strategicprimer.model.common.idreg {
    createIDFactory,
    IDRegistrar
}
import strategicprimer.model.common.map {
    Point,
    Player,
    IMapNG
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import strategicprimer.model.common.map.fixtures.towns {
    Fortress
}
import strategicprimer.report {
    reportGenerator,
    IReportNode,
    simpleReportNode
}
import strategicprimer.viewer.drivers.map_viewer {
    NewUnitDialog,
    ViewerModel,
    IViewerModel,
    MapGUI,
    ViewerGUI
}
import strategicprimer.model.impl.xmlio {
    mapIOHelper
}
import lovelace.util.common {
    matchingPredicate,
    matchingValue,
    silentListener,
    narrowedStream,
    defer
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    MenuBroker,
    SPFileChooser { filteredFileChooser }
}
import ceylon.interop.java {
    JavaRunnable
}

"A window to let the player manage units."
class WorkerMgmtFrame extends SPFrame satisfies PlayerChangeListener {
    static class ReportTreeRenderer(DistanceComparator calculator)
            extends DefaultTreeCellRenderer() {
        shared actual Component getTreeCellRendererComponent(JTree tree, Object val,
                Boolean selected, Boolean expanded, Boolean leaf, Integer row,
                Boolean hasFocus) {
            assert (is JComponent retval =
                super.getTreeCellRendererComponent(tree, val, selected, expanded,
                leaf, row, hasFocus));
            if (is IReportNode val) {
                if (exists point = val.point) {
                    retval.toolTipText = calculator.distanceString(point);
                } else {
                    retval.toolTipText = null;
                }
            }
            return retval;
        }
    }

    static IViewerModel getViewerModel(IDriverModel model, MenuBroker menuHandler) {
        if (exists frame = WindowList.getFrames(false, true, true).array.narrow<MapGUI>()
                .find(matchingValue(model.mapFile, compose(IViewerModel.mapFile,
                    MapGUI.mapModel)))) {
            frame.toFront();
            if (frame.extendedState == Frame.iconified) {
                frame.extendedState = Frame.normal;
            }
            return frame.mapModel;
        } else {
            ViewerGUI viewerGUI = ViewerGUI(ViewerModel.copyConstructor(model));
            SwingUtilities.invokeLater(viewerGUI.startDriver);
            return viewerGUI.model;
        }
    }

    static class ReportMouseHandler(JTree reportTree, IDriverModel model,
                MenuBroker menuHandler) extends MouseAdapter() {
        shared actual void mousePressed(MouseEvent event) {
            if (exists selPath = reportTree.getPathForLocation(event.x, event.y),
                    platform.hotKeyPressed(event),
                    is IReportNode node = selPath.lastPathComponent) {
                if (exists point = node.point) {
                    IViewerModel viewerModel = getViewerModel(model, menuHandler);
                    SwingUtilities.invokeLater(() => viewerModel.selection = point); // TODO: Figure out a way to defer() an assignment
                }
            }
        }
    }

    SPOptions options;
    IWorkerModel model;
    MenuBroker menuHandler;
    WorkerMgmtGUI driver;
    shared new (SPOptions options, IWorkerModel model, MenuBroker menuHandler,
            WorkerMgmtGUI driver) extends SPFrame("Worker Management", driver,
                Dimension(640, 480), true,
                (file) => model.addSubordinateMap(mapIOHelper.readMap(file), file)) {
        this.options = options;
        this.model = model;
        this.menuHandler = menuHandler;
        this.driver = driver;
    }

    Point findHQ() {
        variable Point retval = Point.invalidPoint;
        for (location->fixture in narrowedStream<Point, Fortress>(model.map.fixtures)
                .filter(matchingPredicate(matchingValue(model.currentPlayer.playerId, // TODO: Use compose() instead of nesting matchingPredicate() and matchingValue()
                    Player.playerId), compose(Fortress.owner, Entry<Point,
                        Fortress>.item)))) {
            if ("HQ" == fixture.name) {
                return location;
            } else if (location.valid, !retval.valid) {
                retval = location;
            }
        }
        return retval;
    }

    JTree createReportTree(DefaultTreeModel reportModel) {
        JTree report = JTree(reportModel);
        report.rootVisible = true;
        assert (is DefaultMutableTreeNode root = reportModel.root);
        report.expandPath(TreePath(root.path));
        report.cellRenderer = ReportTreeRenderer(DistanceComparator(findHQ(),
            model.mapDimensions));
        ToolTipManager.sharedInstance().registerComponent(report);
        report.addMouseListener(ReportMouseHandler(report, model, menuHandler));
        return report;
    }

    IMapNG mainMap = model.map;
    IDRegistrar idf = createIDFactory(model.allMaps.map(Entry.key));
    NewUnitDialog newUnitFrame = NewUnitDialog(model.currentPlayer, idf);
    IWorkerTreeModel treeModel = WorkerTreeModelAlt(model);

    void markModified() {
        for (subMap->_ in model.allMaps) {
            model.setModifiedFlag(subMap, true);
        }
    }

    value tree = workerTree(treeModel, model.players,
        defer(IMapNG.currentTurn, [mainMap]), true, idf, markModified);
    newUnitFrame.addNewUnitListener(treeModel);

    Integer keyMask = platform.shortcutMask;
    createHotKey(tree, "openUnits",
        // can't use silentListener() because requestFocusInWindow() is overloaded
        (ActionEvent event) => tree.requestFocusInWindow(),
        JComponent.whenInFocusedWindow,
        KeyStroke.getKeyStroke(KeyEvent.vkU, keyMask));

    String playerLabelText(Player player) =>
            "Units belonging to ``player.name``: (``platform.shortcutDescription``U)";
    InterpolatedLabel<[Player]> playerLabel =
            InterpolatedLabel<[Player]>(playerLabelText, [model.currentPlayer]);

    value ordersPanelObj = ordersPanel(mainMap.currentTurn, model.currentPlayer,
        model.getUnits, uncurry(IUnit.getLatestOrders), uncurry(IUnit.setOrders),
        markModified);
    tree.addTreeSelectionListener(ordersPanelObj);

    DefaultTreeModel reportModel = DefaultTreeModel(simpleReportNode(
        "Please wait, loading report ..."));
    void reportGeneratorThread() {
        log.info("About to generate report");
        IReportNode report = reportGenerator.createAbbreviatedReportIR(
            model.subordinateMaps.first?.key else mainMap, model.currentPlayer);
        log.info("Finished generating report");
        // JavaRunnable because of eclipse/ceylon#7379
        SwingUtilities.invokeLater(JavaRunnable(defer(reportModel.setRoot, [report])));
    }
    Thread(reportGeneratorThread).start();

    value resultsPanel = ordersPanel(mainMap.currentTurn, model.currentPlayer,
        model.getUnits, uncurry(IUnit.getResults), null, noop);
    tree.addTreeSelectionListener(resultsPanel);

    JPanel&UnitMemberListener mdp = memberDetailPanel(resultsPanel);
    tree.addUnitMemberListener(mdp);

    void jumpNext(ActionEvent _) {
        assert (is IWorkerTreeModel treeModel = tree.model);
        TreePath? currentSelection = tree.selectionModel.selectionPath;
        if (exists nextPath =
                treeModel.nextProblem(currentSelection, mainMap.currentTurn)) {
            tree.expandPath(nextPath);
            tree.setSelectionRow(tree.getRowForPath(nextPath));
            // TODO: Should select the "TODO" or "FIXME" in the orders window.
        } else {
            log.trace("Nowhere to jump to, about to beep");
            Toolkit.defaultToolkit.beep();
        }
    }
    value jumpButton = listenedButton(
        "Jump to Next Blank (``platform.shortcutDescription``J)", jumpNext);

    StrategyExporter strategyExporter = StrategyExporter(model, options);
    BorderedPanel lowerLeft = BorderedPanel.verticalPanel(
        listenedButton("Add New Unit", silentListener(newUnitFrame.showWindow)),
        ordersPanelObj, listenedButton("Export a proto-strategy",
            (ActionEvent event) => SPFileChooser.save(null, // TODO: convert lambda to named method
                filteredFileChooser(false, ".", null))
                    .call((file) => strategyExporter.writeStrategy( // TODO: convert lambda to named method
                    parsePath(file.string).resource, treeModel.dismissed))));
    contentPane = horizontalSplit(verticalSplit(
        BorderedPanel.verticalPanel(
            BorderedPanel.horizontalPanel(playerLabel, null, jumpButton),
            JScrollPane(tree), null),
        lowerLeft, 2.0 / 3.0),
        verticalSplit(BorderedPanel.verticalPanel(
            JLabel("Contents of the world you know about, for reference:"),
            JScrollPane(createReportTree(reportModel)), null), mdp, 0.6));

    createHotKey(jumpButton, "jumpToNext", jumpNext, JComponent.whenInFocusedWindow,
        createAccelerator(KeyEvent.vkJ));

    TreeExpansionOrderListener expander = TreeExpansionHandler(tree);
    menuHandler.register(silentListener(expander.expandAll), "expand all");
    menuHandler.register(silentListener(expander.collapseAll), "collapse all");
    menuHandler.register((event) => expander.expandSome(2), "expand unit kinds");
    expander.expandAll();

    addWindowListener(object extends WindowAdapter() {
        shared actual void windowClosed(WindowEvent event) => newUnitFrame.dispose();
    });

    model.addMapChangeListener(object satisfies MapChangeListener {
        shared actual void mapChanged() => Thread(reportGeneratorThread).start();
        shared actual void mapMetadataChanged() {}
    });

    {PlayerChangeListener+} pcListeners = [ newUnitFrame, treeModel, ordersPanelObj,
        resultsPanel ];
    shared actual void playerChanged(Player? old, Player newPlayer) {
        for (listener in pcListeners) {
            listener.playerChanged(old, newPlayer);
        }
        Thread(reportGeneratorThread).start();
        playerLabel.arguments = [newPlayer];
    }

    jMenuBar = workerMenu(menuHandler.actionPerformed,
        contentPane, driver);
    pack();
}
