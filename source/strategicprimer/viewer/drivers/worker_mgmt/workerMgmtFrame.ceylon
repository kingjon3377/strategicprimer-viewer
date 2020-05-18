import ceylon.file {
    parsePath
}

import java.awt {
    Dimension,
    Toolkit
}
import java.awt.event {
    WindowEvent,
    WindowAdapter,
    ActionEvent,
    KeyEvent
}

import javax.swing {
    JPanel,
    JScrollPane,
    JComponent,
    KeyStroke,
    SwingUtilities
}
import javax.swing.tree {
    TreePath
}

import lovelace.util.jvm {
    platform,
    ListenedButton,
    createHotKey,
    horizontalSplit,
    BorderedPanel,
    verticalSplit,
    createAccelerator,
    InterpolatedLabel
}

import strategicprimer.drivers.common {
    SPOptions,
    PlayerChangeListener,
    IWorkerModel
}
import strategicprimer.drivers.worker.common {
    IWorkerTreeModel
}
import strategicprimer.model.common.idreg {
    createIDFactory,
    IDRegistrar
}
import strategicprimer.model.common.map {
    Player,
    IMapNG
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import strategicprimer.viewer.drivers.map_viewer {
    NewUnitDialog
}
import strategicprimer.model.impl.xmlio {
    mapIOHelper
}
import lovelace.util.common {
    silentListener,
    defer,
    PathWrapper
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    MenuBroker,
    SPFileChooser { filteredFileChooser }
}
import strategicprimer.viewer.drivers.worker_mgmt.orderspanel {
    ordersPanel
}

"A window to let the player manage units."
class WorkerMgmtFrame extends SPFrame satisfies PlayerChangeListener {
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

    value resultsPanel = ordersPanel(mainMap.currentTurn, model.currentPlayer,
        model.getUnits, uncurry(IUnit.getResults), null, noop);
    tree.addTreeSelectionListener(resultsPanel);

    JPanel&UnitMemberListener mdp = memberDetailPanel(resultsPanel);
    tree.addUnitMemberListener(mdp);

    void selectTodoText() {
        for (string in ["fixme", "todo", "xxx"]) {
            if (ordersPanelObj.selectText(string)) {
                break;
            }
        }
    }

    void jumpNext() {
        assert (is IWorkerTreeModel treeModel = tree.model);
        TreePath? currentSelection = tree.selectionModel.selectionPath;
        if (exists nextPath =
                treeModel.nextProblem(currentSelection, mainMap.currentTurn)) {
            tree.expandPath(nextPath);
            tree.setSelectionRow(tree.getRowForPath(nextPath));
            // selectTodoText isn't inlined because we need to make sure the
            // tree-selection listeners get updated
            SwingUtilities.invokeLater(selectTodoText);
        } else {
            log.trace("Nowhere to jump to, about to beep");
            Toolkit.defaultToolkit.beep();
        }
    }
    void jumpNextWrapped() => SwingUtilities.invokeLater(jumpNext);
    value jumpButton = ListenedButton(
        "Jump to Next Blank (``platform.shortcutDescription``J)", jumpNextWrapped);

    StrategyExporter strategyExporter = StrategyExporter(model, options);
    void writeStrategy(PathWrapper file) => strategyExporter
        .writeStrategy(parsePath(file.string).resource, treeModel.dismissed);
    void strategyWritingListener() => SPFileChooser.save(null,
        filteredFileChooser(false, ".", null)).call(writeStrategy);
    BorderedPanel lowerLeft = BorderedPanel.verticalPanel(
        ListenedButton("Add New Unit", newUnitFrame.showWindow), ordersPanelObj,
        ListenedButton("Export a proto-strategy", strategyWritingListener));
    contentPane = horizontalSplit(verticalSplit(
        BorderedPanel.verticalPanel(
            BorderedPanel.horizontalPanel(playerLabel, null, jumpButton),
            JScrollPane(tree), null),
        lowerLeft, 2.0 / 3.0), mdp);

    createHotKey(jumpButton, "jumpToNext",
        silentListener(jumpNextWrapped),
        JComponent.whenInFocusedWindow, createAccelerator(KeyEvent.vkJ));

    TreeExpansionOrderListener expander = TreeExpansionHandler(tree);
    void expandTwo() => expander.expandSome(2);

    menuHandler.register(silentListener(expander.expandAll), "expand all");
    menuHandler.register(silentListener(expander.collapseAll), "collapse all");
    menuHandler.register(silentListener(expandTwo), "expand unit kinds");
    expander.expandAll();

    addWindowListener(object extends WindowAdapter() {
        shared actual void windowClosed(WindowEvent event) => newUnitFrame.dispose();
    });

    {PlayerChangeListener+} pcListeners = [ newUnitFrame, treeModel, ordersPanelObj,
        resultsPanel ];
    shared actual void playerChanged(Player? old, Player newPlayer) {
        for (listener in pcListeners) {
            listener.playerChanged(old, newPlayer);
        }
        playerLabel.arguments = [newPlayer];
    }

    jMenuBar = workerMenu(menuHandler.actionPerformed,
        contentPane, driver);
    pack();
}
