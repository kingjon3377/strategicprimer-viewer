package view.exploration;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import model.exploration.ExplorationModel;
import model.listeners.CompletionListener;
import controller.map.misc.MultiIOHandler;

/**
 * The main window for the exploration GUI.
 *
 * @author Jonathan Lovelace
 */
public class ExplorationFrame extends JFrame {
	/**
	 * The exploration model.
	 */
	protected final ExplorationModel model;

	/**
	 * Constructor.
	 *
	 * @param emodel the exploration model
	 * @param ioHandler the I/O handler to let the menu handle the 'save all',
	 *        etc.
	 */
	public ExplorationFrame(final ExplorationModel emodel,
			final MultiIOHandler ioHandler) {
		super("Strategic Primer Exploration");
		model = emodel;
		setMinimumSize(new Dimension(768, 480));
		setPreferredSize(new Dimension(1024, 640));
		final Container outer = getContentPane();
		final CardLayout layout = new CardLayout();
		setLayout(layout);
		final ExplorerSelectingPanel esp = new ExplorerSelectingPanel(emodel);
		final ExplorationPanel explorationPanel = new ExplorationPanel(emodel,
				esp.getMPDocument());
		esp.addCompletionListener(new CompletionListener() {
			@Override
			public void stopWaitingOn(final Object result) {
				explorationPanel.validate();
				layout.next(outer);
			}
		});
		explorationPanel.addCompletionListener(new CompletionListener() {
			@Override
			public void stopWaitingOn(final Object result) {
				esp.validate();
				layout.first(outer);
			}
		});
		add(esp);
		add(explorationPanel);

		setJMenuBar(new ExplorationMenu(ioHandler, emodel, this));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
	}
}
