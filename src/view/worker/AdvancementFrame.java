package view.worker;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.text.View;

import model.map.HasName;
import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import model.misc.IDriverModel;
import util.PropertyChangeSource;
import view.util.AddRemovePanel;
import view.util.SystemOut;
/**
 * A GUI to let a user manage workers.
 * @author Jonathan Lovelace
 *
 */
public class AdvancementFrame extends JFrame implements ItemListener,
		PropertyChangeListener, PropertyChangeSource {
	/**
	 * The map model containing the data we're working from.
	 */
	private final IDriverModel model;
	/**
	 * A drop-down list listing the players in the map.
	 */
	private final JComboBox<Player> players = new JComboBox<Player>();
	/**
	 * Constructor.
	 * @param source the model containing the data to work from
	 */
	public AdvancementFrame(final IDriverModel source) {
		super("Strategic Primer worker advancement");
		model = source;
		model.addPropertyChangeListener(this);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));

		final JPanel panelOne = new JPanel();
		panelOne.setLayout(new BoxLayout(panelOne, BoxLayout.PAGE_AXIS));
		players.addItemListener(this);
		final JLabel playerLabel = new JLabel(htmlize("Current Player:"));
		panelOne.add(playerLabel);
		panelOne.add(players);
		final JLabel unitLabel = new JLabel(htmlize("Player's Units:"));
		panelOne.add(unitLabel);
		final JList<Unit> units = new UnitList(source, this, source, this);
		panelOne.add(units);
		add(panelOne);

		final JPanel panelTwo = new JPanel();
		panelTwo.setLayout(new BoxLayout(panelTwo, BoxLayout.PAGE_AXIS));
		final JLabel memberLabel = new JLabel(htmlize("Selected Unit's Members:"));
		panelTwo.add(memberLabel);
		final JList<UnitMember> members = new UnitMemberList(this, this);
		panelTwo.add(members);
		final StatsLabel statsLabel = new StatsLabel(this);
		panelTwo.add(statsLabel);
		add(panelTwo);

		final JPanel panelThree = new JPanel();
		panelThree.setLayout(new BoxLayout(panelThree, BoxLayout.PAGE_AXIS));
		final AddRemovePanel jarp = new AddRemovePanel(false);
		final JList<Job> jobs = new JobsList(this, this, jarp);
		final JLabel jobsLabel = new JLabel(htmlize("Worker's Jobs:"));
		panelThree.add(jobsLabel);
		panelThree.add(jobs);
		panelThree.add(jarp);
		final AddRemovePanel sarp = new AddRemovePanel(false);
		final JList<Skill> skills = new SkillList(this, this, sarp);
		final JLabel skillsLabel = new JLabel(htmlize("Skills in selected Job:"));
		panelThree.add(skillsLabel);
		panelThree.add(skills);
		panelThree.add(sarp);
		panelThree.add(new SkillAdvancementPanel(this, this));
		add(panelThree);

		addPropertyChangeListener(this);
		firePropertyChange("map", null, null);
		removePropertyChangeListener(this);

		setMinimumSize(new Dimension(640, 480));
		final List<JComponent> lists = new ArrayList<JComponent>();
		lists.add(panelOne);
		lists.add(panelTwo);
		lists.add(panelThree);
		lists.add(players);
		lists.add(jobs);
		lists.add(skills);
		lists.add(units);
		lists.add(members);
		lists.add(playerLabel);
		lists.add(unitLabel);
		lists.add(memberLabel);
		lists.add(jobsLabel);
		lists.add(skillsLabel);
		lists.add(statsLabel);
		getContentPane().addComponentListener(new Resizer(lists));
		addPropertyChangeListener(new LevelListener());
	}
	/**
	 * Handle a property change.
	 * @param evt the property-change event to handle
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("map".equals(evt.getPropertyName())) {
			players.removeAllItems();
			for (Player player : model.getMap().getPlayers()) {
				players.addItem(player);
			}
		} else if (!equals(evt.getSource())) {
			firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
		}
	}
	/**
	 * @param evt an event indicating an item's changed in one of the combo-boxes we listen to
	 */
	@Override
	public void itemStateChanged(final ItemEvent evt) {
		if (players.equals(evt.getSource())) {
			firePropertyChange("player", null, players.getSelectedItem());
		}
	}
	/**
	 * Turn a string into left-aligned HTML.
	 * @param string a string
	 * @return it wrapped in HTML code that should make it left-aligned.
	 */
	private static String htmlize(final String string) {
		return "<html><p align=\"left\">" + string + "</p></html>";
	}
	/**
	 * Get a label's size given a fixed width.
	 * Adapted from http://blog.nobel-joergensen.com/2009/01/18/changing-preferred-size-of-a-html-jlabel/
	 * @param component the component we're laying out
	 * @param width the width we're working within
	 * @return the "ideal" dimensions for the component
	 */
	public static Dimension getComponentPreferredSize(
			final JComponent component, final int width) {
	final View view = (View) component
				.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
		view.setSize(width, 0);
		final int wid = (int) Math.ceil(view.getPreferredSpan(View.X_AXIS));
		final int height = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
		return new Dimension(wid, height);
	}
	/**
	 * A listener to print a line when a worker gains a level.
	 */
	private static final class LevelListener implements PropertyChangeListener {
		/**
		 * Constructor.
		 */
		LevelListener() {
			// Needed to give access ...
		}
		/**
		 * The current worker.
		 */
		private UnitMember worker = null;
		/**
		 * The current skill.
		 */
		private Skill skill = null;
		/**
		 * @param evt the property-change event to handle
		 */
		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			if ("member".equals(evt.getPropertyName())
					&& (evt.getNewValue() instanceof UnitMember || evt
							.getNewValue() == null)) {
				worker = (UnitMember) evt.getNewValue();
			} else if ("skill".equals(evt.getPropertyName())
					&& (evt.getNewValue() instanceof Skill || evt
							.getNewValue() == null)) {
			skill = (Skill) evt.getNewValue();
			} else if ("level".equals(evt.getPropertyName())) {
				final StringBuilder builder = new StringBuilder();
				builder.append(getName(worker));
				builder.append(" gained a level in ");
				builder.append(getName(skill));
				SystemOut.SYS_OUT.println(builder.toString());
			}
		}
		/**
		 * @param named something that may have a name
		 * @return its name if it has one, "null" if null, or its toString otherwise.
		 */
		private static String getName(final Object named) {
			if (named instanceof HasName) {
				return ((HasName) named).getName(); // NOPMD
			} else if (named == null) {
				return "null"; // NOPMD
			} else {
				return named.toString();
			}
		}
	}
	/**
	 * A class to resize components when the frame is resized.
	 */
	private final class Resizer extends ComponentAdapter {
		/**
		 * Constructor.
		 * @param list the list of components to resize each time we get the event.
		 */
		Resizer(final List<JComponent> list) {
			// ESCA-JAVA0256:
			components = list;
		}
		/**
		 * The list of components to reize each time we get an event.
		 */
		private final List<JComponent> components;
		/**
		 * Adjust the size of the sub-panels when this is resized.
		 * @param evt the event being handled
		 */
		@Override
		public void componentResized(final ComponentEvent evt) {
			final int width = getWidth() / 3;
			final int minHeight = 20; // NOPMD
			final int maxHeight = getHeight();
			for (JComponent list : components) {
				if (list instanceof JComboBox) {
					final Dimension dim = dimension(width, minHeight);
					list.setMaximumSize(dim);
					list.setPreferredSize(dim);
				} else if (list instanceof JList) {
					list.setMaximumSize(dimension(width, maxHeight));
					list.setMinimumSize(dimension(width, minHeight));
				} else if (list instanceof JLabel) {
					final Dimension dim = getComponentPreferredSize(list, width);
					list.setMinimumSize(dim);
					list.setPreferredSize(dim);
					list.setMaximumSize(dim);
				} else if (list instanceof JPanel) {
					final Dimension dim = dimension(width, maxHeight);
					list.setMaximumSize(dim);
					list.setPreferredSize(dim);
					list.setMinimumSize(dim);
				}
			}
		}
		/**
		 * @param width a width
		 * @param height a height
		 * @return a Dimension with those values.
		 */
		private Dimension dimension(final int width, final int height) {
			return new Dimension(width, height);
		}
	}
}
