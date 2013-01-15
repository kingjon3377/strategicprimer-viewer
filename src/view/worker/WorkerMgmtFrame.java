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
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import model.viewer.MapModel;
import model.workermgmt.JobsListModel;
import model.workermgmt.UnitListModel;
import model.workermgmt.UnitMemberListModel;
import util.PropertyChangeSource;
import view.map.details.FixtureCellRenderer;
/**
 * A GUI to let a user manage workers.
 * @author Jonathan Lovelace
 *
 */
public class WorkerMgmtFrame extends JFrame implements ItemListener,
		PropertyChangeListener, ListSelectionListener, PropertyChangeSource {
	/**
	 * The map model containing the data we're working from.
	 */
	private final MapModel model;
	/**
	 * A drop-down list listing the players in the map.
	 */
	private final JComboBox<Player> players = new JComboBox<Player>();
	/**
	 * A not-drop-down list of the player's units in the map.
	 */
	private final JList<Unit> units = new JList<Unit>();
	/**
	 * A not-drop-down list of the members of the unit (mostly workers).
	 */
	private final JList<UnitMember> members = new JList<UnitMember>();
	/**
	 * A drop-down list of the worker's jobs. TODO: Make editable, so user can add new job.
	 */
	private final JList<Job> jobs = new JList<Job>();
	/**
	 * A non-drop-down list of the skills associated with that job. TODO: make editable, so user can add new skill.
	 */
	private final JComboBox<Skill> skills = new JComboBox<Skill>();
	/**
	 * Constructor.
	 * @param source the model containing the data to work from
	 */
	public WorkerMgmtFrame(final MapModel source) {
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
		units.addListSelectionListener(this);
		units.setModel(new UnitListModel(source, source, this));
		panelOne.add(units);
		add(panelOne);

		final JPanel panelTwo = new JPanel();
		panelTwo.setLayout(new BoxLayout(panelTwo, BoxLayout.PAGE_AXIS));
		final JLabel memberLabel = new JLabel(htmlize("Selected Unit's Members:"));
		panelTwo.add(memberLabel);
		members.setModel(new UnitMemberListModel(this));
		members.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		members.addListSelectionListener(this);
		panelTwo.add(members);
		final StatsLabel statsLabel = new StatsLabel(this);
		panelTwo.add(statsLabel);
		add(panelTwo);

		final JPanel panelThree = new JPanel();
		panelThree.setLayout(new BoxLayout(panelThree, BoxLayout.PAGE_AXIS));
		jobs.setModel(new JobsListModel(this));
		jobs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jobs.addListSelectionListener(this);
		final JLabel jobsLabel = new JLabel(htmlize("Worker's Jobs:"));
		panelThree.add(jobsLabel);
		panelThree.add(jobs);
		skills.addItemListener(this);
		final JLabel skillsLabel = new JLabel(htmlize("Skills in selected Job:"));
		panelThree.add(skillsLabel);
		panelThree.add(skills);
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
		getContentPane().addComponentListener(new ComponentAdapter() {
			/**
			 * Adjust the size of the sub-panels when this is resized.
			 * @param evt the event being handled
			 */
			@Override
			public void componentResized(final ComponentEvent evt) {
				final int width = getWidth() / 3;
				final int minHeight = 20;
				final int maxHeight = getHeight();
				for (JComponent list : lists) {
					if (list instanceof JComboBox) {
						list.setMaximumSize(new Dimension(width, minHeight));
						list.setPreferredSize(new Dimension(width, minHeight));
					} else if (list instanceof JList) {
						list.setMaximumSize(new Dimension(width, maxHeight));
						list.setMinimumSize(new Dimension(width, minHeight));
					} else if (list instanceof JLabel) {
						FixtureCellRenderer.setComponentPreferredSize(list, width);
						list.setMinimumSize(list.getPreferredSize());
					} else if (list instanceof JPanel) {
						list.setMaximumSize(new Dimension(width, maxHeight));
						list.setPreferredSize(new Dimension(width, maxHeight));
						list.setMinimumSize(new Dimension(width, maxHeight));
					}
				}
			}
		});
	}
	/**
	 * @param evt an event indicating a list's selection changed.
	 */
	@Override
	public void valueChanged(final ListSelectionEvent evt) {
		if (members.equals(evt.getSource())) {
			skills.removeAllItems();
			firePropertyChange("member", null, members.getSelectedValue());
		} else if (units.equals(evt.getSource())) {
			firePropertyChange("unit", null, units.getSelectedValue());
		} else if (jobs.equals(evt.getSource())) {
			skills.removeAllItems();
			if (jobs.getSelectedValue() != null) {
				for (Skill skill : jobs.getSelectedValue()) {
					skills.addItem(skill);
				}
			}
		}
	}
	/**
	 * Handle a property change.
	 * @param evt the property-change event to handle
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("map".equals(evt.getPropertyName())) {
			players.removeAllItems();
			for (Player player : model.getMainMap().getPlayers()) {
				players.addItem(player);
			}
		}
	}
	/**
	 * @param evt an event indicating an item's changed in one of the combo-boxes we listen to
	 */
	@Override
	public void itemStateChanged(final ItemEvent evt) {
		if (players.equals(evt.getSource())) {
			skills.removeAllItems();
			firePropertyChange("player", null, players.getSelectedItem());
		} else if (skills.equals(evt.getSource())) {
			// TODO: make it possible to improve that skill ... or at least show
			// the level and number of hours worked ...
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
}
