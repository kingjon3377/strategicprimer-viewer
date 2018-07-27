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
    FormattedLabel,
    BorderedPanel,
    verticalSplit,
    horizontalSplit
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
        MenuBroker menuHandler) {
    IMapNG map = model.map;
    IWorkerTreeModel treeModel = WorkerTreeModelAlt(model);
    IDRegistrar idf = createIDFactory(map);
    JTree&UnitMemberSelectionSource&UnitSelectionSource tree = workerTree(treeModel,
        model.players, defer(compose(IMapNG.currentTurn, IWorkerModel.map), [model]),
		false, idf);
    WorkerCreationListener newWorkerListener = WorkerCreationListener(treeModel,
        idf);
    tree.addUnitSelectionListener(newWorkerListener);
    JobTreeModel jobsTreeModel = JobTreeModel();
    tree.addUnitMemberListener(jobsTreeModel);
    JPanel&AddRemoveSource jobAdditionPanel = itemAdditionPanel("job");
    jobAdditionPanel.addAddRemoveListener(jobsTreeModel);
    JPanel&AddRemoveSource skillAdditionPanel = itemAdditionPanel("skill");
    skillAdditionPanel.addAddRemoveListener(jobsTreeModel);
    tree.addUnitMemberListener(levelListener);
    value jobsTreeObject = JobsTree(jobsTreeModel);
    jobsTreeObject.addSkillSelectionListener(levelListener);
    value hoursAdditionPanel = skillAdvancementPanel();
    jobsTreeObject.addSkillSelectionListener(hoursAdditionPanel);
    hoursAdditionPanel.addLevelGainListener(levelListener);
    TreeExpansionOrderListener expander = TreeExpansionHandler(tree);
    menuHandler.register(silentListener(expander.expandAll), "expand all");
    menuHandler.register(silentListener(expander.collapseAll), "collapse all");
    menuHandler.register((event) => expander.expandSome(2), "expand unit kinds");
    expander.expandAll();
    FormattedLabel playerLabel = FormattedLabel("%s's Units:", "");
    object retval extends SPFrame("Worker Advancement", model.mapFile,
				Dimension(640, 480), true,
                (file) => model.addSubordinateMap(mapIOHelper.readMap(file), file))
            satisfies PlayerChangeListener {
        shared actual void playerChanged(Player? old, Player newPlayer) {
            playerLabel.setArgs(newPlayer.name);
            treeModel.playerChanged(old, newPlayer);
        }
        shared actual String windowName = "Worker Advancement";
    }
    JLabel html(String string) => JLabel("<html><p align=\"left\">``string``</p></html>");
    retval.contentPane = horizontalSplit(0.5, 0.5,
        BorderedPanel.verticalPanel(playerLabel,
            JScrollPane(tree), listenedButton("Add worker to selected unit ...",
                newWorkerListener)),
        verticalSplit(0.5, 0.3, BorderedPanel.verticalPanel(
            html("Worker's Jobs and Skills:"), JScrollPane(jobsTreeObject), null),
            BorderedPanel.verticalPanel(null,
                BorderedPanel.verticalPanel(
                    BorderedPanel.verticalPanel(html("Add a job to the worker:"), null,
                        jobAdditionPanel), null,
                    BorderedPanel.verticalPanel(html("Add a Skill to the selected Job:"),
                        null, skillAdditionPanel)), hoursAdditionPanel)));
    retval.playerChanged(null, model.currentPlayer);
    retval.jMenuBar = workerMenu(menuHandler.actionPerformed, retval, model);
    retval.pack();
    return retval;
}
