package view.worker;

import static model.map.fixtures.mobile.worker.WorkerStats.getModifierString;

import java.awt.GridLayout;
import java.util.Iterator;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.eclipse.jdt.annotation.Nullable;

import model.listeners.UnitMemberListener;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.ProxyFor;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.WorkerStats;
/**
 * A panel to show the details of the currently selected unit-member.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 *
 * @author Jonathan Lovelace
 */
public final class MemberDetailPanel extends JPanel implements UnitMemberListener {
	/**
	 * The currently selected unit member, or null if no selection.
	 */
	@Nullable private UnitMember current = null;
	/**
	 * The label to say what kind of unit member this is.
	 */
	private final JLabel typeLabel = new JLabel("member type");
	/**
	 * The label to say the name of the unit member.
	 */
	private final JLabel nameLabel = new JLabel("member name");
	/**
	 * The label to say the race or kind of the unit member.
	 */
	private final JLabel kindLabel = new JLabel("member kind");
	/**
	 * The label to say a worker's strength.
	 */
	private final JLabel strLabel = new JLabel("+NaN");
	/**
	 * The label to say a worker's dexterity.
	 */
	private final JLabel dexLabel = new JLabel("+NaN");
	/**
	 * The label to say a worker's constitution.
	 */
	private final JLabel conLabel = new JLabel("+NaN");
	/**
	 * The label to say a worker's intelligence.
	 */
	private final JLabel intLabel = new JLabel("+NaN");
	/**
	 * The label to say a worker's wisdom.
	 */
	private final JLabel wisLabel = new JLabel("+NaN");
	/**
	 * The label to say a worker's charisma.
	 */
	private final JLabel chaLabel = new JLabel("+NaN");
	/**
	 * The subpanel to show a worker's Job experience or training.
	 */
	private final JPanel jobsPanel = new JPanel(new GridLayout(0, 1));
	/**
	 * Constructor: lay out, then clear, the panel.
	 */
	public MemberDetailPanel() {
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		final JLabel header = new JLabel("<html><h2>Unit Member Details:</h2></html>");
		final JLabel typeCaption = new JLabel("<html><b>Member Type:</b></html>");
		final JLabel nameCaption = new JLabel("<html><b>Name:</b></html>");
		final JLabel kindCaption = new JLabel("<html><b>Race or Kind:</b></html>");
		final JLabel strCaption = new JLabel("<html><b>Str:</b></html>");
		final JLabel dexCaption = new JLabel("<html><b>Dex:</b></html>");
		final JLabel conCaption = new JLabel("<html><b>Con:</b></html>");
		final JLabel intCaption = new JLabel("<html><b>Int:</b></html>");
		final JLabel wisCaption = new JLabel("<html><b>Wis:</b></html>");
		final JLabel chaCaption = new JLabel("<html><b>Cha:</b></html>");
		final JLabel jobsCaption = new JLabel("<html><b>Job Levels:</b></html>");
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
		layout.linkSize(SwingConstants.HORIZONTAL, typeCaption, nameCaption,
				kindCaption, jobsCaption);
		layout.linkSize(SwingConstants.HORIZONTAL, typeLabel, nameLabel,
				kindLabel, jobsPanel);
		layout.linkSize(strCaption, dexCaption, conCaption, intCaption,
				wisCaption, chaCaption);
		layout.linkSize(strLabel, dexLabel, conLabel, intLabel, wisLabel, chaLabel);
		layout.linkSize(SwingConstants.VERTICAL, typeCaption, typeLabel);
		layout.linkSize(SwingConstants.VERTICAL, nameCaption, nameLabel);
		layout.linkSize(SwingConstants.VERTICAL, kindCaption, kindLabel);
		recache();
	}

	/**
	 * Handle a member-selection event. Only trigger an invalidation of the
	 * content if it's a different member than before.
	 * @param old what the caller thinks was the previously selected member
	 * @param selected the newly selected unit member
	 */
	@Override
	public void memberSelected(@Nullable final UnitMember old,
			@Nullable final UnitMember selected) {
		if (selected instanceof ProxyFor) {
			if (((ProxyFor<?>) selected).isParallel()) {
				@SuppressWarnings("unchecked")
				final Iterator<? extends UnitMember> iter = ((ProxyFor<? extends UnitMember>) selected)
						                                            .getProxied().iterator();
				if (iter.hasNext()) {
					memberSelected(old, iter.next());
					return;
				}
			} else {
				memberSelected(old, null);
				return;
			}
		}
		if (selected != current) {
			current = selected;
			recache();
		}
	}
	/**
	 * Invalidate and recompute the display.
	 */
	private void recache() {
		final UnitMember local = current;
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
			final WorkerStats stats = ((Worker) local).getStats();
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
			for (final IJob job : (Worker) local) {
				final JLabel label = new JLabel(job.getName() + ' ' + job.getLevel());
				final StringBuilder builder = new StringBuilder(2048);
				boolean first = true;
				for (final ISkill skill : job) {
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
