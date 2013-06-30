package view.worker;

import java.awt.Dimension;

import javax.swing.JFrame;

import model.workermgmt.IWorkerModel;
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
		setMinimumSize(new Dimension(640, 480));
		add(new WorkerTree(model.getMap().getPlayers().getCurrentPlayer(), model));
		pack();
	}
}
