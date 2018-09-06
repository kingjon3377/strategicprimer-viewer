import java.awt {
    Dimension
}

import javax.swing {
    JPanel,
    JLabel,
    JTree,
    JScrollPane
}

import lovelace.util.jvm {
    listenedButton,
    BorderedPanel,
    verticalSplit,
    horizontalSplit,
    InterpolatedLabel
}

import strategicprimer.model.idreg {
    createIDFactory,
    IDRegistrar
}
import strategicprimer.model.map {
    Player,
    IMapNG
}
import strategicprimer.viewer.drivers {
    MenuBroker
}
import strategicprimer.viewer.drivers.worker_mgmt {
    workerMenu,
    TreeExpansionOrderListener,
    WorkerTreeModelAlt,
    TreeExpansionHandler,
    workerTree,
    UnitMemberSelectionSource,
    UnitSelectionSource
}
import strategicprimer.drivers.worker.common {
    IWorkerModel,
    IWorkerTreeModel
}
import strategicprimer.drivers.common {
    PlayerChangeListener
}
import strategicprimer.model.xmlio {
    mapIOHelper
}
import strategicprimer.drivers.gui.common {
    SPFrame
}
import lovelace.util.common {
	silentListener,
	defer
}
"A GUI to let a user manage workers."
SPFrame&PlayerChangeListener advancementFrame(IWorkerModel model,
        MenuBroker menuHandler) { // TODO: Try to convert/partially convert back to a class
    IMapNG map = model.map;
    IWorkerTreeModel treeModel = WorkerTreeModelAlt(model);
    IDRegistrar idf = createIDFactory(map);
	void markModified() {
		for (subMap->_ in model.allMaps) {
			model.setModifiedFlag(subMap, true);
		}
	}
    JTree&UnitMemberSelectionSource&UnitSelectionSource tree = workerTree(treeModel,
        model.players, defer(compose(IMapNG.currentTurn, IWorkerModel.map), [model]),
		false, idf, markModified);
    WorkerCreationListener newWorkerListener = WorkerCreationListener(treeModel,
        idf);
    tree.addUnitSelectionListener(newWorkerListener);
    object flaggingListener satisfies LevelGainListener&AddRemoveListener {
        void flag() {
            for (map->_ in model.allMaps) {
                model.setModifiedFlag(map, true);
            }
        }
        shared actual void add(String category, String addendum) => flag();
        shared actual void level() => flag();
    }
    JobTreeModel jobsTreeModel = JobTreeModel();
    tree.addUnitMemberListener(jobsTreeModel);
    JPanel&AddRemoveSource jobAdditionPanel = itemAdditionPanel("job");
    jobAdditionPanel.addAddRemoveListener(jobsTreeModel);
    jobAdditionPanel.addAddRemoveListener(flaggingListener);
    JPanel&AddRemoveSource skillAdditionPanel = itemAdditionPanel("skill");
    skillAdditionPanel.addAddRemoveListener(jobsTreeModel);
    skillAdditionPanel.addAddRemoveListener(flaggingListener);
    tree.addUnitMemberListener(levelListener);
    value jobsTreeObject = JobsTree(jobsTreeModel);
    jobsTreeObject.addSkillSelectionListener(levelListener);
    value hoursAdditionPanel = skillAdvancementPanel();
    jobsTreeObject.addSkillSelectionListener(hoursAdditionPanel);
    hoursAdditionPanel.addLevelGainListener(levelListener);
    hoursAdditionPanel.addLevelGainListener(flaggingListener);
    TreeExpansionOrderListener expander = TreeExpansionHandler(tree);
    menuHandler.register(silentListener(expander.expandAll), "expand all");
    menuHandler.register(silentListener(expander.collapseAll), "collapse all");
    menuHandler.register((event) => expander.expandSome(2), "expand unit kinds");
    expander.expandAll();
    InterpolatedLabel<[String]> playerLabel =
            InterpolatedLabel<[String]>(shuffle(curry("'s Units:".plus))(), [""]); // TODO: Take the player directly, using a player named "An Unknown Player" as the default
    object retval extends SPFrame("Worker Advancement", model.mapFile,
				Dimension(640, 480), true,
                (file) => model.addSubordinateMap(mapIOHelper.readMap(file), file))
            satisfies PlayerChangeListener {
        shared actual void playerChanged(Player? old, Player newPlayer) {
            playerLabel.arguments = [newPlayer.name];
            treeModel.playerChanged(old, newPlayer);
        }
        shared actual String windowName = "Worker Advancement";
    }
    JLabel html(String string) => JLabel("<html><p align=\"left\">``string``</p></html>");
    retval.contentPane = horizontalSplit(BorderedPanel.verticalPanel(playerLabel,
            JScrollPane(tree), listenedButton("Add worker to selected unit ...",
                newWorkerListener)),
        verticalSplit(BorderedPanel.verticalPanel(
            html("Worker's Jobs and Skills:"), JScrollPane(jobsTreeObject), null),
            BorderedPanel.verticalPanel(null,
                BorderedPanel.verticalPanel(
                    BorderedPanel.verticalPanel(html("Add a job to the worker:"), null,
                        jobAdditionPanel), null,
                    BorderedPanel.verticalPanel(html("Add a Skill to the selected Job:"),
                        null, skillAdditionPanel)), hoursAdditionPanel), 0.5, 0.3));
    retval.playerChanged(null, model.currentPlayer);
    retval.jMenuBar = workerMenu(menuHandler.actionPerformed, retval, model);
    retval.pack();
    return retval;
}
