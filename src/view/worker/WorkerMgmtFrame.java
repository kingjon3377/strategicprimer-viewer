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
import model.workermgmt.UnitListModel;
import model.workermgmt.UnitMemberListModel;
import util.PropertyChangeSource;
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
	private final JComboBox<Job> jobs = new JComboBox<Job>();
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
		panelOne.add(new JLabel("Current Player:"));
		panelOne.add(players);
		panelOne.add(new JLabel("Player's Units:"));
		units.addListSelectionListener(this);
		units.setModel(new UnitListModel(source, source, this));
		panelOne.add(units);
		add(panelOne);

		final JPanel panelTwo = new JPanel();
		panelTwo.setLayout(new BoxLayout(panelTwo, BoxLayout.PAGE_AXIS));
		panelTwo.add(new JLabel("Selected Unit's Members:"));
		members.setModel(new UnitMemberListModel(this));
		members.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		members.addListSelectionListener(this);
		panelTwo.add(members);
		// TODO: a label showing stats
		jobs.addItemListener(this);
		panelTwo.add(new JLabel("Worker's Jobs:"));
		panelTwo.add(jobs);
		skills.addItemListener(this);
		panelTwo.add(new JLabel("Skills in selected Job:"));
		panelTwo.add(skills);
		add(panelTwo);

		addPropertyChangeListener(this);
		firePropertyChange("map", null, null);
		removePropertyChangeListener(this);

		setMinimumSize(new Dimension(640, 480));
		final List<JComponent> lists = new ArrayList<JComponent>();
		lists.add(players);
		lists.add(jobs);
		lists.add(skills);
		lists.add(units);
		lists.add(members);
		getContentPane().addComponentListener(new ComponentAdapter() {
			/**
			 * Adjust the size of the sub-panels when this is resized.
			 * @param evt the event being handled
			 */
			@Override
			public void componentResized(final ComponentEvent evt) {
				panelOne.setMaximumSize(new Dimension(getWidth() / 2 - 1, getHeight()));
				panelTwo.setMaximumSize(new Dimension(getWidth() / 2 - 1, getHeight()));
				panelOne.setPreferredSize(new Dimension(getWidth() / 2 - 1, getHeight()));
				panelTwo.setPreferredSize(new Dimension(getWidth() / 2 - 1, getHeight()));
				panelOne.setMinimumSize(new Dimension(getWidth() / 2 - 1, getHeight()));
				panelTwo.setMinimumSize(new Dimension(getWidth() / 2 - 1, getHeight()));
				for (JComponent list : lists) {
					if (list instanceof JComboBox) {
						list.setMaximumSize(new Dimension(getWidth() / 2 - 1, 20));
						list.setPreferredSize(new Dimension(getWidth() / 2 - 1, 20));
					} else if (list instanceof JList) {
						list.setMaximumSize(new Dimension(getWidth() / 2 - 1, getHeight()));
						list.setMinimumSize(new Dimension(getWidth() / 2 - 1, 20));
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
			jobs.removeAllItems();
			final UnitMember selection = members.getSelectedValue();
			// The Java Language Specification specifies that instanceof will
			// return false if the value is null, so no need to check.
			if (selection instanceof Worker) {
				final Worker worker = (Worker) selection;
				for (Job job : worker) {
					jobs.addItem(job);
				}
			}
		} else if (units.equals(evt.getSource())) {
			firePropertyChange("unit", null, units.getSelectedValue());
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
			jobs.removeAllItems();
			firePropertyChange("player", null, players.getSelectedItem());
		} else if (jobs.equals(evt.getSource())) {
			skills.removeAllItems();
			if (jobs.getSelectedItem() instanceof Job) {
				for (Skill skill : (Job) jobs.getSelectedItem()) {
					skills.addItem(skill);
				}
			}
		} else if (skills.equals(evt.getSource())) {
			// TODO: make it possible to improve that skill ... or at least show
			// the level and number of hours worked ...
		}
	}
}
