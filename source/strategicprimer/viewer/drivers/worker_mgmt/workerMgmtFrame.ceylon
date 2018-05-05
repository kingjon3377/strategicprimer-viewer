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
    SwingUtilities,
	JFrame
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
    verticalSplit,
	ComponentParentStream
}

import strategicprimer.drivers.common {
    MapChangeListener,
    SPOptions,
    PlayerChangeListener,
	IDriverModel
}
import strategicprimer.drivers.worker.common {
    IWorkerModel,
    IWorkerTreeModel
}
import strategicprimer.model {
    DistanceComparator
}
import strategicprimer.model.idreg {
    createIDFactory,
    IDRegistrar
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
    reportGenerator,
    IReportNode,
    simpleReportNode
}
import strategicprimer.viewer.drivers {
    MenuBroker,
    FileChooser,
    IOHandler { filteredFileChooser }
}
import strategicprimer.viewer.drivers.map_viewer {
    newUnitDialog,
    ViewerFrame,
    ViewerModel,
    IViewerModel,
	MapGUI
}
import strategicprimer.model.xmlio {
    mapIOHelper
}
import lovelace.util.common {
    anythingEqual,
	matchingPredicate,
	matchingValue,
	silentListener
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    SPDialog
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
	static IViewerModel getViewerModel(IDriverModel model, MenuBroker menuHandler) {
		for (frame in WindowList.getFrames(false, true, true)) {
			if (is MapGUI frame,
				anythingEqual(frame.mapModel.mapFile, model.mapFile)) {
				frame.toFront();
				if (frame.extendedState == Frame.iconified) {
					frame.extendedState = Frame.normal;
				}
				return frame.mapModel;
			}
		} else {
			SPFrame&MapGUI frame = ViewerFrame(ViewerModel(model.map,
				model.mapFile), menuHandler.actionPerformed);
			SwingUtilities.invokeLater(() => frame.setVisible(true));
			return frame.mapModel;
		}
	}
	static class ReportMouseHandler(JTree reportTree, IDriverModel model, MenuBroker menuHandler)
			extends MouseAdapter() {
		shared actual void mousePressed(MouseEvent event) {
			if (exists selPath = reportTree.getPathForLocation(event.x, event.y),
				platform.hotKeyPressed(event),
				is IReportNode node = selPath.lastPathComponent) {
				Point point = node.point;
				if (point.valid) {
					IViewerModel viewerModel = getViewerModel(model, menuHandler);
					SwingUtilities.invokeLater(() => viewerModel.selection = point);
				}
			}
		}
	}
	SPOptions options;
	IWorkerModel model;
	MenuBroker menuHandler;
	shared new (SPOptions options, IWorkerModel model, MenuBroker menuHandler)
			extends SPFrame("Worker Management", model.mapFile,
                Dimension(640, 480), true,
                (file) => model.addSubordinateMap(mapIOHelper.readMap(file), file)) {
		this.options = options;
		this.model = model;
		this.menuHandler = menuHandler;
	}

	Point findHQ() {
		variable Point retval = invalidPoint;
		for (location in model.map.locations) {
			//            for (fixture in model.map.fixtures[location].narrow<Fortress>().filter(matchingPredicate(matchingValue(model.currentPlayer.playerId, Player.playerId), Fortress.owner))) { // TODO: syntax sugar once compiler bug fixed
			for (fixture in model.map.fixtures.get(location).narrow<Fortress>().filter(matchingPredicate(matchingValue(model.currentPlayer.playerId, Player.playerId), Fortress.owner))) { // TODO: syntax sugar once compiler bug fixed
				if ("HQ" == fixture.name) {
					return location;
				} else if (location.valid, !retval.valid) {
					retval = location;
				}
			}
		}
		return retval;
	}
	JTree createReportTree(DefaultTreeModel reportModel) {
		JTree report = JTree(reportModel);
		report.rootVisible = true;
		assert (is DefaultMutableTreeNode root = reportModel.root);
		report.expandPath(TreePath(root.path));
		report.cellRenderer = ReportTreeRenderer(DistanceComparator(findHQ(), model.mapDimensions));
		ToolTipManager.sharedInstance().registerComponent(report);
		report.addMouseListener(ReportMouseHandler(report, model, menuHandler));
		return report;
	}
	IMapNG mainMap = model.map;
	IDRegistrar idf = createIDFactory(model.allMaps.map(Tuple.first));
	SPDialog&NewUnitSource&PlayerChangeListener newUnitFrame =
			newUnitDialog(model.currentPlayer,
		idf);
	IWorkerTreeModel treeModel = WorkerTreeModelAlt(model);
	value tree = workerTree(treeModel, model.players, () => mainMap.currentTurn,
		true, idf);
	newUnitFrame.addNewUnitListener(treeModel);
	Integer keyMask = platform.shortcutMask;
	createHotKey(tree, "openUnits",
		(ActionEvent event) => tree.requestFocusInWindow(), // can't use silentListener() because requestFocusInWindow() is overloaded
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
		IReportNode report = reportGenerator.createAbbreviatedReportIR(
			model.subordinateMaps.first?.first else mainMap, model.currentPlayer);
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
	menuHandler.register(silentListener(expander.expandAll), "expand all");
	menuHandler.register(silentListener(expander.collapseAll), "collapse all");
	menuHandler.register((event) => expander.expandSome(2), "expand unit kinds");
	expander.expandAll();
	addWindowListener(object extends WindowAdapter() {
		shared actual void windowClosed(WindowEvent event) => newUnitFrame.dispose();
	});
	model.addMapChangeListener(object satisfies MapChangeListener {
		shared actual void mapChanged() => Thread(reportGeneratorThread).start();
	});
	object reportUpdater satisfies PlayerChangeListener {
		shared actual void playerChanged(Player? old, Player newPlayer) =>
				Thread(reportGeneratorThread).start();
	}
	{PlayerChangeListener+} pcListeners = [ newUnitFrame, treeModel, ordersPanelObj,
		reportUpdater, resultsPanel ];
	shared actual void playerChanged(Player? old, Player newPlayer) {
		for (listener in pcListeners) {
			listener.playerChanged(old, newPlayer);
		}
		playerLabel.setArgs(newPlayer.name, platform.shortcutDescription);
	}
	assert (exists thisReference = ComponentParentStream(contentPane).narrow<JFrame>().first);
	jMenuBar = workerMenu(menuHandler.actionPerformed,
		thisReference, model);
	pack();
}
