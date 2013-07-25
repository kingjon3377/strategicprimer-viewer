package view.worker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import javax.swing.text.View;

import model.map.HasName;
import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.Skill;
import model.misc.IDriverModel;
import model.workermgmt.IWorkerModel;
import util.PropertyChangeSource;
import view.util.AddRemovePanel;
import view.util.SystemOut;
import controller.map.misc.IOHandler;

/**
 * A GUI to let a user manage workers.
 *
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
	 *
	 * @param source the model containing the data to work from
	 * @param ioHandler the I/O handler so the menu 'open' item, etc., will work
	 */
	public AdvancementFrame(final IWorkerModel source, final IOHandler ioHandler) {
		super("Strategic Primer worker advancement");
		model = source;
		model.addPropertyChangeListener(this);
		setMinimumSize(new Dimension(640, 480));


		players.addItemListener(this);
		final JPanel playerPanel = new JPanel(new BorderLayout());
		playerPanel.add(new JLabel(htmlize("Current Player:")), BorderLayout.NORTH);
		playerPanel.add(players, BorderLayout.CENTER);

		final JPanel unitPanel = new JPanel(new BorderLayout());
		unitPanel.add(new JLabel(htmlize("Player's Units:")), BorderLayout.NORTH);
		unitPanel.add(new UnitList(source, this, source, this), BorderLayout.CENTER);

		final JSplitPane panelOne = new JSplitPane(JSplitPane.VERTICAL_SPLIT, playerPanel, unitPanel);

		final JPanel panelTwo = new JPanel(new BorderLayout());
		panelTwo.add(new JLabel(htmlize("Selected Unit's Members:")),
				BorderLayout.NORTH);
		panelTwo.add(new UnitMemberList(this, this), BorderLayout.CENTER);

		final JSplitPane jspOne = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelTwo, new StatsLabel(this));
		jspOne.setContinuousLayout(true);
		jspOne.setResizeWeight(.6);
		jspOne.setDividerLocation(.6);

		final JSplitPane jspTwo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelOne, jspOne);
		jspTwo.setContinuousLayout(true);
		jspTwo.setResizeWeight(.5);
		jspTwo.setDividerLocation(.5);

		final JPanel jobsPanel = new JPanel(new BorderLayout());
		final AddRemovePanel jarp = new AddRemovePanel(false);
		jobsPanel.add(new JLabel(htmlize("Worker's Jobs:")), BorderLayout.NORTH);
		jobsPanel.add(new JobsList(this, this, jarp), BorderLayout.CENTER);
		jobsPanel.add(jarp, BorderLayout.SOUTH);

		final JPanel skillPanel = new JPanel(new BorderLayout());
		final AddRemovePanel sarp = new AddRemovePanel(false);
		skillPanel.add(new JLabel(htmlize("Skills in selected Job:")),
				BorderLayout.NORTH);
		skillPanel.add(new SkillList(this, this, sarp), BorderLayout.CENTER);
		skillPanel.add(sarp, BorderLayout.SOUTH);

		final JPanel skillSuperPanel = new JPanel(new BorderLayout());
		skillSuperPanel.add(skillPanel, BorderLayout.CENTER);
		skillSuperPanel.add(new SkillAdvancementPanel(this, this), BorderLayout.SOUTH);

		final JSplitPane panelThree = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jobsPanel, skillSuperPanel);
		panelThree.setContinuousLayout(true);
		panelThree.setResizeWeight(.3);
		panelThree.setDividerLocation(.5);

		final JSplitPane jspThree = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jspTwo, panelThree);
		jspThree.setContinuousLayout(true);
		jspThree.setResizeWeight(2.0 / 3.0);
		jspThree.setDividerLocation(2.0 / 3.0);
		setContentPane(jspThree);

		addPropertyChangeListener(this);
		firePropertyChange("map", null, null);
		removePropertyChangeListener(this);

		addPropertyChangeListener(new LevelListener());

		setJMenuBar(new WorkerMenu(ioHandler, this));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
	}

	/**
	 * Handle a property change.
	 *
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
			firePropertyChange(evt.getPropertyName(), evt.getOldValue(),
					evt.getNewValue());
		}
	}

	/**
	 * @param evt an event indicating an item's changed in one of the
	 *        combo-boxes we listen to
	 */
	@Override
	public void itemStateChanged(final ItemEvent evt) {
		if (players.equals(evt.getSource())) {
			firePropertyChange("player", null, players.getSelectedItem());
		}
	}

	/**
	 * Turn a string into left-aligned HTML.
	 *
	 * @param string a string
	 * @return it wrapped in HTML code that should make it left-aligned.
	 */
	private static String htmlize(final String string) {
		return "<html><p align=\"left\">" + string + "</p></html>";
	}

	/**
	 * Get a label's size given a fixed width. Adapted from
	 * http://blog.nobel-joergensen
	 * .com/2009/01/18/changing-preferred-size-of-a-html-jlabel/
	 *
	 * @param component the component we're laying out
	 * @param width the width we're working within
	 * @return the "ideal" dimensions for the component
	 */
	public static Dimension getComponentPreferredSize(
			final JComponent component, final int width) {
		final View view = (View) component
				.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
		if (view == null) {
			return component.getPreferredSize(); // NOPMD
		} else {
			view.setSize(width, 0);
			final int wid = (int) Math.ceil(view.getPreferredSpan(View.X_AXIS));
			final int height = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
			return new Dimension(wid, height);
		}
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
					&& (evt.getNewValue() instanceof Skill || evt.getNewValue() == null)) {
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
		 * @return its name if it has one, "null" if null, or its toString
		 *         otherwise.
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
}
