package view.worker;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.eclipse.jdt.annotation.Nullable;

import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IOHandler;
import controller.map.report.ReportGenerator;
import model.listeners.MapChangeListener;
import model.listeners.PlayerChangeListener;
import model.map.HasName;
import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.Job;
import model.workermgmt.IWorkerModel;
import model.workermgmt.IWorkerTreeModel;
import model.workermgmt.WorkerTreeModelAlt;
import util.NullCleaner;
import util.TypesafeLogger;
import view.util.BorderedPanel;
import view.util.ListenedButton;
import view.util.SplitWithWeights;

/**
 * A window to let the player manage units.
 *
 * @author Jonathan Lovelace
 *
 */
public class WorkerMgmtFrame extends JFrame {
	/**
	 * The header to put above the report.
	 */
	private static final String RPT_HDR = NullCleaner
			.assertNotNull(new StringBuilder("A report on everything")
					.append(" except your units and fortresses,")
					.append("for reference:").toString());
	/**
	 * A constant for when a split panel should be divided evenly in half.
	 */
	private static final double HALF_WAY = .5;
	/**
	 * A constant for when a split panel should be divided not quite evenly.
	 */
	private static final double TWO_THIRDS = 2.0 / 3.0;

	/**
	 * At this point (proof-of-concept) we default to the first player of the
	 * choices.
	 *
	 * @param model the driver model.
	 * @param ioHandler the I/O handler, so we can handle 'open' and 'save' menu
	 *        items.
	 */
	public WorkerMgmtFrame(final IWorkerModel model, final IOHandler ioHandler) {
		super("Worker Management");
		if (model.getMapFile().exists()) {
			setTitle(model.getMapFile().getName() + " | Worker Management");
			getRootPane().putClientProperty("Window.documentFile",
					model.getMapFile());
		}
		setMinimumSize(new Dimension(640, 480));
		final NewUnitDialog newUnitFrame =
				new NewUnitDialog(model.getMap().getCurrentPlayer(),
						IDFactoryFiller.createFactory(model.getMap()));
		final PlayerChooserHandler pch = new PlayerChooserHandler(this, model);
		final IWorkerTreeModel wtmodel =
				new WorkerTreeModelAlt(model.getMap().getCurrentPlayer(), model);
		final WorkerTree tree =
				new WorkerTree(wtmodel, model.getMap().players(), true);
		pch.addPlayerChangeListener(wtmodel);
		newUnitFrame.addNewUnitListener(wtmodel);
		final PlayerLabel plabel = new PlayerLabel("Units belonging to", model
				.getMap().getCurrentPlayer(), ":");
		pch.addPlayerChangeListener(plabel);
		pch.addPlayerChangeListener(newUnitFrame);
		final OrdersPanel ordersPanel = new OrdersPanel(model);
		pch.addPlayerChangeListener(ordersPanel);
		ordersPanel.playerChanged(null, model.getMap().getCurrentPlayer());
		tree.addTreeSelectionListener(ordersPanel);
		final Component outer = this;
		final IWorkerModel smodel = model;
		final DefaultTreeModel reportModel = new DefaultTreeModel(
				ReportGenerator.createAbbreviatedReportIR(model.getMap(), model
						.getMap().getCurrentPlayer()));
		final JTree report = new JTree(reportModel);
		report.setRootVisible(false);
		report.expandPath(new TreePath(((DefaultMutableTreeNode) reportModel
				.getRoot()).getPath()));
		final ReportUpdater reportUpdater = new ReportUpdater(model,
				reportModel);
		pch.addPlayerChangeListener(reportUpdater);
		model.addMapChangeListener(reportUpdater);
		final MemberDetailPanel mdp = new MemberDetailPanel();
		tree.addUnitMemberListener(mdp);
		setContentPane(new SplitWithWeights(JSplitPane.HORIZONTAL_SPLIT,
				HALF_WAY, HALF_WAY, new SplitWithWeights(
						JSplitPane.VERTICAL_SPLIT, TWO_THIRDS, TWO_THIRDS,
						new BorderedPanel(new JScrollPane(tree), plabel, null,
								null, null),
						new BorderedPanel(
								ordersPanel,
								new ListenedButton("Add New Unit",
										new WindowShower(newUnitFrame,
												"Add New Unit")),
								new ListenedButton(
										"Export a proto-strategy from units' orders",
										new ExportButtonHandler(outer, smodel,
												wtmodel)), null, null)),
				new BorderedPanel(new JScrollPane(report), new JLabel(RPT_HDR),
						mdp, null, null)));

		setJMenuBar(new WorkerMenu(ioHandler, this, pch, model));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		pack();
	}

	/**
	 * A listener to show a window on button press.
	 *
	 * @author Jonathan Lovelace
	 *
	 */
	private static final class WindowShower implements ActionListener {
		/**
		 * The window to show.
		 */
		private final JFrame frame;
		/**
		 * The button to listen for.
		 */
		private final String action;

		/**
		 * @param window the window to show
		 * @param buttonText the button to listen for.
		 */
		protected WindowShower(final JFrame window, final String buttonText) {
			frame = window;
			action = buttonText;
		}

		/**
		 * Handle button press.
		 *
		 * @param evt the event to handle
		 */
		@Override
		public void actionPerformed(@Nullable final ActionEvent evt) {
			if (evt != null && action.equals(evt.getActionCommand())) {
				frame.setVisible(true);
			}
		}
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "WindowShower";
		}
	}

	/**
	 * A class to update the report when a new map is loaded.
	 *
	 * @author Jonathan Lovelace
	 *
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
		 * @param wmodel The driver model to get the map from
		 * @param tmodel the tree model we update
		 */
		protected ReportUpdater(final IWorkerModel wmodel,
				final DefaultTreeModel tmodel) {
			model = wmodel;
			reportModel = tmodel;
		}

		/**
		 * Handle notification that a new map was loaded.
		 */
		@Override
		public void mapChanged() {
			reportModel.setRoot(ReportGenerator.createAbbreviatedReportIR(
					model.getMap(), model.getMap().getCurrentPlayer()));
		}

		/**
		 * Handle change in current player.
		 *
		 * @param old the previous current player
		 * @param newPlayer the new current player
		 */
		@Override
		public void playerChanged(@Nullable final Player old,
				final Player newPlayer) {
			reportModel.setRoot(ReportGenerator.createAbbreviatedReportIR(
					model.getMap(), newPlayer));
		}
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "ReportUpdater";
		}
	}

	/**
	 * Handle the strategy-export button.
	 *
	 * @author Jonathan Lovelace
	 *
	 */
	private static final class ExportButtonHandler implements ActionListener {
		/**
		 * The surrounding frame.
		 */
		private final Component parent;
		/**
		 * The logger. FIXME: Should be private static final on the outer class.
		 */
		private static final Logger LOGGER = TypesafeLogger
				.getLogger(WorkerMgmtFrame.class);
		/**
		 * The file chooser.
		 */
		private final JFileChooser chooser = new JFileChooser(".");
		/**
		 * The strategy-exporter.
		 */
		private final StrategyExporter exp;

		/**
		 * @param outer the surrounding frame.
		 * @param smodel the driver model.
		 * @param wtmodel the worker-tree model, to get dismissed unit-members from
		 */
		protected ExportButtonHandler(final Component outer,
				final IWorkerModel smodel, final IWorkerTreeModel wtmodel) {
			parent = outer;
			exp = new StrategyExporter(smodel, wtmodel);
		}

		/**
		 * Handle button press.
		 *
		 * @param evt the event to handle.
		 */
		@Override
		public void actionPerformed(@Nullable final ActionEvent evt) {
			if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
				try (final FileWriter writer = new FileWriter(
						chooser.getSelectedFile())) {
					writer.append(exp.createStrategy());
				} catch (final IOException except) {
					LOGGER.log(Level.SEVERE, "I/O error exporting strategy",
							except);
				}
			}
		}
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "ExportButtonHandler";
		}
	}

	/**
	 * A class to export a "proto-strategy" to file.
	 * @author Jonathan Lovelace
	 */
	public static class StrategyExporter {
		/**
		 * The worker model.
		 */
		private final IWorkerModel model;
		/**
		 * Unit members that have been dismissed.
		 */
		private final IWorkerTreeModel tmodel;
		/**
		 * Constructor.
		 *
		 * @param wmodel the driver model to draw from
		 * @param wtmodel the tree model to get dismissed unit members from
		 */
		public StrategyExporter(final IWorkerModel wmodel,
				final IWorkerTreeModel wtmodel) {
			model = wmodel;
			tmodel = wtmodel;
		}

		/**
		 * @return the proto-strategy as a String
		 */
		public String createStrategy() {
			final Player currentPlayer = model.getMap().getCurrentPlayer();
			final String playerName = currentPlayer.getName();
			final String turn = Integer.toString(model.getMap().getCurrentTurn());
			final List<IUnit> units = model.getUnits(currentPlayer);

			final Map<String, List<IUnit>> unitsByKind = new HashMap<>();
			for (final IUnit unit : units) {
				if (!unit.iterator().hasNext()) {
					// FIXME: This should be exposed as a user option. Sometimes
					// users *want* empty units printed.
					continue;
				}
				// ESCA-JAVA0177:
				final List<IUnit> list; // NOPMD
				if (unitsByKind.containsKey(unit.getKind())) {
					list = unitsByKind.get(unit.getKind());
				} else {
					list = new ArrayList<>(); // NOPMD
					unitsByKind.put(unit.getKind(), list);
				}
				list.add(unit);
			}

			int size = 58 + playerName.length() + turn.length();
			for (final Entry<String, List<IUnit>> entry : unitsByKind.entrySet()) {
				size += 4;
				size += entry.getKey().length();
				for (final IUnit unit : entry.getValue()) {
					size += 10;
					size += unit.getName().length();
					size += unitMemberSize(unit);
					size += unit.getOrders().length();
				}
			}
			final Iterable<UnitMember> dismissed = tmodel.dismissed();
			for (final UnitMember member : dismissed) {
				size += 2;
				if (member instanceof HasName) {
					size += ((HasName) member).getName().length();
				} else {
					size += member.toString().length();
				}
			}
			final StringBuilder builder = new StringBuilder(size);
			builder.append('[');
			builder.append(playerName);
			builder.append("\nTurn ");
			builder.append(turn);
			builder.append("]\n\nInventions: TODO: any?\n\n");
			if (dismissed.iterator().hasNext()) {
				builder.append("Dismissed workers etc.: ");
				String separator = "";
				for (final UnitMember member : dismissed) {
					builder.append(separator);
					separator = ", ";
					if (member instanceof HasName) {
						builder.append(((HasName) member).getName());
					} else {
						builder.append(member.toString());
					}
				}
				builder.append("\n\n");
			}
			builder.append("Workers:\n");
			for (final Entry<String, List<IUnit>> entry : unitsByKind.entrySet()) {
				builder.append("* ");
				builder.append(entry.getKey());
				builder.append(":\n");
				for (final IUnit unit : entry.getValue()) {
					builder.append("  - ");
					builder.append(unit.getName());
					builder.append(unitMembers(unit));
					builder.append(":\n\n");
					final String orders = unit.getOrders().trim();
					if (orders.isEmpty()) {
						builder.append("TODO");
					} else {
						builder.append(orders);
					}
					builder.append("\n\n");
				}
			}
			return NullCleaner.assertNotNull(builder.toString());
		}
		/**
		 * @param unit a unit
		 * @return the size of string needed to represent its members
		 */
		private static int unitMemberSize(final IUnit unit) {
			if (unit.iterator().hasNext()) {
				int size = 3;
				for (final UnitMember member : unit) {
					size += 2;
					size += memberStringSize(NullCleaner.assertNotNull(member));
				}
				return size;
			} else {
				return 0;
			}
		}
		/**
		 * @param unit a unit
		 * @return a String representing its members
		 */
		private static String unitMembers(final IUnit unit) {
			if (unit.iterator().hasNext()) {
				// Assume at least two K.
				final StringBuilder builder = new StringBuilder(2048)
						.append(" [");
				boolean first = true;
				for (final UnitMember member : unit) {
					if (member == null) {
						continue;
					}
					if (first) {
						first = false;
					} else {
						builder.append(", ");
					}
					builder.append(memberString(member));
				}
				builder.append(']');
				return NullCleaner.assertNotNull(builder.toString()); // NOPMD
			} else {
				return "";
			}
		}
		/**
		 * @param member a unit member
		 * @return the size of a string for it
		 */
		private static int memberStringSize(final UnitMember member) {
			if (member instanceof Worker) {
				int size = ((Worker) member).getName().length();
				size += 2;
				for (final IJob job : (IWorker) member) {
					size += 3;
					size += job.getName().length();
					size += Integer.toString(job.getLevel()).length();
				}
				return size;
			} else {
				return member.toString().length();
			}
		}
		/**
		 * @param member a unit member
		 * @return a suitable string for it
		 */
		private static String memberString(final UnitMember member) {
			if (member instanceof Worker) {
				final Worker worker = (Worker) member;
				// To save calculations, assume a half-K every time.
				final StringBuilder builder = new StringBuilder(512)
						.append(worker.getName());
				if (worker.iterator().hasNext()) {
					builder.append(" (");
					boolean first = true;
					for (final IJob job : worker) {
						if (!(job instanceof Job)) {
							continue;
						}
						if (first) {
							first = false;
						} else {
							builder.append(", ");
						}
						builder.append(job.getName());
						builder.append(' ');
						builder.append(job.getLevel());
					}
					builder.append(')');
				}
				return NullCleaner.assertNotNull(builder.toString()); // NOPMD
			} else {
				return NullCleaner.assertNotNull(member.toString());
			}
		}
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "StrategyExporter";
		}
	}
}
