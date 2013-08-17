package view.worker;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;

import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.workermgmt.IWorkerModel;
import view.util.BorderedPanel;
import view.util.SplitWithWeights;
import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IOHandler;
import controller.map.report.ReportGenerator;
/**
 * A window to let the player manage units.
 * @author Jonathan Lovelace
 *
 */
public class WorkerMgmtFrame extends JFrame {
	/**
	 * At this point (proof-of-concept) we default to the first player of the choices.
	 * @param model the driver model.
	 * @param ioHandler the I/O handler, so we can handle 'open' and 'save' menu items.
	 */
	public WorkerMgmtFrame(final IWorkerModel model, final IOHandler ioHandler) {
		super("Strategic Primer worker management");
		setMinimumSize(new Dimension(640, 480));
		final NewUnitDialog newUnitFrame = new NewUnitDialog(model.getMap()
				.getPlayers().getCurrentPlayer(),
				IDFactoryFiller.createFactory(model.getMap()));
		final PlayerChooserHandler pch = new PlayerChooserHandler(this, model);
		final JTree tree = new WorkerTree(model.getMap().getPlayers()
				.getCurrentPlayer(), model, newUnitFrame, pch, model);
		final PlayerLabel plabel = new PlayerLabel("Units belonging to", model
				.getMap().getPlayers().getCurrentPlayer(), ":");
		pch.addPropertyChangeListener(plabel);
		final JButton newUnitButton = new JButton("Add New Unit");
		model.addPropertyChangeListener(newUnitFrame);
		newUnitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				newUnitFrame.setVisible(true);
			}
		});
		final OrdersPanel ordersPanel = new OrdersPanel();
		tree.addTreeSelectionListener(ordersPanel);
		final JButton exportButton = new JButton("Export a proto-strategy from units' orders");
		final Component outer = this;
		final IWorkerModel smodel = model;
		exportButton.addActionListener(new ExportButtonHandler(outer, smodel));
		final JEditorPane report = new JEditorPane("text/html", ReportGenerator
				.createAbbreviatedReport(model.getMap(), model.getMap().getPlayers().getCurrentPlayer()));
		pch.addPropertyChangeListener(new PropertyChangeListener() {
			// TODO: Make a named subclass
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				if ("player".equalsIgnoreCase(evt.getPropertyName())
						&& evt.getNewValue() instanceof Player) {
					report.setText(ReportGenerator.createAbbreviatedReport(
							model.getMap(), (Player) evt.getNewValue()));
				} else if ("map".equalsIgnoreCase(evt.getPropertyName())) {
					report.setText(ReportGenerator.createAbbreviatedReport(
							model.getMap(), model.getMap().getPlayers()
									.getCurrentPlayer()));
				}
			}
		});
		setContentPane(new SplitWithWeights(
				JSplitPane.HORIZONTAL_SPLIT,
				.5,
				.5,
				new SplitWithWeights(JSplitPane.VERTICAL_SPLIT, 0.7, 0.7,
						new BorderedPanel().setNorth(plabel).setCenter(
								new JScrollPane(tree)), new BorderedPanel()
								.setNorth(newUnitButton).setCenter(ordersPanel)
								.setSouth(exportButton)),
				new BorderedPanel()
						.setNorth(
								new JLabel(
										"A report on everything except your units and fortresses, for reference:"))
						.setCenter(new JScrollPane(report))));

		setJMenuBar(new WorkerMenu(ioHandler, this, pch));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
	}
	/**
	 * Handle the strategy-export button.
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
		private static final Logger LOGGER = Logger.getLogger(WorkerMgmtFrame.class.getName());
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
		 */
		ExportButtonHandler(final Component outer, final IWorkerModel smodel) {
			parent = outer;
			exp = new StrategyExporter(smodel);
		}
		/**
		 * Handle button press.
		 * @param evt the event to handle.
		 */
		@Override
		public void actionPerformed(final ActionEvent evt) {
			if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
				try (final FileWriter writer = new FileWriter(chooser
						.getSelectedFile())) {
					writer.append(exp.createStrategy());
				} catch (IOException except) {
					LOGGER.log(Level.SEVERE, "I/O error exporting strategy", except);
				}
			}
		}
	}
	/**
	 * A class to export a "proto-strategy" to file.
	 */
	public static class StrategyExporter {
		/**
		 * Constructor.
		 * @param wmodel the driver model to draw from
		 */
		public StrategyExporter(final IWorkerModel wmodel) {
			model = wmodel;
		}
		/**
		 * The worker model.
		 */
		private final IWorkerModel model;
		/**
		 * @return the proto-strategy as a String
		 */
		public String createStrategy() {
			final StringBuilder builder = new StringBuilder();
			builder.append('[');
			builder.append(model.getMap().getPlayers().getCurrentPlayer().getName());
			builder.append("\nTurn ");
			builder.append(model.getMap().getCurrentTurn());
			builder.append("]\n\nInventions: TODO: any?\n\n");
			final Map<String, List<Unit>> unitsByKind = new HashMap<>();
			for (final Unit unit : model.getUnits(model.getMap().getPlayers().getCurrentPlayer())) {
				// ESCA-JAVA0177:
				final List<Unit> list; // NOPMD
				if (unitsByKind.containsKey(unit.getKind())) {
					list = unitsByKind.get(unit.getKind());
				} else {
					list = new ArrayList<>(); // NOPMD
					unitsByKind.put(unit.getKind(), list);
				}
				list.add(unit);
			}
			builder.append("Workers:\n");
			for (final Entry<String, List<Unit>> entry : unitsByKind.entrySet()) {
				builder.append("* ");
				builder.append(entry.getKey());
				builder.append(":\n");
				for (Unit unit : entry.getValue()) {
					builder.append("  - ");
					builder.append(unit.getName());
					builder.append(unitMembers(unit));
					builder.append(":\n\n");
					final String orders = unit.getOrders().trim();
					builder.append(orders.isEmpty() ? "TODO" : orders);
					builder.append("\n\n");
				}
			}
			return builder.toString();
		}
		/**
		 * @param unit a unit
		 * @return a String representing its members
		 */
		private static String unitMembers(final Unit unit) {
			if (unit.iterator().hasNext()) {
				final StringBuilder builder = new StringBuilder(" [");
				final Iterator<UnitMember> iter = unit.iterator();
				while (iter.hasNext()) {
					final UnitMember member = iter.next();
					builder.append(memberString(member));
					if (iter.hasNext()) {
						builder.append(", ");
					}
				}
				builder.append(']');
				return builder.toString(); // NOPMD
			} else {
				return "";
			}
		}
		/**
		 * @param member a unit member
		 * @return a suitable string for it
		 */
		private static String memberString(final UnitMember member) {
			if (member instanceof Worker) {
				final Worker worker = (Worker) member;
				final StringBuilder builder = new StringBuilder(worker.getName());
				if (worker.iterator().hasNext()) {
					builder.append(" (");
					final Iterator<Job> iter = worker.iterator();
					while (iter.hasNext()) {
						final Job job = iter.next();
						builder.append(job.getName());
						builder.append(' ');
						builder.append(job.getLevel());
						if (iter.hasNext()) {
							builder.append(", ");
						}
					}
					builder.append(')');
				}
				return builder.toString(); // NOPMD
			} else {
				return member.toString();
			}
		}
	}
}
