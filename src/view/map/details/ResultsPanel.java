package view.map.details;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import model.exploration.ExplorationRunner;
import model.viewer.Tile;
/**
 * A panel for displaying and editing tile text i.e. results.
 * @author Jonathan Lovelace
 */
public class ResultsPanel extends JPanel {
	/**
	 * Exploration runner to produce exploration results.
	 */
	private final ExplorationRunner runner = new ExplorationRunner();
	/**
	 * Minimum width of this panel.
	 */
	private static final int MINIMUM_WIDTH = 200;
	/**
	 * Preferred width of this panel.
	 */
	private static final int PREF_WIDTH = 250;
	/**
	 * Maximum width of this panel.
	 */
	private static final int MAX_WIDTH = 300;
	/**
	 * Minimum height of the label.
	 */
	private static final int LABEL_MIN_HT = 10;
	/**
	 * Preferred height of the label.
	 */
	private static final int LABEL_HEIGHT = 15;
	/**
	 * Maximum height of the label.
	 */
	private static final int LABEL_MAX_HT = 20;
	/**
	 * Minimum height of the button.
	 */
	private static final int BUTTON_MIN_HT = 15;
	/**
	 * Preferred height of the button.
	 */
	private static final int BUTTON_HEIGHT = 20;
	/**
	 * Maximum height of the button.
	 */
	private static final int BUTTON_MAX_HT = 25;
	/**
	 * Field to show and edit exploration results.
	 */
	private final JTextArea field = new JTextArea();
	/**
	 * Command for saving changed results to the map.
	 */
	private static final String SAVE_COMMAND = "<html><p>Save changed results</p></html>";
	/**
	 * Constructor.
	 * @param minHeight the minimum height of this panel
	 * @param height the (preferred) height of this panel
	 * @param maxHeight the maximum height of this panel
	 */
	public ResultsPanel(final int minHeight, final int height, final int maxHeight) {
		super(new BorderLayout());
		final JLabel label = new JLabel("Exploration results:");
		label.setAlignmentY(SwingConstants.CENTER);
		label.setMinimumSize(new Dimension(MINIMUM_WIDTH, LABEL_MIN_HT));
		label.setPreferredSize(new Dimension(PREF_WIDTH, LABEL_HEIGHT));
		label.setMaximumSize(new Dimension(MAX_WIDTH, LABEL_MAX_HT));
		add(label, BorderLayout.NORTH);
		
		field.setLineWrap(true);
		field.setEditable(true);
		field.setWrapStyleWord(true);
		final JScrollPane wrapper = new JScrollPane(field);
		wrapper.setMinimumSize(new Dimension(MINIMUM_WIDTH, minHeight - LABEL_MIN_HT - BUTTON_MIN_HT));
		wrapper.setPreferredSize(new Dimension(PREF_WIDTH, height - LABEL_HEIGHT - BUTTON_HEIGHT));
		wrapper.setMaximumSize(new Dimension(MAX_WIDTH, maxHeight - LABEL_MAX_HT - BUTTON_MAX_HT));
		add(wrapper, BorderLayout.CENTER);
		
		final JButton button = new JButton(SAVE_COMMAND);
		button.addActionListener(new ActionListener() {
			/**
			 * Handle button presses.
			 */
			@Override
			public void actionPerformed(final ActionEvent event) {
				saveTileText();
			}
		});
		button.setMinimumSize(new Dimension(MINIMUM_WIDTH, BUTTON_MIN_HT));
		button.setPreferredSize(new Dimension(PREF_WIDTH, BUTTON_HEIGHT));
		button.setMaximumSize(new Dimension(MAX_WIDTH, BUTTON_MAX_HT));
		add(button, BorderLayout.SOUTH);
		runner.loadAllTables("tables");
	}
	/**
	 * Save changed results back to the tile.
	 */
	public void saveTileText() {
		tile.setTileText(field.getText().trim());
	}
	/**
	 * @param newTile the new tile
	 */
	public void setTile(final Tile newTile) {
		if (!newTile.equals(tile)) {
		tile = newTile;
		field.setText(tile.getTileText());
		}
	}
	/**
	 * The tile we get and save results from and to. 
	 */
	private Tile tile;
	/**
	 * Run an encounter.
	 */
	public void runEncounter() {
		field.setText(field.getText() + '\n'
				+ runner.recursiveConsultTable("main", tile));
		saveTileText();
	}
}
