package view.worker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;

import model.map.fixtures.mobile.Unit;
import model.workermgmt.IWorkerModel;
import model.workermgmt.IWorkerTreeModel;
import controller.map.misc.IDFactoryFiller;
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
	 */
	public WorkerMgmtFrame(final IWorkerModel model) {
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
		pack();
	}
}
