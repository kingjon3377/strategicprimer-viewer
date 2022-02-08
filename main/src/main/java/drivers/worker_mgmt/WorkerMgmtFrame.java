package drivers.worker_mgmt;

import java.util.logging.Level;
import java.io.IOException;
import java.util.logging.Logger;
import drivers.gui.common.SPFileChooser;
import java.util.stream.StreamSupport;
import java.util.Collections;
import javax.swing.JButton;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JScrollPane;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import static lovelace.util.MenuUtils.createHotKey;
import static lovelace.util.MenuUtils.createAccelerator;
import static lovelace.util.FunctionalSplitPane.horizontalSplit;
import static lovelace.util.FunctionalSplitPane.verticalSplit;
import lovelace.util.Platform;
import lovelace.util.ListenedButton;
import lovelace.util.BorderedPanel;
import lovelace.util.FormattedLabel;

import drivers.common.SPOptions;
import drivers.common.PlayerChangeListener;
import drivers.common.IWorkerModel;
import worker.common.IWorkerTreeModel;
import common.idreg.IDFactoryFiller;
import common.idreg.IDRegistrar;
import common.map.Player;
import common.map.IMapNG;
import common.map.fixtures.mobile.IUnit;
import drivers.map_viewer.NewUnitDialog;
import impl.xmlio.MapIOHelper;

import drivers.gui.common.SPFrame;
import drivers.gui.common.MenuBroker;
import static drivers.gui.common.SPFileChooser.filteredFileChooser;

import drivers.worker_mgmt.orderspanel.OrdersPanel;

/**
 * A window to let the player manage units.
 */
/* package */ class WorkerMgmtFrame extends SPFrame implements PlayerChangeListener {
	private static final Logger LOGGER = Logger.getLogger(WorkerMgmtFrame.class.getName());
	private final SPOptions options;
	private final IWorkerModel model;
	private final MenuBroker menuHandler;
	private final WorkerMgmtGUI driver;
	private final WorkerTree tree;

	public WorkerMgmtFrame(final SPOptions options, final IWorkerModel model, final MenuBroker menuHandler,
	                       final WorkerMgmtGUI driver) {
		super("Worker Management", driver, new Dimension(640, 480), true,
			(file) -> model.addSubordinateMap(MapIOHelper.readMap(file)));
		this.options = options;
		this.model = model;
		this.menuHandler = menuHandler;
		this.driver = driver;
		mainMap = model.getMap();
		IDRegistrar idf = new IDFactoryFiller().createIDFactory(model.streamAllMaps()
				.toArray(IMapNG[]::new));
		newUnitFrame = new NewUnitDialog(model.getCurrentPlayer(), idf);
		IWorkerTreeModel treeModel = new WorkerTreeModelAlt(model); // TODO: Try with WorkerTreeModel again?

		tree = new WorkerTree(treeModel, model.getPlayers(),
				mainMap::getCurrentTurn, true, idf);
		newUnitFrame.addNewUnitListener(treeModel);

		int keyMask = Platform.SHORTCUT_MASK;
		createHotKey(tree, "openUnits", ignored -> tree.requestFocusInWindow(),
			JComponent.WHEN_IN_FOCUSED_WINDOW,
			KeyStroke.getKeyStroke(KeyEvent.VK_U, keyMask));

		playerLabel = new FormattedLabel(String.format("Units belonging to %%s: (%sU)",
			Platform.SHORTCUT_DESCRIPTION), model.getCurrentPlayer().getName());
		ordersPanelObj = new OrdersPanel("Orders", mainMap.getCurrentTurn(),
			model.getCurrentPlayer(), model::getUnits, IUnit::getLatestOrders,
			model::setUnitOrders, WorkerMgmtFrame::isCurrent); // TODO: inline isCurrent?
		tree.addTreeSelectionListener(ordersPanelObj);

		OrdersPanel.IIsCurrent trueSupplier = (unit, turn) -> true;

		OrdersPanel.IOrdersConsumer resultsSupplier;
		if ("true".equals(options.getArgument("--edit-results"))) {
			resultsSupplier = model::setUnitResults;
		} else {
			resultsSupplier = null;
		}
		OrdersPanel resultsPanel = new OrdersPanel("Results", mainMap.getCurrentTurn(),
			model.getCurrentPlayer(), model::getUnits, IUnit::getResults,
			resultsSupplier, trueSupplier);
		tree.addTreeSelectionListener(resultsPanel);

		NotesPanel notesPanelInstance = new NotesPanel(model.getMap().getCurrentPlayer());
		tree.addUnitMemberListener(notesPanelInstance);

		MemberDetailPanel mdp = new MemberDetailPanel(resultsPanel, notesPanelInstance);
		tree.addUnitMemberListener(mdp);

		JButton jumpButton = new ListenedButton(String.format("Jump to Next Blank (%sJ)",
			Platform.SHORTCUT_DESCRIPTION), ignored -> jumpNextWrapped());

		strategyExporter = new StrategyExporter(model, options);

		BorderedPanel lowerLeft = BorderedPanel.verticalPanel(
			new ListenedButton("Add New Unit", ignored -> newUnitFrame.showWindow()),
			ordersPanelObj,
			new ListenedButton("Export a proto-strategy", ignored -> strategyWritingListener()));
		setContentPane(horizontalSplit(verticalSplit(
			BorderedPanel.verticalPanel(
				BorderedPanel.horizontalPanel(playerLabel, null, jumpButton),
				new JScrollPane(tree), null),
			lowerLeft, 2.0 / 3.0), mdp));

		// TODO: inline jumpNextWrapped()?
		createHotKey(jumpButton, "jumpToNext", ignored -> jumpNextWrapped(),
			JComponent.WHEN_IN_FOCUSED_WINDOW, createAccelerator(KeyEvent.VK_J));

		expander = new TreeExpansionHandler(tree);

		menuHandler.register(ignored -> expander.expandAll(), "expand all");
		menuHandler.register(ignored -> expander.collapseAll(), "collapse all");
		menuHandler.register(ignored -> expandTwo(), "expand unit kinds");
		expander.expandAll();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(final WindowEvent event) {
				newUnitFrame.dispose();
			}
		});

		pcListeners = Collections.unmodifiableList(Arrays.asList(newUnitFrame, treeModel,
			ordersPanelObj, resultsPanel, notesPanelInstance));

		setJMenuBar(WorkerMenu.workerMenu(menuHandler::actionPerformed, getContentPane(), driver));
		pack();
	}

	private final OrdersPanel ordersPanelObj; // TODO: rename to ordersPanel;
	private final FormattedLabel playerLabel;

	private final IMapNG mainMap;
	private final NewUnitDialog newUnitFrame;

	private static boolean isCurrent(final IUnit unit, final int turn) {
		return unit.getOrders(turn).equals(unit.getLatestOrders(turn));
	}

	private void selectTodoText() {
		for (String string : Arrays.asList("fixme", "todo", "xxx")) {
			if (ordersPanelObj.selectText(string)) {
				break;
			}
		}
	}

	private void jumpNext() {
		IWorkerTreeModel treeModel = (IWorkerTreeModel) tree.getModel();
		TreePath currentSelection = tree.getSelectionModel().getSelectionPath();
		TreePath nextPath = treeModel.nextProblem(currentSelection, mainMap.getCurrentTurn());
		if (nextPath != null) {
			tree.expandPath(nextPath);
			tree.setSelectionRow(tree.getRowForPath(nextPath));
			// selectTodoText isn't inlined because we need to make sure the
			// tree-selection listeners get updated
			SwingUtilities.invokeLater(this::selectTodoText);
		} else {
			LOGGER.finer("Nowhere to jump to, about to beep");
			Toolkit.getDefaultToolkit().beep();
		}
	}

	private void jumpNextWrapped() {
		SwingUtilities.invokeLater(this::jumpNext);
	}

	private final StrategyExporter strategyExporter;

	private void writeStrategy(final Path file) {
		try {
			strategyExporter.writeStrategy(file, model.getDismissed());
		} catch (final IOException except) {
			// FIXME: Show error dialog
			LOGGER.log(Level.SEVERE, "I/O error while trying to write strategy", except);
		}
	}

	private void strategyWritingListener() {
		SPFileChooser.save(null, filteredFileChooser(false, ".", null)).call(this::writeStrategy);
	}

	private final TreeExpansionOrderListener expander;

	private void expandTwo() {
		expander.expandSome(2);
	}

	private final List<PlayerChangeListener> pcListeners;

	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		for (PlayerChangeListener listener : pcListeners) {
			listener.playerChanged(old, newPlayer);
		}
		playerLabel.setArguments(newPlayer.getName());
	}
}
