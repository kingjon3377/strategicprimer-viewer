package view.worker;

import com.bric.window.WindowList;
import controller.map.drivers.SPOptions;
import controller.map.misc.FileChooser;
import controller.map.misc.FileChooser.FileChooserOperation;
import controller.map.misc.IDFactoryFiller;
import controller.map.misc.MenuBroker;
import controller.map.misc.StrategyExporter;
import controller.map.report.ReportGenerator;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import model.listeners.PlayerChangeListener;
import model.map.DistanceComparator;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import model.map.fixtures.towns.Fortress;
import model.misc.IDriverModel;
import model.report.IReportNode;
import model.report.SimpleReportNode;
import model.viewer.IViewerModel;
import model.viewer.ViewerModel;
import model.workermgmt.IWorkerModel;
import model.workermgmt.IWorkerTreeModel;
import model.workermgmt.WorkerTreeModelAlt;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.OnMac;
import util.TypesafeLogger;
import view.map.main.ViewerFrame;
import view.util.BorderedPanel;
import view.util.FocusRequester;
import view.util.FormattedLabel;
import view.util.HotKeyCreator;
import view.util.ListenedButton;
import view.util.SPFrame;
import view.util.SystemOut;
import view.util.TreeExpansionOrderListener;

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static view.util.BorderedPanel.verticalPanel;
import static view.util.SplitWithWeights.horizontalSplit;
import static view.util.SplitWithWeights.verticalSplit;

/**
 * A window to let the player manage units.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class WorkerMgmtFrame extends SPFrame
		implements PlayerChangeListener, HotKeyCreator {
	/**
	 * The header to put above the report.
	 */
	private static final String RPT_HDR =
			"The contents of the world you know about, for reference:";
	/**
	 * A constant for when a split panel should be divided evenly in half.
	 */
	private static final double HALF_WAY = 0.5;
	/**
	 * A constant for when a split panel should be divided not quite evenly.
	 */
	private static final double TWO_THIRDS = 2.0 / 3.0;
	/**
	 * The label saying to whom the units belong.
	 */
	private final FormattedLabel playerLabel;
	/**
	 * A list of things to notify when the current player changes.
	 */
	private final List<PlayerChangeListener> pcListeners;

	/**
	 * At this point (proof-of-concept) we default to the first player of the choices.
	 *
	 * @param options   options passed to the driver
	 * @param model     the driver model.
	 * @param menuHandler the menu-item handler.
	 */
	public WorkerMgmtFrame(final SPOptions options, final IWorkerModel model,
						   final MenuBroker menuHandler) {
		super("Worker Management", model.getMapFile(), new Dimension(640, 480));
		final IMapNG mainMap = model.getMap();
		final NewUnitDialog newUnitFrame =
				new NewUnitDialog(mainMap.getCurrentPlayer(),
										 IDFactoryFiller.createFactory(mainMap));
		final IWorkerTreeModel treeModel =
				new WorkerTreeModelAlt(mainMap.getCurrentPlayer(), model);
		final WorkerTree tree =
				WorkerTree.factory(treeModel, mainMap.players(),
						mainMap::getCurrentTurn, true);
		newUnitFrame.addNewUnitListener(treeModel);
		final int keyMask = OnMac.SHORTCUT_MASK;
		createHotKey(tree, "openUnits", new FocusRequester(tree), WHEN_IN_FOCUSED_WINDOW,
				KeyStroke.getKeyStroke(KeyEvent.VK_U, keyMask));
		playerLabel = new FormattedLabel("Units belonging to %s: (%sU)",
												mainMap.getCurrentPlayer().getName(),
									   OnMac.SHORTCUT_DESC);
		final OrdersPanel ordersPanel = new OrdersPanel(model.getMap().getCurrentTurn(),
							   mainMap.getCurrentPlayer(), model::getUnits,
							   (unit, turn) -> unit.getLatestOrders(turn),
							   (unit, turn, orders) -> unit.setOrders(turn,
									   orders));
		tree.addTreeSelectionListener(ordersPanel);
		final DefaultTreeModel reportModel =
				new DefaultTreeModel(new SimpleReportNode("Please wait, loading report" +
																  " ..."));
		createReportGeneratorThread(reportModel, model, mainMap.getCurrentPlayer())
				.start();
		final OrdersPanel resultsPanel =
				new OrdersPanel(mainMap.getCurrentTurn(), mainMap.getCurrentPlayer(),
							   model::getUnits,
							   (unit, turn) -> unit.getResults(turn), null);
		tree.addTreeSelectionListener(resultsPanel);
		final MemberDetailPanel mdp = new MemberDetailPanel(resultsPanel);
		tree.addUnitMemberListener(mdp);
		final StrategyExporter strategyExporter = new StrategyExporter(model);
		final ActionListener exporterLambda =
				evt -> new FileChooser(Optional.empty(),
											  new JFileChooser("."),
											  FileChooserOperation.Save)
							   .call(file -> strategyExporter.writeStrategy(file, options,
									   treeModel.dismissed()));
		final BorderedPanel lowerLeft = verticalPanel(
				new ListenedButton("Add New Unit", evt -> newUnitFrame.setVisible(true)),
				ordersPanel,
				new ListenedButton("Export a proto-strategy", exporterLambda));
		//noinspection SuspiciousNameCombination
		setContentPane(horizontalSplit(HALF_WAY, HALF_WAY,
				verticalSplit(TWO_THIRDS, TWO_THIRDS,
						verticalPanel(playerLabel, new JScrollPane(tree), null),
						lowerLeft),
				verticalSplit(0.6, 0.6, verticalPanel(new JLabel(RPT_HDR),
						new JScrollPane(createReportTree(model, menuHandler,
								reportModel)), null), mdp)));
		final TreeExpansionOrderListener expander = new TreeExpansionHandler(tree);
		menuHandler.register(evt -> expander.expandAll(), "expand all");
		menuHandler.register(evt -> expander.collapseAll(), "collapse all");
		menuHandler.register(evt -> expander.expandSome(2), "expand unit kinds");
		setJMenuBar(new WorkerMenu(menuHandler, this, model));
		expander.expandAll();
		addWindowListener(new CloseListener(newUnitFrame));
		pcListeners = new ArrayList<>(Arrays.asList(newUnitFrame, treeModel, ordersPanel,
				new ReportUpdater(model, reportModel), resultsPanel));
		pack();
	}
	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "WorkerMgmtFrame";
	}

	/**
	 * Create the "report" tree.
	 * @param model the driver model
	 * @param menuHandler the menu-item/hot-key broker
	 * @param reportModel the report-tree model
	 * @return the report tree based on that model
	 */
	private static JTree createReportTree(final IWorkerModel model,
										  final MenuBroker menuHandler,
										  final DefaultTreeModel reportModel) {
		final JTree report = new JTree(reportModel);
		report.setRootVisible(false);
		report.expandPath(new TreePath(
				((DefaultMutableTreeNode) reportModel.getRoot()).getPath()));

		final DistanceComparator calculator = new DistanceComparator(findHQ(model));
		final TreeCellRenderer defRender = new DefaultTreeCellRenderer();
		report.setCellRenderer(
				(renderedTree, value, selected, expanded, leaf, row, hasFocus) -> {
					final Component retval = defRender.getTreeCellRendererComponent(
							renderedTree, value, selected, expanded, leaf, row,
							hasFocus);
					if (value instanceof IReportNode) {
						final Point point = ((IReportNode) value).getPoint();
						// (-inf, -inf) replaces null
						if (point.getRow() > Integer.MIN_VALUE) {
							((JComponent) retval)
									.setToolTipText(calculator.distanceString(point));
						} else {
							((JComponent) retval).setToolTipText(null);
						}
					}
					assert retval != null;
					return retval;
				});
		report.addMouseListener(new ReportMouseHandler(report, model, menuHandler));
		return report;
	}

	/**
	 * Find the location of the current player's headquarters fortress.
	 * @param model a driver model
	 * @return the location of the current player's headquarters
	 */
	private static Point findHQ(final IDriverModel model) {
		@NonNull
		Point retval = PointFactory.INVALID_POINT;
		for (final Point location : model.getMap().locations()) {
			for (final TileFixture fix : model.getMap().getOtherFixtures(location)) {
				if ((fix instanceof Fortress) &&
							Objects.equals(((Fortress) fix).getOwner(),
									model.getMap().getCurrentPlayer())) {
					if ("HQ".equals(((Fortress) fix).getName())) {
						return location;
					} else if (!retval.isValid() && location.isValid()) {
						retval = location;
						break;
					}
				}
			}
		}
		return retval;
	}

	/**
	 * Get a map-GUI driver model viewing the same map as that of the given model.
	 * @param model the driver-model of the worker-management GUI
	 * @param menuHandler   the menu-item broker.
	 * @return the viewer model of a viewer window the same map as that in the given
	 * driver-model
	 */
	protected static IViewerModel getViewerModelFor(final IDriverModel model,
													final MenuBroker menuHandler) {
		for (final Frame frame : WindowList.getFrames(false, true, true)) {
			if ((frame instanceof ViewerFrame) &&
						((ViewerFrame) frame).getModel().getMapFile()
								.equals(model.getMapFile())) {
				frame.toFront();
				if (frame.getExtendedState() == Frame.ICONIFIED) {
					frame.setExtendedState(Frame.NORMAL);
				}
				return ((ViewerFrame) frame).getModel();
			}
		}
		final ViewerFrame frame =
				new ViewerFrame(new ViewerModel(model.getMap(), model.getMapFile()),
									   menuHandler);
		frame.setVisible(true);
		return frame.getModel();
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings("unused")
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization.
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings("unused")
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * The title of this app.
	 * @return the title of this app
	 */
	@Override
	public String getWindowName() {
		return "Worker Management";
	}

	/**
	 * Called when the current player changes.
	 *
	 * @param old       the previous current player
	 * @param newPlayer the new current player
	 */
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		for (final PlayerChangeListener listener : pcListeners) {
			listener.playerChanged(old, newPlayer);
		}
		playerLabel.setArgs(newPlayer.getName(), OnMac.SHORTCUT_DESC);
	}
	/**
	 * Create the report-generation thread.
	 * @param treeModel     The tree-model to put the report into.
	 * @param workerModel   the driver model to generate the report from
	 * @param currentPlayer the player to generate the report for
	 * @return the thread, not yet started
	 */
	protected static Thread createReportGeneratorThread(final DefaultTreeModel treeModel,
														final IWorkerModel workerModel,
														final Player currentPlayer) {
		return new Thread(new ReportGeneratorThread(treeModel, workerModel,
														   currentPlayer));
	}
	/**
	 * A class to update the report when a new map is loaded.
	 *
	 * @author Jonathan Lovelace
	 */
	private static final class ReportUpdater implements PlayerChangeListener {
		/**
		 * The driver model, to get the map from.
		 */
		private final IWorkerModel model;
		/**
		 * The pane that we update.
		 */
		private final DefaultTreeModel reportModel;

		/**
		 * Constructor.
		 *
		 * @param workerModel The driver model to get the map from
		 * @param treeModel   the tree model we update
		 */
		protected ReportUpdater(final IWorkerModel workerModel,
								final DefaultTreeModel treeModel) {
			model = workerModel;
			reportModel = treeModel;
			model.addMapChangeListener(
					() -> createReportGeneratorThread(
							treeModel, model, model.getMap().getCurrentPlayer())
								  .start());
		}

		/**
		 * Handle change in current player.
		 *
		 * @param old       the previous current player
		 * @param newPlayer the new current player
		 */
		@Override
		public void playerChanged(@Nullable final Player old, final Player newPlayer) {
			createReportGeneratorThread(reportModel, model, newPlayer).start();
		}

		/**
		 * A trivial toString().
		 * @return a String representation of the object
		 */
		@SuppressWarnings("MethodReturnAlwaysConstant")
		@Override
		public String toString() {
			return "ReportUpdater";
		}
	}

	/**
	 * A thread to generate the report tree in the background.
	 */
	private static final class ReportGeneratorThread implements Runnable {
		/**
		 * A logger for the thread.
		 */
		private static final Logger RGT_LOGGER =
				TypesafeLogger.getLogger(ReportGeneratorThread.class);
		/**
		 * The tree-model to put the report into.
		 */
		protected final DefaultTreeModel reportModel;
		/**
		 * The worker model to generate the report from.
		 */
		private final IWorkerModel driverModel;
		/**
		 * The player to generate the report for.
		 */
		private final Player player;

		/**
		 * Constructor.
		 *
		 * @param treeModel     The tree-model to put the report into.
		 * @param workerModel   the driver model to generate the report from
		 * @param currentPlayer the player to generate the report for
		 */
		protected ReportGeneratorThread(final DefaultTreeModel treeModel,
										final IWorkerModel workerModel,
										final Player currentPlayer) {
			reportModel = treeModel;
			driverModel = workerModel;
			player = currentPlayer;
		}

		/**
		 * Run the thread.
		 */
		@Override
		public void run() {
			RGT_LOGGER.info("About to generate report");
			final IReportNode report =
					ReportGenerator
							.createAbbreviatedReportIR(driverModel.getMap(), player);
			RGT_LOGGER.info("Finished generating report");
			SwingUtilities.invokeLater(() -> reportModel.setRoot(report));
		}

		/**
		 * A simple toString().
		 * @return a String representation of the thread
		 */
		@Override
		public String toString() {
			return "Generating report for " + player;
		}
	}

	/**
	 * Handler for mouse clicks in the report tree.
	 */
	@SuppressWarnings("ClassHasNoToStringMethod")
	private static class ReportMouseHandler extends MouseAdapter {
		/**
		 * The report tree.
		 */
		private final JTree report;
		/**
		 * The driver model.
		 */
		private final IWorkerModel model;
		/**
		 * The menu-item handler.
		 */
		private final MenuBroker menuBroker;

		/**
		 * Constructor.
		 *
		 * @param reportTree  The report tree.
		 * @param workerModel the driver model
		 * @param menuHandler   the menu-item handler
		 */
		protected ReportMouseHandler(final JTree reportTree,
									 final IWorkerModel workerModel,
									 final MenuBroker menuHandler) {
			report = reportTree;
			model = workerModel;
			menuBroker = menuHandler;
			ToolTipManager.sharedInstance().registerComponent(report);
		}

		/**
		 * Handle a mouse press.
		 *
		 * @param evt the event to handle
		 */
		@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
		@Override
		public void mousePressed(final @Nullable MouseEvent evt) {
			if (evt == null) {
				SystemOut.SYS_OUT.println("MouseEvent was null");
				return;
			}
			final TreePath selPath = report.getPathForLocation(evt.getX(), evt.getY());
			if (selPath == null) {
				return;
			}
			final Object node = selPath.getLastPathComponent();
			if (OnMac.isHotkeyPressed(evt) && (node instanceof IReportNode)) {
				final Point point = ((IReportNode) node).getPoint();
				// (-inf, -inf) replaces null
				if (point.getRow() > Integer.MIN_VALUE) {
					final IViewerModel vModel =
							getViewerModelFor(model, menuBroker);
					SwingUtilities.invokeLater(() -> vModel.setSelection(point));
				}
			}
		}
	}

	/**
	 * A listener to make sure the NewUnitDialog gets disposed when we do.
	 */
	private static class CloseListener extends WindowAdapter {
		/**
		 * The dialog to keep track of.
		 */
		private final Window dialog;

		/**
		 * Constructor.
		 * @param subWindow The window that we are to eventually dispose.
		 */
		protected CloseListener(final NewUnitDialog subWindow) {
			dialog = subWindow;
		}

		/**
		 * Dispose the dialog when the parent window is closed.
		 * @param event the event to handle by disposing the dialog
		 */
		@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
		@Override
		public void windowClosed(final WindowEvent event) {
			dialog.dispose();
		}
		/**
		 * A trivial toString().
		 * @return a String representation of the object.
		 */
		@Override
		public String toString() {
			return "CloseListener";
		}
	}
}
