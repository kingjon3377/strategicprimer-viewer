package view.worker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;

import model.map.fixtures.mobile.Unit;
import model.workermgmt.IWorkerModel;
import model.workermgmt.IWorkerTreeModel;
import controller.map.misc.IDFactoryFiller;
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
		final JSplitPane splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		splitpane.setDividerLocation(0.7);
		splitpane.setResizeWeight(0.7);
		final JTree tree = new WorkerTree(model.getMap().getPlayers()
				.getCurrentPlayer(), model);
		splitpane.setTopComponent(new JScrollPane(tree));
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
		splitpane.setBottomComponent(bottom);
		setContentPane(splitpane);
		pack();
	}
}
