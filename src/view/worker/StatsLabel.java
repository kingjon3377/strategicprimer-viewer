package view.worker;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;

import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.WorkerStats;
import util.PropertyChangeSource;

/**
 * A label to show a worker's stats. At present it just shows placeholder text.
 * TODO: implement properly when model support for stats is added.
 *
 * @author Jonathan Lovelace
 *
 */
public class StatsLabel extends JLabel implements PropertyChangeListener {
	/**
	 * Constructor.
	 * @param sources things to listen to
	 */
	public StatsLabel(final PropertyChangeSource... sources) {
		for (PropertyChangeSource source : sources) {
			source.addPropertyChangeListener(this);
		}
	}
	/**
	 * A HTML newline.
	 */
	private static final String NEWLINE = "<br />";
	/**
	 * Handle a property change.
	 * @param evt the event to handle.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("member".equals(evt.getPropertyName())) {
			if (evt.getNewValue() instanceof Worker && ((Worker) evt.getNewValue()).getStats() != null) {
				final WorkerStats stats = ((Worker) evt.getNewValue()).getStats();
				final StringBuilder builder = new StringBuilder("<html>HP: ");
				builder.append(stats.getHitPoints());
				builder.append(" / ");
				builder.append(stats.getMaxHitPoints());
				builder.append(NEWLINE);
				builder.append("Str: ");
				builder.append(stats.getStrength());
				builder.append(NEWLINE);
				builder.append("Dex: ");
				builder.append(stats.getDexterity());
				builder.append(NEWLINE);
				builder.append("Con: ");
				builder.append(stats.getConstitution());
				builder.append(NEWLINE);
				builder.append("Int: ");
				builder.append(stats.getIntelligence());
				builder.append(NEWLINE);
				builder.append("Wis: ");
				builder.append(stats.getWisdom());
				builder.append(NEWLINE);
				builder.append("Cha: ");
				builder.append(stats.getCharisma());
				builder.append(NEWLINE);
				setText(builder.toString());
			} else {
				setText("Worker stats will appear here.");
			}
		}
	}
}
