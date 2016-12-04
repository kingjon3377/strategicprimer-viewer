package view.worker;

import com.bric.window.WindowList;
import controller.map.drivers.SPOptions;
import controller.map.misc.FileChooser;
import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IOHandler;
import controller.map.misc.StrategyExporter;
import controller.map.report.ReportGenerator;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import model.listeners.MapChangeListener;
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
import util.ActionWrapper;
import util.NullCleaner;
import util.OnMac;
import util.TypesafeLogger;
import view.map.main.ViewerFrame;
import view.util.ISPWindow;
import view.util.ListenedButton;
import view.util.SystemOut;

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
public final class WorkerMgmtFrame extends JFrame implements ISPWindow {
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
	 * At this point (proof-of-concept) we default to the first player of the choices.
	 *
	 * @param options   options passed to the driver
	 * @param model     the driver model.
	 * @param ioHandler the I/O handler, so we can handle 'open' and 'save' menu items.
	 */
	public WorkerMgmtFrame(final SPOptions options, final IWorkerModel model,
						   final IOHandler ioHandler) {
		super("Worker Management");
		final Optional<Path> filename = model.getMapFile();
		if (filename.isPresent()) {
			setTitle(filename.get() + " | Worker Management");
			getRootPane().putClientProperty("Window.documentFile",
					filename.get().toFile());
		}
		final IMapNG mainMap = model.getMap();
		setMinimumSize(new Dimension(640, 480));
		final NewUnitDialog newUnitFrame =
				new NewUnitDialog(mainMap.getCurrentPlayer(),
										 IDFactoryFiller.createFactory(mainMap));
		final IWorkerTreeModel treeModel =
				new WorkerTreeModelAlt(mainMap.getCurrentPlayer(), model);
		final WorkerTree tree =
				WorkerTree.factory(treeModel, mainMap.players(),
						() -> mainMap.getCurrentTurn(), true);
		ioHandler.addPlayerChangeListener(treeModel);
		newUnitFrame.addNewUnitListener(treeModel);
		final boolean onMac = OnMac.SYSTEM_IS_MAC;
		final int keyMask;
		final String keyDesc;
		if (onMac) {
			keyMask = InputEvent.META_DOWN_MASK;
			keyDesc = ": (\u2318U)";
		} else {
			keyMask = InputEvent.CTRL_DOWN_MASK;
			keyDesc = ": (Ctrl+U)";
		}
		final InputMap inputMap = tree.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		final ActionMap actionMap = tree.getActionMap();
		assert (inputMap != null) && (actionMap != null);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, keyMask), "openUnits");
		actionMap.put("openUnits", new FocusRequester(tree));
		final PlayerLabel playerLabel =
				new PlayerLabel("Units belonging to ", mainMap.getCurrentPlayer(),
									   keyDesc);
		ioHandler.addPlayerChangeListener(playerLabel);
		ioHandler.addPlayerChangeListener(newUnitFrame);
		final OrdersPanel ordersPanel =
				new OrdersPanel(model.getMap().getCurrentTurn(),
									   model.getMap().getCurrentPlayer(),
									   model::getUnits,
									   (unit, turn) -> unit.getLatestOrders(turn),
									   (unit, turn, orders) -> unit.setOrders(turn,
											   orders));
		ioHandler.addPlayerChangeListener(ordersPanel);
		tree.addTreeSelectionListener(ordersPanel);
		final DefaultTreeModel reportModel =
				new DefaultTreeModel(new SimpleReportNode("Please wait, loading report" +
																  " ..."));
		new Thread(new ReportGeneratorThread(reportModel, model,
													mainMap.getCurrentPlayer())).start();
		final JTree report = new JTree(reportModel);
		report.setRootVisible(false);
		report.expandPath(
				new TreePath(((DefaultMutableTreeNode) reportModel.getRoot()).getPath
																					  ()));

		final DistanceComparator distCalculator = new DistanceComparator(findHQ(model));
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
									.setToolTipText(distCalculator.distanceString
																		   (point));
						} else {
							((JComponent) retval).setToolTipText(null);
						}
					}
					assert retval != null;
					return retval;
				});
		report.addMouseListener(new ReportMouseHandler(report, model, ioHandler));
		final ReportUpdater reportUpdater = new ReportUpdater(model, reportModel);
		ioHandler.addPlayerChangeListener(reportUpdater);
		model.addMapChangeListener(reportUpdater);
		final OrdersPanel resultsPanel =
				new OrdersPanel(mainMap.getCurrentTurn(), mainMap.getCurrentPlayer(),
									   model::getUnits,
									   (unit, turn) -> unit.getResults(turn), null);
		ioHandler.addPlayerChangeListener(resultsPanel);
		tree.addTreeSelectionListener(resultsPanel);
		final MemberDetailPanel mdp = new MemberDetailPanel(resultsPanel);
		tree.addUnitMemberListener(mdp);
		final StrategyExporter strategyExporter = new StrategyExporter(model);
		setContentPane(horizontalSplit(HALF_WAY, HALF_WAY,
				verticalSplit(TWO_THIRDS, TWO_THIRDS,
						verticalPanel(playerLabel, new JScrollPane(tree), null),
						verticalPanel(new ListenedButton("Add New Unit",
																evt -> newUnitFrame
																			   .setVisible(
																					   true)),
								ordersPanel,
								new ListenedButton("Export a proto-strategy",
														  evt -> new FileChooser
																		 (Optional
																				  .empty(),
																						new JFileChooser("."),
																						FileChooser.FileChooserOperation.Save)
																		 .call(file ->
																					   strategyExporter
																							   .writeStrategy(
																									   file,
																									   options,
																									   treeModel
																											   .dismissed()))))),
				verticalSplit(0.6, 0.6,
						verticalPanel(new JLabel(RPT_HDR), new JScrollPane(report),
								null),
						mdp)));
		ioHandler.addTreeExpansionListener(new TreeExpansionHandler(tree));
		setJMenuBar(new WorkerMenu(ioHandler, this, model));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		addWindowListener(new CloseListener(newUnitFrame));
		pack();
	}

	/**
	 * @param model a driver model
	 * @return the location of the current player's headquarters
	 */
	private static Point findHQ(final IWorkerModel model) {
		@NonNull
		Point retval = PointFactory.point(-1, -1);
		for (final Point location : model.getMap().locations()) {
			for (final TileFixture fix : model.getMap().getOtherFixtures(location)) {
				if ((fix instanceof Fortress) && ((Fortress) fix).getOwner()
														 .equals(model.getMap()
																		 .getCurrentPlayer())) {
					if ("HQ".equals(((Fortress) fix).getName())) {
						return location;
					} else if ((retval.getRow() < 0) && (location.getRow() >= 0)) {
						retval = location;
						break;
					}
				}
			}
		}
		return retval;
	}

	/**
	 * @param model the driver-model of the worker-management GUI
	 * @param ioh   the I/O handler
	 * @return the viewer model of a viewer window the same map as that in the given
	 * driver-model
	 */
	protected static IViewerModel getViewerModelFor(final IDriverModel model,
													final IOHandler ioh) {
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
		final ViewerFrame frame = new ViewerFrame(
														 new ViewerModel(model.getMap(),
																				model
																						.getMapFile()),
														 ioh);
		frame.setVisible(true);
		return frame.getModel();
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * @return the title of this app
	 */
	@Override
	public String getWindowName() {
		return "Worker Management";
	}

	/**
	 * A class to update the report when a new map is loaded.
	 *
	 * @author Jonathan Lovelace
	 */
	private static final class ReportUpdater implements PlayerChangeListener,
																MapChangeListener {
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
		}

		/**
		 * Handle notification that a new map was loaded.
		 */
		@Override
		public void mapChanged() {
			new Thread(new ReportGeneratorThread(reportModel, model, model.getMap()
																			 .getCurrentPlayer()))
					.start();
		}

		/**
		 * Handle change in current player.
		 *
		 * @param old       the previous current player
		 * @param newPlayer the new current player
		 */
		@Override
		public void playerChanged(@Nullable final Player old, final Player newPlayer) {
			new Thread(new ReportGeneratorThread(reportModel, model, newPlayer)).start();
		}

		/**
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
		 * @return a String representation of the thread
		 */
		@Override
		public String toString() {
			return "Generating report for " + player;
		}
	}

	/**
	 * An action to request focus in a component.
	 */
	@SuppressWarnings({"CloneableClassInSecureContext", "CloneableClassWithoutClone"})
	private static class FocusRequester extends ActionWrapper {
		/**
		 * The type of component we're handling.
		 */
		private final String type;

		/**
		 * Constructor.
		 *
		 * @param comp The component to request focus in.
		 */
		protected FocusRequester(final WorkerTree comp) {
			super(evt -> comp.requestFocusInWindow());
			type = NullCleaner.assertNotNull(comp.getClass().getSimpleName());
		}

		/**
		 * Prevent serialization.
		 *
		 * @param out ignored
		 * @throws IOException always
		 */
		@SuppressWarnings({"unused", "static-method"})
		private void writeObject(final ObjectOutputStream out) throws IOException {
			throw new NotSerializableException("Serialization is not allowed");
		}

		/**
		 * Prevent serialization
		 *
		 * @param in ignored
		 * @throws IOException            always
		 * @throws ClassNotFoundException never
		 */
		@SuppressWarnings({"unused", "static-method"})
		private void readObject(final ObjectInputStream in)
				throws IOException, ClassNotFoundException {
			throw new NotSerializableException("Serialization is not allowed");
		}

		/**
		 * @return a String representation of the action
		 */
		@Override
		public String toString() {
			return "Requesting focus in a " + type;
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
		private final IOHandler ioh;

		/**
		 * Constructor.
		 *
		 * @param reportTree  The report tree.
		 * @param workerModel the driver model
		 * @param ioHandler   the menu-item handler
		 */
		protected ReportMouseHandler(final JTree reportTree,
									 final IWorkerModel workerModel,
									 final IOHandler ioHandler) {
			report = reportTree;
			model = workerModel;
			ioh = ioHandler;
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
			if ((evt.isControlDown() || evt.isMetaDown()) &&
						(node instanceof IReportNode)) {
				final Point point = ((IReportNode) node).getPoint();
				// (-inf, -inf) replaces null
				if (point.getRow() > Integer.MIN_VALUE) {
					final IViewerModel vModel =
							getViewerModelFor(model, ioh);
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
		 * @param subWindow The window that we are to eventually dispose.
		 */
		protected CloseListener(final NewUnitDialog subWindow) {
			this.dialog = subWindow;
		}

		/**
		 * @param event the event to handle by disposing the dialog
		 */
		@Override
		public void windowClosed(final WindowEvent event) {
			dialog.dispose();
		}
	}
}
