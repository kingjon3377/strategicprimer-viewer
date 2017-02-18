import controller.map.misc {
    ICLIHelper,
    MenuBroker,
    WindowCloser,
    IDFactoryFiller,
    FileChooser
}
import model.misc {
    IDriverModel
}
import model.workermgmt {
    IWorkerModel,
    WorkerModel,
    IWorkerTreeModel,
    WorkerTreeModelAlt
}
import javax.swing {
    SwingUtilities,
    JComponent,
    KeyStroke,
    JFileChooser,
    JScrollPane,
    JLabel,
    JTree,
    ToolTipManager,
    SpinnerNumberModel,
    JPanel,
    JSpinner,
    JTextArea
}
import view.worker {
    NewUnitDialog,
    WorkerTree,
    MemberDetailPanel,
    TreeExpansionHandler,
    WorkerMenu
}
import view.util {
    SPFrame,
    HotKeyCreator,
    FocusRequester,
    FormattedLabel,
    BorderedPanel,
    ListenedButton,
    SplitWithWeights,
    TreeExpansionOrderListener,
    Revertible,
    Applyable,
    BoxPanel
}
import java.nio.file {
    JPath=Path
}
import ceylon.interop.java {
    CeylonIterable
}
import model.map.fixtures {
    UnitMember
}
import model.listeners {
    PlayerChangeListener
}
import java.awt {
    Dimension,
    Component,
    Frame
}
import model.map {
    IMapNG,
    Player,
    DistanceComparator,
    Point,
    PointFactory,
    HasName
}
import util {
    OnMac,
    ActionWrapper
}
import java.awt.event {
    KeyEvent,
    ActionEvent,
    WindowEvent,
    WindowAdapter,
    MouseAdapter,
    MouseEvent,
    KeyAdapter
}
import model.map.fixtures.mobile {
    IUnit,
    ProxyUnit,
    IWorker
}
import javax.swing.tree {
    DefaultTreeModel,
    DefaultMutableTreeNode,
    TreePath,
    DefaultTreeCellRenderer
}
import model.report {
    SimpleReportNode,
    IReportNode
}
import java.lang {
    Thread,
    IllegalStateException
}
import java.util {
    JOptional=Optional
}
import controller.map.report {
    ReportGenerator
}
import model.map.fixtures.towns {
    Fortress
}
import model.viewer {
    IViewerModel,
    ViewerModel
}
import com.bric.window {
    WindowList
}
import view.map.main {
    ViewerFrame
}
import javax.swing.event {
        TreeSelectionListener,
    TreeSelectionEvent
}
import ceylon.file {
    Resource,
    File,
    Nil,
    Writer,
    parsePath
}
import ceylon.collection {
    HashMap,
    ArrayList,
    MutableMap,
    MutableList
}
import model.map.fixtures.mobile.worker {
    IJob
}
import strategicprimer.viewer.about {
    aboutDialog
}
"A panel for the user to enter a unit's orders or read a unit's results."
JPanel&Applyable&Revertible&TreeSelectionListener&PlayerChangeListener ordersPanel(
        Integer currentTurn, variable Player currentPlayer,
        Iterable<IUnit>(Player, String) playerUnits,
        String(IUnit, Integer) ordersSupplier,
        Anything(IUnit, Integer, String)? ordersConsumer) {
    JTextArea area = JTextArea();
    Integer minimumTurn = (currentTurn<0) then currentTurn else - 1;
    Integer maximumTurn = (currentTurn>100) then currentTurn else 100;
    SpinnerNumberModel spinnerModel = SpinnerNumberModel(currentTurn, minimumTurn,
        maximumTurn, 1);
    object retval extends BorderedPanel()
            satisfies Applyable&Revertible&TreeSelectionListener&PlayerChangeListener&HotKeyCreator {
        variable Anything selection = null;
        "If a unit is selected, change its orders to what the user wrote."
        shared actual void apply() {
            if (is IUnit sel = selection) {
                if (exists ordersConsumer) {
                    ordersConsumer(sel, spinnerModel.number.intValue(), area.text);
                }
                parent.parent.repaint();
            }
        }
        "Change the text in the area to either the current orders, if a unit is selected,
          or the empty string, if one is not."
        shared actual void revert() {
            if (is IUnit sel = selection) {
                area.text = ordersSupplier(sel, spinnerModel.number.intValue());
            } else {
                area.text = "";
            }
        }
        if (exists ordersConsumer) {
            ListenedButton applyButton = ListenedButton("Apply", (event) => apply());
            ListenedButton revertButton = ListenedButton("Revert", (event) => revert());
            OnMac.makeButtonsSegmented(applyButton, revertButton);
            JPanel buttonPanel = (OnMac.systemIsMac) then
            BoxPanel.centeredHorizBox(applyButton, revertButton) else horizontalPanel(applyButton, null, revertButton);
            String prefix = OnMac.shortcutDesc;
            setPageStart(horizontalPanel(JLabel("Orders for current selection, if a unit: (``prefix``D)"), null,
                horizontalPanel(null, JLabel("Turn "), JSpinner(spinnerModel))));
            setPageEnd(buttonPanel);
        } else {
            setPageStart(horizontalPanel(JLabel("Results for current selection, if a unit"), null,
                horizontalPanel(null, JLabel("Turn "), JSpinner(spinnerModel))));
        }
        setCenter(JScrollPane(area));
        area.lineWrap = true;
        area.wrapStyleWord = true;
        spinnerModel.addChangeListener((event) => revert());
        object modifiedEnterListener extends KeyAdapter() {
            shared actual void keyPressed(KeyEvent event) {
                if (event.keyCode == KeyEvent.vkEnter, OnMac.isHotkeyPressed(event)) {
                    apply();
                }
            }
        }
        area.addKeyListener(modifiedEnterListener);
        "Handle a changed value in the tree."
        shared actual void valueChanged(TreeSelectionEvent event) {
            if (exists selectedPath = event.newLeadSelectionPath) {
                value sel = selectedPath.lastPathComponent;
                Object temp;
                if (is DefaultMutableTreeNode sel) {
                    temp = sel.userObject;
                } else {
                    temp = sel;
                }
                if (is String temp) {
                    ProxyUnit proxyUnit = ProxyUnit(temp);
                    for (unit in playerUnits(currentPlayer, temp)) {
                        proxyUnit.addProxied(unit);
                    }
                    selection = proxyUnit;
                } else {
                    selection = temp;
                }
                revert();
            }
        }
        shared actual void playerChanged(Player? old, Player newPlayer) {
            currentPlayer = newPlayer;
        }
    }
    Integer keyMask = OnMac.shortcutMask;
    retval.createHotKey(retval, "openOrders", ActionWrapper((event) {
        Boolean newlyGainingFocus = !area.focusOwner;
        area.requestFocusInWindow();
        if (newlyGainingFocus) {
            area.selectAll();
        }
    }), JComponent.whenInFocusedWindow, KeyStroke.getKeyStroke(KeyEvent.vkD, keyMask));
    return retval;
}
"A class to write a proto-strategy to file."
class StrategyExporter(IWorkerModel model, SPOptions options) satisfies PlayerChangeListener {
    variable Player currentPlayer = model.map.currentPlayer;
    shared actual void playerChanged(Player? old, Player newPlayer) =>
            currentPlayer = newPlayer;
    void writeMember(Writer writer, UnitMember? member) {
        if (is IWorker member) {
            writer.write(member.name);
            Iterable<IJob> iter = CeylonIterable(member);
            if (exists first = iter.first) {
                writer.write(" (``first.name`` ``first.level``");
                for (job in iter.rest) {
                    writer.write(", ``job.name`` ``job.level``");
                }
                writer.write(")");
            }
        } else if (exists member) {
            writer.write(member.string);
        }
    }
    shared void writeStrategy(Resource path, {UnitMember*} dismissed) {
        File file;
        if (is Nil path) {
            file = path.createFile();
        } else if (is File path) {
            file = path;
        } else {
            throw IllegalStateException("Can't write to a directory or link");
        }
        try (writer = file.Overwriter()) {
            String playerName = currentPlayer.name;
            Integer turn = model.map.currentTurn;
            {IUnit*} units = CeylonIterable(model.getUnits(currentPlayer))
                .sequence();
            MutableMap<String, MutableList<IUnit>> unitsByKind =
                    HashMap<String, MutableList<IUnit>>();
            for (unit in units) {
                if (!unit.iterator().hasNext(),
                    "false" == options.getArgument("--print-empty")) {
                    continue;
                }
                if (exists list = unitsByKind.get(unit.kind)) {
                    list.add(unit);
                } else {
                    MutableList<IUnit> list = ArrayList<IUnit>();
                    list.add(unit);
                    unitsByKind.put(unit.kind, list);
                }
            }
            MutableMap<IUnit, String> orders = HashMap<IUnit, String>();
            for (kind->list in unitsByKind) {
                for (unit in list) {
                    String unitOrders = unit.getLatestOrders(turn);
                    if (unitOrders == unit.getOrders(turn)) {
                        orders.put(unit, unitOrders);
                    } else {
                        orders.put(unit, "(From turn #``unit
                            .getOrdersTurn(unitOrders)``) ``unitOrders``");
                    }
                }
            }
            writer.writeLine("[``playerName``");
            writer.writeLine("Turn ``turn``]");
            writer.writeLine();
            writer.writeLine();
            writer.writeLine("Inventions: TODO: any?");
            writer.writeLine();
            if (!dismissed.empty) {
                writer.write("Dismissed workers etc.: ``dismissed
                    .first else ""``");
                for (member in dismissed.rest) {
                    writer.write(", ");
                    if (is HasName member) {
                        writer.write(member.name);
                    } else {
                        writer.write(member.string);
                    }
                }
                writer.writeLine();
                writer.writeLine();
            }
            writer.write("Workers:");
            for (kind->list in unitsByKind) {
                writer.writeLine("* ``kind``:");
                for (unit in list) {
                    Iterable<UnitMember> iter = CeylonIterable(unit);
                    writer.write("  - ``unit.name``");
                    if (!iter.empty) {
                        writer.write(" [");
                        writeMember(writer, iter.first);
                        for (member in iter.rest) {
                            writer.write(", ");
                            writeMember(writer, member);
                        }
                        writer.write("]");
                    }
                    writer.writeLine(":");
                    writer.writeLine();
                    if (exists unitOrders = orders.get(unit), !unitOrders.empty) {
                        writer.writeLine(unitOrders);
                    } else {
                        writer.writeLine("TODO");
                    }
                    writer.writeLine();
                    writer.writeLine();
                }
            }
        }
    }
}
"A window to let the player manage units."
SPFrame&PlayerChangeListener&HotKeyCreator workerMgmtFrame(SPOptions options,
        IWorkerModel model, MenuBroker menuHandler) {
    Point findHQ() {
        variable Point retval = PointFactory.invalidPoint;
        for (location in model.map.locations()) {
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
    IViewerModel getViewerModel() {
        for (frame in WindowList.getFrames(false, true, true)) {
            if (is ViewerFrame frame, frame.model.mapFile == model.mapFile) {
                frame.toFront();
                if (frame.extendedState == Frame.iconified) {
                    frame.extendedState = Frame.normal;
                }
                return frame.model;
            }
        } else {
            ViewerFrame frame = ViewerFrame(ViewerModel(model.map, model.mapFile),
                menuHandler);
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
                    if (OnMac.isHotkeyPressed(event), is IReportNode node) {
                        Point point = node.point;
                        if (point.valid) {
                            IViewerModel viewerModel = getViewerModel();
                            SwingUtilities.invokeLater(() =>
                                viewerModel.setSelection(point));
                        }
                    }
                }
            }
        }
        report.addMouseListener(reportMouseHandler);
        return report;
    }
    object retval extends SPFrame("Worker Management", model.mapFile, Dimension(640, 480))
            satisfies PlayerChangeListener&HotKeyCreator {
        IMapNG mainMap = model.map;
        NewUnitDialog newUnitFrame = ConstructorWrapper.newUnitDialog(mainMap.currentPlayer,
            IDFactoryFiller.createFactory(mainMap));
        IWorkerTreeModel treeModel = WorkerTreeModelAlt(mainMap.currentPlayer, model);
        WorkerTree tree = WorkerTree.factory(treeModel, mainMap.players().iterator,
            () => mainMap.currentTurn, true);
        newUnitFrame.addNewUnitListener(treeModel);
        Integer keyMask = OnMac.shortcutMask;
        createHotKey(tree, "openUnits", FocusRequester(tree), JComponent.whenInFocusedWindow,
            KeyStroke.getKeyStroke(KeyEvent.vkU, keyMask));
        FormattedLabel playerLabel = FormattedLabel("Units belonging to %s: (%sU)",
            mainMap.currentPlayer.name, OnMac.shortcutDesc);
        value ordersPanelObj = ordersPanel(mainMap.currentTurn, mainMap.currentPlayer,
            (Player player, String kind) => CeylonIterable(model.getUnits(player, kind)),
            (IUnit unit, Integer turn) => unit.getLatestOrders(turn),
            (IUnit unit, Integer turn, String orders) => unit.setOrders(turn, orders));
        tree.addTreeSelectionListener(ordersPanelObj);
        DefaultTreeModel reportModel = DefaultTreeModel(SimpleReportNode(
            "Please wait, loading report ..."));
        Anything() reportGeneratorThread = () {
            log.info("About to generate report");
            IReportNode report = ReportGenerator.createAbbreviatedReportIR(mainMap,
                mainMap.currentPlayer);
            log.info("Finished generating report");
            SwingUtilities.invokeLater(() => reportModel.setRoot(report));
        };
        Thread(reportGeneratorThread).start();
        value resultsPanel = ordersPanel(mainMap.currentTurn, mainMap.currentPlayer,
            (Player player, String kind) => CeylonIterable(model.getUnits(player, kind)),
            (IUnit unit, Integer turn) => unit.getResults(turn), null);
        tree.addTreeSelectionListener(resultsPanel);
        MemberDetailPanel mdp = MemberDetailPanel(resultsPanel);
        StrategyExporter strategyExporter = StrategyExporter(model, options);
        BorderedPanel lowerLeft = BorderedPanel.verticalPanel(
            ListenedButton("Add New Unit", (event) => newUnitFrame.setVisible(true)),
            ordersPanelObj, ListenedButton("Export a proto-strategy",
                (ActionEvent event) => FileChooser(JOptional.empty<JPath>(),
                    JFileChooser(".", null), FileChooser.FileChooserOperation.save).call(
                    (file) => strategyExporter.writeStrategy(
                        parsePath(file.string).resource,
                        CeylonIterable(treeModel.dismissed())))));
        contentPane = SplitWithWeights.horizontalSplit(0.5, 0.5,
            SplitWithWeights.verticalSplit(2.0 / 3.0, 2.0 / 3.0,
                BorderedPanel.verticalPanel(playerLabel, JScrollPane(tree), null), lowerLeft),
            SplitWithWeights.verticalSplit(0.6, 0.6,
                BorderedPanel.verticalPanel(
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
            playerLabel.setArgs(newPlayer.name, OnMac.shortcutDesc);
        }
        shared actual String windowName = "Worker Management";
    }
    retval.jMenuBar = WorkerMenu(menuHandler, retval, model);
    retval.pack();
    return retval;
}
"A driver to start the worker management GUI."
object workerGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        shortOption = "-w";
        longOption = "--worker";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Manage a player's workers in units";
        longDescription = "Organize the members of a player's units.";
        supportedOptionsTemp = [ "--current-turn=NN", "--print-empty" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IWorkerModel model) {
            MenuBroker menuHandler = MenuBroker();
            menuHandler.register(IOHandler(model, options, cli), "load", "save",
                "save as", "new", "load secondary", "save all", "open in map viewer",
                "open secondary map in map viewer");
            PlayerChangeMenuListener pcml = PlayerChangeMenuListener(model);
            menuHandler.register(pcml, "change current player");
            menuHandler.register((event) => process.exit(0), "quit");
            SwingUtilities.invokeLater(() {
                value frame = workerMgmtFrame(options, model, menuHandler);
                pcml.addPlayerChangeListener(frame);
                menuHandler.register((event) => frame.playerChanged(
                        model.map.currentPlayer, model.map.currentPlayer),
                    "reload tree");
                menuHandler.register(WindowCloser(frame), "close");
                menuHandler.register((event) =>
                    aboutDialog(frame, frame.windowName).setVisible(true), "about");
                frame.setVisible(true);
            });
        } else {
            startDriverOnModel(cli, options, WorkerModel(model));
        }
    }
}
"A command-line program to export a proto-strategy for a player from orders in a map."
object strategyExportCLI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-w";
        longOption = "--worker";
        paramsWanted = ParamCount.one;
        shortDescription = "Export a proto-strategy";
        longDescription = "Create a proto-strategy using orders stored in the map";
        supportedOptionsTemp = [ "--current-turn=NN", "--print-empty", "--export=filename.txt" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IWorkerModel model) {
            if (options.hasOption("--export")) {
                StrategyExporter(model, options).writeStrategy(parsePath(
                        options.getArgument("--export")).resource, {});
            } else {
                throw DriverFailedException(
                    IllegalStateException("--export option is required"),
                    "--export option is required");
            }
        } else {
            startDriverOnModel(cli, options, WorkerModel(model));
        }
    }
}