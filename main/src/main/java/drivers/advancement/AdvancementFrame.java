package drivers.advancement;

import drivers.common.ISPDriver;
import drivers.common.cli.ICLIHelper;
import org.jetbrains.annotations.Nullable;

import java.awt.Dimension;
import java.io.Serial;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import lovelace.util.ListenedButton;
import lovelace.util.BorderedPanel;

import static lovelace.util.FunctionalSplitPane.verticalSplit;
import static lovelace.util.FunctionalSplitPane.horizontalSplit;

import lovelace.util.FormattedLabel;

import legacy.idreg.IDFactoryFiller;
import legacy.idreg.IDRegistrar;

import legacy.map.ILegacyMap;
import legacy.map.Player;

import static drivers.worker_mgmt.WorkerMenu.workerMenu;

import drivers.worker_mgmt.TreeExpansionOrderListener;
import drivers.worker_mgmt.WorkerTreeModelAlt;
import drivers.worker_mgmt.TreeExpansionHandler;
import drivers.worker_mgmt.WorkerTree;

import worker.common.IWorkerTreeModel;

import drivers.common.PlayerChangeListener;
import drivers.common.IWorkerModel;

import legacy.xmlio.MapIOHelper;

import drivers.gui.common.SPFrame;
import drivers.gui.common.MenuBroker;

/**
 * A GUI to let a user manage workers.
 */
/* package */ final class AdvancementFrame extends SPFrame implements PlayerChangeListener {
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * Start with the frame's width equally divided between the two panels.
	 */
	private static final double DIVIDER_LOCATION = 0.5;
	/**
	 * As the user resizes the frame horizontally, give most of the weight to the right panel(?).
	 */
	private static final double RESIZE_WEIGHT = 0.3;
	private final FormattedLabel playerLabel;
	private final IWorkerTreeModel treeModel;

	public AdvancementFrame(final IWorkerModel model, final MenuBroker menuHandler, final ISPDriver driver,
							final ICLIHelper cli) {
		super("Worker Advancement", driver, new Dimension(640, 480), true,
				(file) -> model.addSubordinateMap(MapIOHelper.readMap(file)));
		final ILegacyMap map = model.getMap();
		treeModel = new WorkerTreeModelAlt(model);
		final IDRegistrar idf = IDFactoryFiller.createIDFactory(map);

		// TODO: replace lambda with (model::getMap).andThen(IMapNG::getCurrentTurn)?
		final WorkerTree tree = new WorkerTree(treeModel, model.getPlayers(),
				() -> model.getMap().getCurrentTurn(), false, idf);

		final WorkerCreationListener newWorkerListener = new WorkerCreationListener(treeModel, idf);

		tree.addUnitSelectionListener(newWorkerListener);

		final JobTreeModel jobsTreeModel = new JobTreeModel(model);
		tree.addUnitMemberListener(jobsTreeModel);

		final ItemAdditionPanel jobAdditionPanel = new ItemAdditionPanel("job");
		jobAdditionPanel.addAddRemoveListener(jobsTreeModel);

		final ItemAdditionPanel skillAdditionPanel = new ItemAdditionPanel("skill");
		skillAdditionPanel.addAddRemoveListener(jobsTreeModel);

		final LevelListener levelListener = new LevelListener(cli);

		tree.addUnitMemberListener(levelListener);

		final JobsTree jobsTreeObject = new JobsTree(jobsTreeModel);
		jobsTreeObject.addSkillSelectionListener(levelListener);

		final SkillAdvancementPanel hoursAdditionPanel = new SkillAdvancementPanel(model);
		tree.addUnitMemberListener(hoursAdditionPanel);
		jobsTreeObject.addSkillSelectionListener(hoursAdditionPanel);
		hoursAdditionPanel.addLevelGainListener(levelListener);

		final TreeExpansionOrderListener expander = new TreeExpansionHandler(tree);
		menuHandler.register(ignored -> expander.expandAll(), "expand all");
		menuHandler.register(ignored -> expander.collapseAll(), "collapse all");
		menuHandler.register(event -> expander.expandSome(2), "expand unit kinds");
		expander.expandAll();

		playerLabel = new FormattedLabel("%s's Units:", "An Unknown Player");
		setContentPane(horizontalSplit(BorderedPanel.verticalPanel(playerLabel,
						new JScrollPane(tree),
						new ListenedButton("Add worker to selected unit ...", newWorkerListener)),
				verticalSplit(BorderedPanel.verticalPanel(html("Worker's Jobs and Skills:"),
								new JScrollPane(jobsTreeObject), null),
						BorderedPanel.verticalPanel(null,
								BorderedPanel.verticalPanel(BorderedPanel.verticalPanel(
												html("Add a job to the worker:"), null,
												jobAdditionPanel), null,
										BorderedPanel.verticalPanel(
												html("Add a Skill to the selected Job:"),
												null, skillAdditionPanel)),
								hoursAdditionPanel), DIVIDER_LOCATION, RESIZE_WEIGHT)));

		playerChanged(null, model.getCurrentPlayer());
		setJMenuBar(workerMenu(menuHandler, this, driver));
		pack();
	}

	@Override
	public void playerChanged(final @Nullable Player old, final Player newPlayer) {
		playerLabel.setArguments(newPlayer.getName());
		treeModel.playerChanged(old, newPlayer);
	}

	@Override
	public String getWindowName() {
		return "Worker Advancement";
	}

	private static JLabel html(final String string) {
		return new JLabel("<html><p align=\"left\">%s</p></html>".formatted(string));
	}
}
