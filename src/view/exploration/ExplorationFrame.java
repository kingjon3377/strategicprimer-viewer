package view.exploration;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;

import model.exploration.ExplorationModel;
import util.PropertyChangeSource;

/**
 * The main window for the exploration GUI.
 *
 * @author Jonathan Lovelace
 */
public class ExplorationFrame extends JFrame implements PropertyChangeSource {
	/**
	 * The exploration model.
	 */
	protected final ExplorationModel model;

	/**
	 * Constructor.
	 *
	 * @param emodel the exploration model
	 */
	public ExplorationFrame(final ExplorationModel emodel) {
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
		esp.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				if ("switch".equalsIgnoreCase(evt.getPropertyName())) {
					explorationPanel.validate();
					layout.next(outer);
				}
			}
		});
		explorationPanel.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				if ("switch".equalsIgnoreCase(evt.getPropertyName())) {
					esp.validate();
					layout.first(outer);
				}
			}
		});
		add(esp);
		add(explorationPanel);
		pack();
	}
}
