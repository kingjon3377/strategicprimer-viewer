package view.worker;

import static model.map.fixtures.mobile.worker.WorkerStats.getModifierString;

import java.awt.GridLayout;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.eclipse.jdt.annotation.Nullable;

import model.listeners.UnitMemberListener;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.WorkerStats;

public class MemberDetailPanel extends JPanel implements UnitMemberListener {
	@Nullable private UnitMember current = null;
	private final JLabel typeLabel = new JLabel("member type");
	private final JLabel nameLabel = new JLabel("member name");
	private final JLabel kindLabel = new JLabel("member kind");
	private final JLabel strLabel = new JLabel("+NaN");
	private final JLabel dexLabel = new JLabel("+NaN");
	private final JLabel conLabel = new JLabel("+NaN");
	private final JLabel intLabel = new JLabel("+NaN");
	private final JLabel wisLabel = new JLabel("+NaN");
	private final JLabel chaLabel = new JLabel("+NaN");
	private final JPanel jobsPanel = new JPanel(new GridLayout(0, 1));
	public MemberDetailPanel() {
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		JLabel header = new JLabel("<html><h2>Unit Member Details:</h2></html>");
		JLabel typeCaption = new JLabel("<html><b>Member Type:</b></html>");
		JLabel nameCaption = new JLabel("<html><b>Name:</b></html>");
		JLabel kindCaption = new JLabel("<html><b>Race or Kind:</b></html>");
		JLabel strCaption = new JLabel("<html><b>Str:</b></html>");
		JLabel dexCaption = new JLabel("<html><b>Dex:</b></html>");
		JLabel conCaption = new JLabel("<html><b>Con:</b></html>");
		JLabel intCaption = new JLabel("<html><b>Int:</b></html>");
		JLabel wisCaption = new JLabel("<html><b>Wis:</b></html>");
		JLabel chaCaption = new JLabel("<html><b>Cha:</b></html>");
		JLabel jobsCaption = new JLabel("<html><b>Job Levels:</b></html>");
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(header)
				.addGroup(layout.createParallelGroup().addComponent(typeCaption)
						.addComponent(typeLabel))
				.addGroup(layout.createParallelGroup().addComponent(nameCaption)
						.addComponent(nameLabel))
				.addGroup(layout.createParallelGroup().addComponent(kindCaption)
						.addComponent(kindLabel))
				.addGroup(layout.createParallelGroup().addComponent(strCaption)
						.addComponent(strLabel).addComponent(intCaption)
						.addComponent(intLabel))
				.addGroup(layout.createParallelGroup().addComponent(dexCaption)
						.addComponent(dexLabel).addComponent(wisCaption)
						.addComponent(wisLabel))
				.addGroup(layout.createParallelGroup().addComponent(conCaption)
						.addComponent(conLabel).addComponent(chaCaption)
						.addComponent(chaLabel))
				.addGroup(layout.createParallelGroup().addComponent(jobsCaption)
						.addComponent(jobsPanel)));
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(header)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup()
								.addComponent(typeCaption)
								.addComponent(nameCaption)
								.addComponent(kindCaption)
								.addGroup(layout.createSequentialGroup()
										.addComponent(strCaption)
										.addComponent(strLabel))
								.addGroup(layout.createSequentialGroup()
										.addComponent(dexCaption)
										.addComponent(dexLabel))
								.addGroup(layout.createSequentialGroup()
										.addComponent(conCaption)
										.addComponent(conLabel))
								.addComponent(jobsCaption))
						.addGroup(layout.createParallelGroup()
								.addComponent(typeLabel)
								.addComponent(nameLabel)
								.addComponent(kindLabel)
								.addGroup(layout.createSequentialGroup()
										.addComponent(intCaption)
										.addComponent(intLabel))
								.addGroup(layout.createSequentialGroup()
										.addComponent(wisCaption)
										.addComponent(wisLabel))
								.addGroup(layout.createSequentialGroup()
										.addComponent(chaCaption)
										.addComponent(chaLabel))
								.addComponent(jobsPanel)
						)));
		layout.linkSize(SwingConstants.HORIZONTAL, typeCaption, nameCaption, kindCaption, jobsCaption);
		layout.linkSize(SwingConstants.HORIZONTAL, typeLabel, nameLabel, kindLabel, jobsPanel);
		layout.linkSize(strCaption, dexCaption, conCaption, intCaption, wisCaption, chaCaption);
		layout.linkSize(strLabel, dexLabel, conLabel, intLabel, wisLabel, chaLabel);
		layout.linkSize(SwingConstants.VERTICAL, typeCaption, typeLabel);
		layout.linkSize(SwingConstants.VERTICAL, nameCaption, nameLabel);
		layout.linkSize(SwingConstants.VERTICAL, kindCaption, kindLabel);
		recache();
	}
	@Override
	public void memberSelected(@Nullable final UnitMember old, @Nullable final UnitMember selected) {
		if (selected != current) {
			current = selected;
			recache();
		}
	}
	private void recache() {
		UnitMember local = current;
		if (local == null) {
			typeLabel.setText("");
			nameLabel.setText("");
			kindLabel.setText("");
			strLabel.setText("");
			dexLabel.setText("");
			conLabel.setText("");
			intLabel.setText("");
			wisLabel.setText("");
			chaLabel.setText("");
			jobsPanel.removeAll();
		} else if (local instanceof Worker) {
			typeLabel.setText("Worker");
			nameLabel.setText(((Worker) local).getName());
			kindLabel.setText(((Worker) local).getKind());
			WorkerStats stats = ((Worker) local).getStats();
			if (stats == null) {
				strLabel.setText("");
				dexLabel.setText("");
				conLabel.setText("");
				intLabel.setText("");
				wisLabel.setText("");
				chaLabel.setText("");
			} else {
				strLabel.setText(getModifierString(stats.getStrength()));
				dexLabel.setText(getModifierString(stats.getDexterity()));
				conLabel.setText(getModifierString(stats.getConstitution()));
				intLabel.setText(getModifierString(stats.getIntelligence()));
				wisLabel.setText(getModifierString(stats.getWisdom()));
				chaLabel.setText(getModifierString(stats.getCharisma()));
			}
			jobsPanel.removeAll();
			for (IJob job : (Worker) local) {
				JLabel label = new JLabel(job.getName() + ' ' + job.getLevel());
				StringBuilder builder = new StringBuilder();
				boolean first = true;
				for (ISkill skill : job) {
					if (first) {
						first = false;
						builder.append("Skills: ");
					} else {
						builder.append(", ");
					}
					builder.append(skill.getName());
					builder.append(' ');
					builder.append(skill.getLevel());
				}
				if (!first) {
					label.setToolTipText(builder.toString());
				}
				jobsPanel.add(label);
			}
		} else if (local instanceof Animal) {
			typeLabel.setText("Animal");
			nameLabel.setText("");
			kindLabel.setText(((Animal) local).getKind());
			strLabel.setText("");
			dexLabel.setText("");
			conLabel.setText("");
			intLabel.setText("");
			wisLabel.setText("");
			chaLabel.setText("");
			jobsPanel.removeAll();
		} else {
			typeLabel.setText("Unknown");
			nameLabel.setText("");
			kindLabel.setText(local.getClass().getSimpleName());
			strLabel.setText("");
			dexLabel.setText("");
			conLabel.setText("");
			intLabel.setText("");
			wisLabel.setText("");
			chaLabel.setText("");
			jobsPanel.removeAll();
		}
	}
}
