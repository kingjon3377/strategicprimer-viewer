package view.exploration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;

import model.exploration.ExplorationModel;
import model.exploration.ExplorationUnitListModel;
import model.exploration.PlayerListModel;
import model.map.Player;
import model.map.fixtures.mobile.Unit;
import util.PropertyChangeSource;
import view.util.BorderedPanel;
/**
 * The panel that lets the user select the unit to explore with.
 * @author Jonathan Lovelace
 *
 */
public class ExplorerSelectingPanel extends BorderedPanel implements
		ListSelectionListener, PropertyChangeSource, ActionListener {
	/**
	 * The proportion between the two sides.
	 */
	private static final double PROPORTION = 0.5;
	/**
	 * The text on the 'start exploring' button.
	 */
	private static final String BUTTON_TEXT = "Start exploring!";
	/**
	 * The exploration  model.
	 */
	private final ExplorationModel model;
	/**
	 * Constructor.
	 * @param emodel the driver model
	 */
	public ExplorerSelectingPanel(final ExplorationModel emodel) {
		model = emodel;
		final BorderedPanel uspFirst = new BorderedPanel();
		uspFirst.setNorth(new JLabel("Players in all maps:"));
		playerList = new JList<>(new PlayerListModel(emodel));
		playerList.addListSelectionListener(this);
		uspFirst.setCenter(playerList);
		final BorderedPanel uspSecond = new BorderedPanel();
		uspSecond
				.setNorth(new JLabel(
						"<html><body><p>Units belonging to that player:</p>"
								+ "<p>(Selected unit will be used for exploration.)</p></body></html>"));
		unitList = new JList<>(new ExplorationUnitListModel(emodel, this));
		uspSecond.setCenter(unitList);
		final BorderedPanel mpPanel = new BorderedPanel();
		mpPanel.setWest(new JLabel("Unit's Movement Points: "));
		mpPanel.setEast(mpField);
		final JButton explButton = new JButton(BUTTON_TEXT);
		explButton.addActionListener(this);
		mpPanel.setSouth(explButton);
		uspSecond.setSouth(mpPanel);
		final JSplitPane unitSelPanel = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, uspFirst, uspSecond);
		unitSelPanel.setDividerLocation(PROPORTION);
		unitSelPanel.setResizeWeight(PROPORTION);
		setCenter(unitSelPanel);
	}
	/**
	 * The text-field containing the running MP total.
	 */
	private final JTextField mpField = new JTextField(5);
	/**
	 * @return the model underlying the field containing the running MP total.
	 */
	public Document getMPDocument() {
		return mpField.getDocument();
	}
	/**
	 * The list of players.
	 */
	private final JList<Player> playerList;
	/**
	 * Handle the user selecting a different player.
	 *
	 * @param evt event
	 */
	@Override
	public void valueChanged(final ListSelectionEvent evt) {
		firePropertyChange("player", null, playerList.getSelectedValue());
	}
	/**
	 * Handle a press of the 'start exploring' button.
	 * @param event the event to handle
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if (BUTTON_TEXT.equalsIgnoreCase(event.getActionCommand())
				&& !unitList.isSelectionEmpty()) {
			model.selectUnit(unitList.getSelectedValue());
			firePropertyChange("switch", null, Boolean.TRUE);
		}
	}
	/**
	 * The list of units.
	 */
	private final JList<Unit> unitList;
}
