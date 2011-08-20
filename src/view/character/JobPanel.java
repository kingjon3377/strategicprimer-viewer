package view.character;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.character.JobLevels;
import model.character.JobType;
import view.util.Applyable;

/**
 * A panel to represent levels in a Job.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class JobPanel extends JPanel implements Applyable, ActionListener {
	/**
	 * The panel this is embedded in.
	 */
	private final CharacterPanel outer;
	/**
	 * The Job this represents.
	 */
	private JobLevels job;
	/**
	 * A control to choose what type of job this is.
	 */
	private final JComboBox jobChoices;
	/**
	 * A control to specify how many levels in it.
	 */
	private final JTextField levels;

	/**
	 * Constructor.
	 * 
	 * @param cpanel
	 *            the panel this is embedded in
	 */
	public JobPanel(final CharacterPanel cpanel) {
		super(new GridLayout(0, 4));
		outer = cpanel;
		jobChoices = new JComboBox(JobType.values());
		levels = new JTextField(3);
		add(levels);
		add(new JLabel(" levels of "));
		add(jobChoices);
		final JButton button = new JButton("Remove Job");
		add(button);
		button.addActionListener(this);
	}

	/**
	 * Constructor.
	 * 
	 * @param cpanel
	 *            the panel this is embedded in
	 * @param charJob
	 *            the job this represents
	 */
	public JobPanel(final CharacterPanel cpanel, final JobLevels charJob) {
		this(cpanel);
		job = charJob;
	}

	/**
	 * @return the job this represents
	 */
	public JobLevels getJob() {
		return job;
	}

	/**
	 * @param newJob
	 *            the job this now represents
	 */
	public void setJob(final JobLevels newJob) {
		job = newJob;
		revert();
	}

	/**
	 * Apply user changes to the job.
	 */
	@Override
	public void apply() {
		if (job == null) {
			job = new JobLevels((JobType) jobChoices.getSelectedItem(),
					Integer.parseInt(levels.getText()));
		} else {
			job.setJob((JobType) jobChoices.getSelectedItem());
			job.setLevels(Integer.parseInt(levels.getText()));
		}
	}
	
	/**
	 * Revert all changes made in the panel.
	 *
	 * @see view.util.Applyable#revert()
	 */
	@Override
	public void revert() {
		if (job == null) {
			jobChoices.setSelectedItem(0);
			levels.setText("0");
		} else {
			jobChoices.setSelectedItem(job.getJob());
			levels.setText(Integer.toString(job.getLevels()));
		}
	}

	/**
	 * Handle button presses.
	 * @param event the button press being handled.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if ("Remove Job".equals(event.getActionCommand())) {
			outer.removeJob(this);
		}
	}
}
