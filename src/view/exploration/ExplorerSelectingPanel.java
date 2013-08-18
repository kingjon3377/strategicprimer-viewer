package view.exploration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import view.util.ListenedButton;
import view.util.SplitWithWeights;
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
		playerList = new JList<>(new PlayerListModel(emodel));
		playerList.addListSelectionListener(this);
		unitList = new JList<>(new ExplorationUnitListModel(emodel, this));
		setCenter(new SplitWithWeights(JSplitPane.HORIZONTAL_SPLIT, PROPORTION,
				PROPORTION, new BorderedPanel(playerList,
						label("Players in all maps:"), null, null, null),
				new BorderedPanel(unitList, label(html(
						"Units belonging to that player:",
						"(Selected unit will be used for exploration.)")),
						new BorderedPanel(null, null, new ListenedButton(
								BUTTON_TEXT, this), mpField,
								label("Unit's Movement Points: ")), null, null)));
	}
	/**
	 * @param string a String
	 * @return a JLabel with that string on it
	 */
	private static JLabel label(final String string) {
		return new JLabel(string);
	}
	/**
	 * @param paras Strings, each of which should be put in its own paragraph.
	 * @return them wrapped in HTML.
	 */
	private static String html(final String... paras) {
		final StringBuilder builder = new StringBuilder("<html><body>");
		for (final String para : paras) {
			builder.append("<p>").append(para).append("</p>");
		}
		return builder.append("</body></html>").toString();
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
