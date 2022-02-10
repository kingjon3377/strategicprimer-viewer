package drivers.worker_mgmt;

import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.StreamSupport;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.io.IOException;

import java.util.List;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTabbedPane;

import lovelace.util.FunctionalGroupLayout;
import static lovelace.util.FunctionalSplitPane.horizontalSplit;
import lovelace.util.BorderedPanel;
import lovelace.util.Platform;
import lovelace.util.InterpolatedLabel;

import common.map.HasPortrait;
import common.map.fixtures.UnitMember;
import common.map.fixtures.Implement;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.ProxyFor;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.MaturityModel;
import common.map.fixtures.mobile.AnimalPlurals;
import common.map.fixtures.mobile.worker.WorkerStats;
import common.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.ISkill;
import drivers.map_viewer.ImageLoader;
import java.util.Objects;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A panel to show the details of the currently selected unit-member.
 */
public class MemberDetailPanel extends BorderedPanel implements UnitMemberListener {
	private static final Logger LOGGER = Logger.getLogger(MemberDetailPanel.class.getName());
	public MemberDetailPanel(final JPanel resultsPanel, final JPanel notesPanel) { // TODO: Move initialization of those into here?
		final JPanel statPanel = new JPanel();
		final FunctionalGroupLayout statLayout = new FunctionalGroupLayout(statPanel, true, true);
		statPanel.setLayout(statLayout);
		statPanel.setBorder(BorderFactory.createEmptyBorder());

		final InterpolatedLabel<@Nullable WorkerStats> strLabel = statLabel(WorkerStats::getStrength);
		final InterpolatedLabel<@Nullable WorkerStats> dexLabel = statLabel(WorkerStats::getDexterity);
		final InterpolatedLabel<@Nullable WorkerStats> conLabel = statLabel(WorkerStats::getConstitution);
		final InterpolatedLabel<@Nullable WorkerStats> intLabel = statLabel(WorkerStats::getIntelligence);
		final InterpolatedLabel<@Nullable WorkerStats> wisLabel = statLabel(WorkerStats::getWisdom);
		final InterpolatedLabel<@Nullable WorkerStats> chaLabel = statLabel(WorkerStats::getCharisma);
		statLabels = Collections.unmodifiableList(Arrays.asList(strLabel, dexLabel, conLabel,
			intLabel, wisLabel, chaLabel));

		final JLabel typeCaption = caption("Member Type");
		typeLabel = new JLabel("member type");
		final JLabel nameCaption = caption("Name");
		nameLabel = new JLabel("member name");
		final JLabel kindCaption = caption("Race or Kind");
		kindLabel = new JLabel("member kind");
		final JLabel strCaption = caption("Str");
		final JLabel dexCaption = caption("Dex");
		final JLabel conCaption = caption("Con");
		final JLabel intCaption = caption("Int");
		final JLabel wisCaption = caption("Wis");
		final JLabel chaCaption = caption("Cha");
		final JLabel jobsCaption = caption("Job Levels");

		jobsPanel = new JPanel(new GridLayout(0, 1));

		statLayout.setVerticalGroup(statLayout.sequentialGroupOf(
			statLayout.parallelGroupOf(typeCaption, typeLabel),
			statLayout.parallelGroupOf(nameCaption, nameLabel),
			statLayout.parallelGroupOf(kindCaption, kindLabel),
			statLayout.parallelGroupOf(strCaption, strLabel, intCaption, intLabel),
			statLayout.parallelGroupOf(dexCaption, dexLabel, wisCaption, wisLabel),
			statLayout.parallelGroupOf(conCaption, conLabel, chaCaption, chaLabel),
			statLayout.parallelGroupOf(jobsCaption, jobsPanel)));

		statLayout.setHorizontalGroup(statLayout.parallelGroupOf(
			statLayout.sequentialGroupOf(
				statLayout.parallelGroupOf(typeCaption, nameCaption, kindCaption,
					statLayout.sequentialGroupOf(strCaption, strLabel),
					statLayout.sequentialGroupOf(dexCaption, dexLabel),
					statLayout.sequentialGroupOf(conCaption, conLabel), jobsCaption),
				statLayout.parallelGroupOf(typeLabel, nameLabel, kindLabel,
					statLayout.sequentialGroupOf(intCaption, intLabel),
					statLayout.sequentialGroupOf(wisCaption, wisLabel),
					statLayout.sequentialGroupOf(chaCaption, chaLabel), jobsPanel))));

		statLayout.linkSize(SwingConstants.HORIZONTAL, typeCaption, nameCaption,
			kindCaption, jobsCaption);
		statLayout.linkSize(SwingConstants.HORIZONTAL, typeLabel, nameLabel,
			kindLabel, jobsPanel);
		statLayout.linkSize(strCaption, dexCaption, conCaption, intCaption,
			wisCaption, chaCaption);
		statLayout.linkSize(statLabels.toArray(new InterpolatedLabel[0]));
		statLayout.linkSize(SwingConstants.VERTICAL, typeCaption, typeLabel);
		statLayout.linkSize(SwingConstants.VERTICAL, nameCaption, nameLabel);
		statLayout.linkSize(SwingConstants.VERTICAL, kindCaption, kindLabel);

		final JScrollPane statPanelWrapped = new JScrollPane(horizontalSplit(statPanel,
			portraitComponent, 0.6),
				(Platform.SYSTEM_IS_MAC) ? ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
					: ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				(Platform.SYSTEM_IS_MAC) ? ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
					: ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		final JTabbedPane tabbed = new JTabbedPane();

		tabbed.addTab("Stats",statPanelWrapped);
		tabbed.addTab("Results", resultsPanel);
		tabbed.addTab("Notes", notesPanel);
		setPageStart(new JLabel("<html><h2>Unit Member Details:</h2></html>"));
		setCenter(tabbed);
		recache();
	}

	private final JLabel nameLabel;
	private final JLabel kindLabel;
	private final JLabel typeLabel;
	private final JPanel jobsPanel;

	private final PortraitComponent portraitComponent = new PortraitComponent();

	private static Function<@Nullable WorkerStats, String> labelFormat(final ToIntFunction<WorkerStats> stat) {
		return (stats) -> {
			if (stats == null) {
				return "";
			} else {
				return WorkerStats.getModifierString(stat.applyAsInt(stats));
			}
		};
	}

	private static InterpolatedLabel<@Nullable WorkerStats> statLabel(final ToIntFunction<WorkerStats> stat) {
		return new InterpolatedLabel<>(labelFormat(stat), null);
	}

	private final List<InterpolatedLabel<@Nullable WorkerStats>> statLabels;

	private static JLabel caption(final String string) {
		return new JLabel("<html><b>" + string + ":</b></html>");
	}

	private static class PortraitComponent extends JComponent {
		@Nullable
		private Image portrait = null;

		public void setPortrait(final Image portrait) {
			this.portrait = portrait;
		}

		@Override
		public void paintComponent(final Graphics pen) {
			super.paintComponent(pen);
			if (portrait != null) {
				pen.drawImage(portrait, 0, 0, getWidth(), getHeight(), this);
			}
		}
	}

	@Nullable
	private UnitMember current = null;


	private static String skillString(final ISkill skill) {
		return skill.getName() + " " + skill.getLevel();
	}

	private void recache() {
		final UnitMember local = current;
		jobsPanel.removeAll();
		if (local instanceof IWorker) {
			final IWorker worker = (IWorker) local;
			typeLabel.setText("Worker");
			nameLabel.setText(worker.getName());
			kindLabel.setText(worker.getKind());
			final WorkerStats stats = worker.getStats();
			for (final InterpolatedLabel<@Nullable WorkerStats> label : statLabels) {
				label.setArgument(stats);
			}
			for (final IJob job : worker) {
				if (job.isEmpty()) {
					continue;
				}
				final JLabel label = new JLabel(String.format("%s %d", job.getName(),
					job.getLevel()));
				if (job.iterator().hasNext()) {
					label.setToolTipText(StreamSupport.stream(job.spliterator(), false)
						.map(MemberDetailPanel::skillString)
						.collect(Collectors.joining(", ", "Skills: ", "")));
				}
				jobsPanel.add(label);
			}
		} else if (local instanceof Animal) {
			final Animal animal = (Animal) local;
			final String plural = AnimalPlurals.get(animal.getKind());
				final Map<String, Integer> maturityAges = MaturityModel.getMaturityAges();
			if (animal.getBorn() >= 0 && MaturityModel.getCurrentTurn() >= 0 &&
					maturityAges.containsKey(animal.getKind()) &&
					MaturityModel.getCurrentTurn() - animal.getBorn() <
						maturityAges.get(animal.getKind())) {
				if (animal.getPopulation() > 1) {
					typeLabel.setText("Young Animals");
					kindLabel.setText(String.format("%d young %s",
						animal.getPopulation(), plural));
				} else {
					typeLabel.setText("Young Animal");
					kindLabel.setText("Young " + animal.getKind());
				}
			} else {
				if (animal.getPopulation() > 1) {
					typeLabel.setText("Animals");
					kindLabel.setText(String.format("%d %s", animal.getPopulation(),
						plural));
				} else {
					typeLabel.setText("Animal");
					kindLabel.setText(animal.getKind());
				}
			}
			nameLabel.setText("");
			for (final InterpolatedLabel<@Nullable WorkerStats> label : statLabels) {
				label.setArgument(null);
			}
		} else if (local instanceof Implement) {
			final Implement eq = (Implement) local;
			typeLabel.setText("Equipment");
			nameLabel.setText("");
			if (eq.getCount() > 1) {
				kindLabel.setText(String.format("%d x %s", eq.getCount(), eq.getKind()));
			} else {
				kindLabel.setText(eq.getKind());
			}
			for (final InterpolatedLabel<@Nullable WorkerStats> label : statLabels) {
				label.setArgument(null);
			}
		} else if (local instanceof IResourcePile) {
			final IResourcePile rp = (IResourcePile) local;
			typeLabel.setText("Resource");
			nameLabel.setText("");
			kindLabel.setText(String.format("%s %s (%s)", rp.getQuantity(), rp.getContents(),
				rp.getKind()));
			for (final InterpolatedLabel<@Nullable WorkerStats> label : statLabels) {
				label.setArgument(null);
			}
		} else if (local == null) {
			typeLabel.setText("");
			nameLabel.setText("");
			kindLabel.setText("");
			for (final InterpolatedLabel<@Nullable WorkerStats> label : statLabels) {
				label.setArgument(null);
			}
		} else {
			typeLabel.setText("Unknown");
			nameLabel.setText("");
			kindLabel.setText(local.getClass().getName());
			for (final InterpolatedLabel<@Nullable WorkerStats> label : statLabels) {
				label.setArgument(null);
			}
		}
		portraitComponent.setPortrait(null);
		if (local instanceof HasPortrait) {
			final String portraitName = ((HasPortrait) local).getPortrait();
			if (!portraitName.isEmpty()) {
				try {
					portraitComponent.setPortrait(ImageLoader.loadImage(portraitName));
				} catch (final IOException except) {
					LOGGER.log(Level.WARNING, "Failed to load portrait", except);
				}
			}
		}
		portraitComponent.repaint();
	}

	@Override
	public void memberSelected(@Nullable final UnitMember old, @Nullable final UnitMember selected) {
		if (selected instanceof ProxyFor) {
			if (((ProxyFor<? extends UnitMember>) selected).isParallel()) {
				final Iterator<? extends UnitMember> proxied =
					((ProxyFor<? extends UnitMember>) selected).getProxied().iterator();
				if (proxied.hasNext()) {
					memberSelected(old, proxied.next());
					return;
				}
			} else {
				memberSelected(old, null);
				return;
			}
		}
		if (selected != null) {
			if (!Objects.equals(current, selected)) {
				current = selected;
				recache();
			}
		} else if (current != null) {
			current = null;
			recache();
		}
	}
}
