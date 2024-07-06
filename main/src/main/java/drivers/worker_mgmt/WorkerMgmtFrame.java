package drivers.worker_mgmt;

import java.io.Serial;
import java.nio.file.Paths;
import java.io.IOException;

import drivers.gui.common.SPFileChooser;

import javax.swing.JButton;
import java.util.Arrays;
import java.util.List;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;

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
import legacy.idreg.IDFactoryFiller;
import legacy.idreg.IDRegistrar;
import legacy.map.Player;
import legacy.map.ILegacyMap;
import legacy.map.fixtures.mobile.IUnit;
import drivers.map_viewer.NewUnitDialog;
import legacy.xmlio.MapIOHelper;

import drivers.gui.common.SPFrame;
import drivers.gui.common.MenuBroker;

import static drivers.gui.common.SPFileChooser.filteredFileChooser;

import drivers.worker_mgmt.orderspanel.OrdersPanel;

/**
 * A window to let the player manage units.
 */
/* package */ final class WorkerMgmtFrame extends SPFrame implements PlayerChangeListener {
	@Serial
	private static final long serialVersionUID = 1L;
	private final IWorkerModel model;
	private final WorkerTree tree;

	public WorkerMgmtFrame(final SPOptions options, final IWorkerModel model, final MenuBroker menuHandler,
						   final WorkerMgmtGUI driver) {
		super("Worker Management", driver, new Dimension(640, 480), true,
				(file) -> model.addSubordinateMap(MapIOHelper.readMap(file)));
		this.model = model;
		mainMap = model.getMap();
		final IDRegistrar idf = IDFactoryFiller.createIDFactory(model.streamAllMaps()
				.toArray(ILegacyMap[]::new));
		newUnitFrame = new NewUnitDialog(model.getCurrentPlayer(), idf);
		final IWorkerTreeModel treeModel = new WorkerTreeModelAlt(model); // TODO: Try with WorkerTreeModel again?

		tree = new WorkerTree(treeModel, model.getPlayers(),
				mainMap::getCurrentTurn, true, idf);
		newUnitFrame.addNewUnitListener(treeModel);

		final int keyMask = Platform.SHORTCUT_MASK;
		createHotKey(tree, "openUnits", ignored -> tree.requestFocusInWindow(),
				JComponent.WHEN_IN_FOCUSED_WINDOW,
				KeyStroke.getKeyStroke(KeyEvent.VK_U, keyMask));

		playerLabel = new FormattedLabel("Units belonging to %%s: (%sU)".formatted(Platform.SHORTCUT_DESCRIPTION),
				model.getCurrentPlayer().getName());
		ordersPanelObj = new OrdersPanel("Orders", mainMap.getCurrentTurn(),
				model.getCurrentPlayer(), model::getUnits, IUnit::getLatestOrders,
				model::setUnitOrders, WorkerMgmtFrame::isCurrent); // TODO: inline isCurrent?
		tree.addTreeSelectionListener(ordersPanelObj);

		final OrdersPanel.IIsCurrent trueSupplier = (unit, turn) -> true;

		final OrdersPanel.IOrdersConsumer resultsSupplier;
		if ("true".equals(options.getArgument("--edit-results"))) {
			resultsSupplier = model::setUnitResults;
		} else {
			resultsSupplier = null;
		}
		final OrdersPanel resultsPanel = new OrdersPanel("Results", mainMap.getCurrentTurn(),
				model.getCurrentPlayer(), model::getUnits, IUnit::getResults,
				resultsSupplier, trueSupplier);
		tree.addTreeSelectionListener(resultsPanel);

		final NotesPanel notesPanelInstance = new NotesPanel(model.getMap().getCurrentPlayer());
		tree.addUnitMemberListener(notesPanelInstance);

		final MemberDetailPanel mdp = new MemberDetailPanel(resultsPanel, notesPanelInstance);
		tree.addUnitMemberListener(mdp);

		final JButton jumpButton = new ListenedButton("Jump to Next Blank (%sJ)"
				.formatted(Platform.SHORTCUT_DESCRIPTION), ignored -> SwingUtilities.invokeLater(this::jumpNext));

		strategyExporter = new StrategyExporter(model, options);

		final BorderedPanel lowerLeft = BorderedPanel.verticalPanel(
				new ListenedButton("Add New Unit", newUnitFrame::showWindow),
				ordersPanelObj,
				new ListenedButton("Export a proto-strategy", this::strategyWritingListener));
		setContentPane(horizontalSplit(verticalSplit(
				BorderedPanel.verticalPanel(
						BorderedPanel.horizontalPanel(playerLabel, null, jumpButton),
						new JScrollPane(tree), null),
				lowerLeft, 2.0 / 3.0), mdp));

		createHotKey(jumpButton, "jumpToNext", ignored -> SwingUtilities.invokeLater(this::jumpNext),
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

		pcListeners = List.of(newUnitFrame, treeModel, ordersPanelObj, resultsPanel, notesPanelInstance);

		setJMenuBar(WorkerMenu.workerMenu(menuHandler, getContentPane(), driver));
		pack();
	}

	private final OrdersPanel ordersPanelObj; // TODO: rename to ordersPanel;
	private final FormattedLabel playerLabel;

	private final ILegacyMap mainMap;
	private final NewUnitDialog newUnitFrame;

	private static boolean isCurrent(final IUnit unit, final int turn) {
		return unit.getOrders(turn).equals(unit.getLatestOrders(turn));
	}

	private void selectTodoText() {
		for (final String string : Arrays.asList("fixme", "todo", "xxx")) {
			if (ordersPanelObj.selectText(string)) {
				break;
			}
		}
	}

	private void jumpNext() {
		final IWorkerTreeModel treeModel = (IWorkerTreeModel) tree.getModel();
		final TreePath currentSelection = tree.getSelectionModel().getSelectionPath();
		final TreePath nextPath = treeModel.nextProblem(currentSelection, mainMap.getCurrentTurn());
		if (Objects.isNull(nextPath)) {
			LovelaceLogger.trace("Nowhere to jump to, about to beep");
			Toolkit.getDefaultToolkit().beep();
		} else {
			tree.expandPath(nextPath);
			tree.setSelectionRow(tree.getRowForPath(nextPath));
			// selectTodoText isn't inlined because we need to make sure the
			// tree-selection listeners get updated
			SwingUtilities.invokeLater(this::selectTodoText);
		}
	}

	private final StrategyExporter strategyExporter;

	private void writeStrategy(final Path file) {
		try {
			strategyExporter.writeStrategy(file, model.getDismissed());
		} catch (final IOException except) {
			// FIXME: Show error dialog
			LovelaceLogger.error(except, "I/O error while trying to write strategy");
		}
	}

	private void strategyWritingListener() {
		SPFileChooser.save(null, filteredFileChooser(false, Paths.get("."), null)).call(this::writeStrategy);
	}

	private final TreeExpansionOrderListener expander;

	private void expandTwo() {
		expander.expandSome(2);
	}

	private final List<PlayerChangeListener> pcListeners;

	@Override
	public void playerChanged(final @Nullable Player old, final Player newPlayer) {
		for (final PlayerChangeListener listener : pcListeners) {
			listener.playerChanged(old, newPlayer);
		}
		playerLabel.setArguments(newPlayer.getName());
	}
}
