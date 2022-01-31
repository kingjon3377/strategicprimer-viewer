package drivers.advancement;

import org.jetbrains.annotations.Nullable;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.JScrollPane;

import lovelace.util.ListenedButton;
import lovelace.util.BorderedPanel;
import static lovelace.util.FunctionalSplitPane.verticalSplit;
import static lovelace.util.FunctionalSplitPane.horizontalSplit;
import lovelace.util.InterpolatedLabel;
import lovelace.util.FormattedLabel;

import common.idreg.IDFactoryFiller;
import common.idreg.IDRegistrar;

import common.map.IMapNG;
import common.map.Player;
import common.map.PlayerImpl;

import static drivers.worker_mgmt.WorkerMenu.workerMenu;
import drivers.worker_mgmt.TreeExpansionOrderListener;
import drivers.worker_mgmt.WorkerTreeModelAlt;
import drivers.worker_mgmt.TreeExpansionHandler;
import drivers.worker_mgmt.WorkerTree;
import drivers.worker_mgmt.UnitMemberSelectionSource;
import drivers.worker_mgmt.UnitSelectionSource;

import worker.common.IWorkerTreeModel;

import drivers.common.PlayerChangeListener;
import drivers.common.ModelDriver;
import drivers.common.IWorkerModel;

import impl.xmlio.MapIOHelper;

import drivers.gui.common.SPFrame;
import drivers.gui.common.MenuBroker;

/**
 * A GUI to let a user manage workers.
 */
/* package */ class AdvancementFrame extends SPFrame implements PlayerChangeListener {
	private final FormattedLabel playerLabel;
	private final IWorkerTreeModel treeModel;
	public AdvancementFrame(IWorkerModel model, MenuBroker menuHandler, ModelDriver driver) {
		super("Worker Advancement", driver, new Dimension(640, 480), true,
			(file) -> model.addSubordinateMap(MapIOHelper.readMap(file)));
		IMapNG map = model.getMap();
		treeModel = new WorkerTreeModelAlt(model);
		IDRegistrar idf = new IDFactoryFiller().createIDFactory(map);

		WorkerTree tree = new WorkerTree(treeModel, model.getPlayers(),
			() -> model.getMap().getCurrentTurn(), false, idf);

		WorkerCreationListener newWorkerListener = new WorkerCreationListener(treeModel, idf);

		tree.addUnitSelectionListener(newWorkerListener);

		JobTreeModel jobsTreeModel = new JobTreeModel(model);
		tree.addUnitMemberListener(jobsTreeModel);

		ItemAdditionPanel jobAdditionPanel = new ItemAdditionPanel("job");
		jobAdditionPanel.addAddRemoveListener(jobsTreeModel);

		ItemAdditionPanel skillAdditionPanel = new ItemAdditionPanel("skill");
		skillAdditionPanel.addAddRemoveListener(jobsTreeModel);

		LevelListener levelListener = new LevelListener();

		tree.addUnitMemberListener(levelListener);

		JobsTree jobsTreeObject = new JobsTree(jobsTreeModel);
		jobsTreeObject.addSkillSelectionListener(levelListener);

		SkillAdvancementPanel hoursAdditionPanel = new SkillAdvancementPanel(model);
		tree.addUnitMemberListener(hoursAdditionPanel);
		jobsTreeObject.addSkillSelectionListener(hoursAdditionPanel);
		hoursAdditionPanel.addLevelGainListener(levelListener);

		TreeExpansionOrderListener expander = new TreeExpansionHandler(tree);
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
					hoursAdditionPanel), 0.5, 0.3)));

		playerChanged(null, model.getCurrentPlayer());
		setJMenuBar(workerMenu(menuHandler::actionPerformed, this, driver));
		pack();
	}

	@Override
	public void playerChanged(@Nullable Player old, Player newPlayer) {
		playerLabel.setArguments(newPlayer.getName());
		treeModel.playerChanged(old, newPlayer);
	}

	@Override
	public String getWindowName() {
		return "Worker Advancement";
	}

	private static JLabel html(String string) {
		return new JLabel(String.format("<html><p align=\"left\">%s</p></html>", string));
	}
}
