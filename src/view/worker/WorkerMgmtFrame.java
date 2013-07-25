package view.worker;

import java.awt.BorderLayout;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;

import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.workermgmt.IWorkerModel;
import model.workermgmt.IWorkerTreeModel;
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
		final JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		left.setDividerLocation(0.7);
		left.setResizeWeight(0.7);
		final JTree tree = new WorkerTree(model.getMap().getPlayers()
				.getCurrentPlayer(), model);
		left.setTopComponent(new JScrollPane(tree));
		final JPanel bottom = new JPanel(new BorderLayout());
		final JButton newUnitButton = new JButton("Add New Unit");
		final NewUnitDialog newUnitFrame = new NewUnitDialog(model.getMap()
				.getPlayers().getCurrentPlayer(),
				IDFactoryFiller.createFactory(model.getMap()));
		model.addPropertyChangeListener(newUnitFrame);
		newUnitFrame.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				if ("unit".equals(evt.getPropertyName())) {
					((IWorkerTreeModel) tree.getModel()).addUnit((Unit) evt.getNewValue());
				}
			}
		});
		newUnitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				newUnitFrame.setVisible(true);
			}
		});
		bottom.add(newUnitButton, BorderLayout.NORTH);
		final OrdersPanel ordersPanel = new OrdersPanel();
		tree.addTreeSelectionListener(ordersPanel);
		bottom.add(ordersPanel, BorderLayout.CENTER);
		final JButton exportButton = new JButton("Export a proto-strategy from units' orders");
		final Component outer = this;
		final IWorkerModel smodel = model;
		final Logger logger = Logger.getLogger(WorkerMgmtFrame.class.getName());
		exportButton.addActionListener(new ActionListener() {
			private final JFileChooser chooser = new JFileChooser(".");
			private final StrategyExporter exp = new StrategyExporter(smodel);
			@Override
			public void actionPerformed(final ActionEvent evt) {
				if (chooser.showSaveDialog(outer) == JFileChooser.APPROVE_OPTION) {
					// ESCA-JAVA0177:
					final FileWriter writer; // NOPMD
					try {
						writer = new FileWriter(chooser
								.getSelectedFile());
					} catch (IOException except) {
						logger.log(Level.SEVERE, "I/O error opening file for strategy", except);
						return;
					}
					try {
						writer.append(exp.createStrategy());
					} catch (IOException except) {
						logger.log(Level.SEVERE, "I/O error exporting strategy", except);
					} finally {
						try {
							writer.close();
						} catch (IOException except) {
							logger.log(Level.SEVERE, "I/O error closing file", except);
						}
					}
				}
			}
		});
		bottom.add(exportButton, BorderLayout.SOUTH);
		left.setBottomComponent(bottom);
		final JPanel right = new JPanel(new BorderLayout());
		right.add(
				new JLabel(
						"A report on everything except your units and fortresses, for reference:"),
				BorderLayout.NORTH);
		right.add(new JScrollPane(new JEditorPane("text/html", new ReportGenerator()
				.createAbbreviatedReport(model.getMap()))), BorderLayout.CENTER);
		final JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
		main.setDividerLocation(.5);
		main.setResizeWeight(.5);
		setContentPane(main);

		setJMenuBar(new WorkerMenu(ioHandler, this));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
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
			final Map<String, List<Unit>> unitsByKind = new HashMap<String, List<Unit>>();
			for (final Unit unit : model.getUnits(model.getMap().getPlayers().getCurrentPlayer())) {
				// ESCA-JAVA0177:
				final List<Unit> list; // NOPMD
				if (unitsByKind.containsKey(unit.getKind())) {
					list = unitsByKind.get(unit.getKind());
				} else {
					list = new ArrayList<Unit>(); // NOPMD
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
