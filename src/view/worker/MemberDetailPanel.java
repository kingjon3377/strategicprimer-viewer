package view.worker;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.ToIntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import model.listeners.UnitMemberListener;
import model.map.HasPortrait;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.ProxyFor;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.WorkerStats;
import org.eclipse.jdt.annotation.Nullable;
import util.ImageLoader;
import util.TypesafeLogger;
import view.util.FunctionalGroupLayout;
import view.util.SplitWithWeights;

import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;
import static model.map.fixtures.mobile.worker.WorkerStats.getModifierString;

/**
 * A panel to show the details of the currently selected unit-member.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class MemberDetailPanel extends JPanel implements UnitMemberListener {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			TypesafeLogger.getLogger(MemberDetailPanel.class);
	/**
	 * The component displaying the portrait of the currently selected unit member, if
	 * it has one.
	 */
	private final PortraitComponent portraitComponent = new PortraitComponent();
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
	 * The labels to say a worker's stats.
	 */
	private final Collection<StatLabel> statLabels = new ArrayList<>(6);
	/**
	 * The sub-panel to show a worker's Job experience or training.
	 */
	private final JPanel jobsPanel = new JPanel(new GridLayout(0, 1));
	/**
	 * The currently selected unit member, or null if no selection.
	 */
	@Nullable
	private UnitMember current = null;

	/**
	 * Constructor: lay out, then clear, the panel.
	 *
	 * @param resultsPanel the panel to display unit results
	 */
	public MemberDetailPanel(final JPanel resultsPanel) {
		super(new BorderLayout());
		final JComponent split = SplitWithWeights.verticalSplit(0.5, 0.5,
				SplitWithWeights.horizontalSplit(0.6, 0.6,
						createStatPanel(), portraitComponent), resultsPanel);
		split.setBorder(BorderFactory.createEmptyBorder());
		add(split);
		recache();
	}

	/**
	 * Create the "stat panel".
	 * @return the panel for showing worker stats, all laid out.
	 */
	private JPanel createStatPanel() {
		final JPanel groupedPanel = new JPanel();
		final FunctionalGroupLayout layout = new FunctionalGroupLayout(groupedPanel);
		groupedPanel.setLayout(layout);
		groupedPanel.setBorder(BorderFactory.createEmptyBorder());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		final StatLabel strLabel = new StatLabel(WorkerStats::getStrength);
		final StatLabel dexLabel = new StatLabel(WorkerStats::getDexterity);
		final StatLabel conLabel = new StatLabel(WorkerStats::getConstitution);
		final StatLabel intLabel = new StatLabel(WorkerStats::getIntelligence);
		final StatLabel wisLabel = new StatLabel(WorkerStats::getWisdom);
		final StatLabel chaLabel = new StatLabel(WorkerStats::getCharisma);
		statLabels.addAll(Arrays.asList(strLabel, dexLabel, conLabel, intLabel, wisLabel,
				chaLabel));
		add(new JLabel("<html><h2>Unit Member Details:</h2></html>"),
				BorderLayout.PAGE_START);
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
		layout.setVerticalGroup(layout.createSequentialGroupOf(
				layout.createParallelGroupOf(typeCaption, typeLabel),
				layout.createParallelGroupOf(nameCaption, nameLabel),
				layout.createParallelGroupOf(kindCaption, kindLabel),
				layout.createParallelGroupOf(strCaption, strLabel, intCaption, intLabel),
				layout.createParallelGroupOf(dexCaption, dexLabel, wisCaption, wisLabel),
				layout.createParallelGroupOf(conCaption, conLabel, chaCaption, chaLabel),
				layout.createParallelGroupOf(jobsCaption, jobsPanel)));
		layout.setHorizontalGroup(layout.createParallelGroupOf(
				layout.createSequentialGroupOf(
						layout.createParallelGroupOf(typeCaption, nameCaption,
								kindCaption,
								layout.createSequentialGroupOf(strCaption, strLabel),
								layout.createSequentialGroupOf(dexCaption, dexLabel),
								layout.createSequentialGroupOf(conCaption, conLabel),
								jobsCaption),
						layout.createParallelGroupOf(typeLabel, nameLabel, kindLabel,
								layout.createSequentialGroupOf(intCaption, intLabel),
								layout.createSequentialGroupOf(wisCaption, wisLabel),
								layout.createSequentialGroupOf(chaCaption, chaLabel),
								jobsPanel))));
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
		return groupedPanel;
	}

	/**
	 * Handle a member-selection event. Only trigger an invalidation of the content if
	 * it's a different member than before.
	 *
	 * @param old      what the caller thinks was the previously selected member
	 * @param selected the newly selected unit member
	 */
	@SuppressWarnings("ObjectEquality")
	@Override
	public void memberSelected(@Nullable final UnitMember old,
							   @Nullable final UnitMember selected) {
		if (selected instanceof ProxyFor) {
			if (((ProxyFor<?>) selected).isParallel()) {
				@SuppressWarnings("unchecked")
				final Iterator<? extends UnitMember> iter =
						((ProxyFor<? extends UnitMember>) selected)
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
			for (final StatLabel label : statLabels) {
				label.recache(null);
			}
			jobsPanel.removeAll();
		} else if (local instanceof Worker) {
			typeLabel.setText("Worker");
			final Worker worker = (Worker) local;
			nameLabel.setText(worker.getName());
			kindLabel.setText(worker.getKind());
			final WorkerStats stats = worker.getStats();
			for (final StatLabel label : statLabels) {
				label.recache(stats);
			}
			jobsPanel.removeAll();
			for (final IJob job : worker) {
				if (job.isEmpty()) {
					continue;
				}
				//noinspection ObjectAllocationInLoop
				final JLabel label = new JLabel(job.getName() + ' ' + job.getLevel());
				final String skills = stream(job.spliterator(), false)
											  .map(skill -> skill.getName() + ' ' +
																	skill.getLevel())
											  .collect(joining(", ", "Skills: ", ""));
				if ("Skills: ".length() != skills.length()) {
					label.setToolTipText(skills);
				}
				jobsPanel.add(label);
			}
		} else if (local instanceof Animal) {
			typeLabel.setText("Animal");
			nameLabel.setText("");
			kindLabel.setText(((Animal) local).getKind());
			for (final StatLabel label : statLabels) {
				label.recache(null);
			}
			jobsPanel.removeAll();
		} else {
			typeLabel.setText("Unknown");
			nameLabel.setText("");
			kindLabel.setText(local.getClass().getSimpleName());
			for (final StatLabel label : statLabels) {
				label.recache(null);
			}
			jobsPanel.removeAll();
		}
		portraitComponent.setPortrait(null);
		if (local instanceof HasPortrait) {
			final String portraitName = ((HasPortrait) local).getPortrait();
			if (!portraitName.isEmpty()) {
				try {
					portraitComponent
							.setPortrait(ImageLoader.getLoader().loadImage
																		 (portraitName));
				} catch (final IOException except) {
					LOGGER.log(Level.WARNING, "Failed to load portrait", except);
				}
			}
		}
		portraitComponent.repaint();
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings("unused")
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization.
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings("unused")
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * A simple toString().
	 * @return a diagnostic String
	 */
	@Override
	public String toString() {
		return "MemberDetailPanel, currently showing a " + typeLabel.getText();
	}

	/**
	 * A label to represent a stat.
	 */
	private static class StatLabel extends JLabel {
		/**
		 * A handle for the stat in question.
		 */
		private final ToIntFunction<WorkerStats> handle;

		/**
		 * Constructor.
		 *
		 * @param stat a handle for the stat we represent
		 */
		protected StatLabel(final ToIntFunction<WorkerStats> stat) {
			super("+NaN");
			handle = stat;
		}

		/**
		 * Update the label.
		 *
		 * @param stats the stats to show
		 */
		public void recache(@Nullable final WorkerStats stats) {
			if (stats == null) {
				setText("");
			} else {
				setText(getModifierString(handle.applyAsInt(stats)));
			}
		}

		/**
		 * Prevent serialization.
		 *
		 * @param out ignored
		 * @throws IOException always
		 */
		@SuppressWarnings("unused")
		private void writeObject(final ObjectOutputStream out) throws IOException {
			throw new NotSerializableException("Serialization is not allowed");
		}

		/**
		 * Prevent serialization.
		 *
		 * @param in ignored
		 * @throws IOException            always
		 * @throws ClassNotFoundException never
		 */
		@SuppressWarnings("unused")
		private void readObject(final ObjectInputStream in)
				throws IOException, ClassNotFoundException {
			throw new NotSerializableException("Serialization is not allowed");
		}

		/**
		 * A simple toString().
		 * @return a diagnostic String
		 */
		@Override
		public String toString() {
			return "StatLabel: " + getText();
		}
	}

	/**
	 * A component to show the portrait of the currently selected member.
	 */
	@SuppressWarnings("ClassHasNoToStringMethod")
	private static class PortraitComponent extends JComponent {
		/**
		 * The image to draw.
		 */
		@SuppressWarnings("FieldHasSetterButNoGetter")
		@Nullable
		private Image portrait = null;

		/**
		 * Set the portrait to show.
		 * @param image the image to draw from now on
		 */
		public void setPortrait(@Nullable final Image image) {
			portrait = image;
		}

		/**
		 * Draw the component.
		 * @param pen the graphics context
		 */
		@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
		@Override
		protected void paintComponent(@Nullable final Graphics pen) {
			super.paintComponent(pen);
			final Image local = portrait;
			if ((local != null) && (pen != null)) {
				pen.drawImage(local, 0, 0, getWidth(), getHeight(), this);
			}
		}

		/**
		 * Prevent serialization.
		 *
		 * @param out ignored
		 * @throws IOException always
		 */
		@SuppressWarnings("unused")
		private void writeObject(final ObjectOutputStream out) throws IOException {
			throw new NotSerializableException("Serialization is not allowed");
		}

		/**
		 * Prevent serialization.
		 *
		 * @param in ignored
		 * @throws IOException            always
		 * @throws ClassNotFoundException never
		 */
		@SuppressWarnings("unused")
		private void readObject(final ObjectInputStream in)
				throws IOException, ClassNotFoundException {
			throw new NotSerializableException("Serialization is not allowed");
		}
	}
}
