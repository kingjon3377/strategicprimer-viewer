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
    createIDFactory
}
import strategicprimer.model.map {
    Player,
    IMapNG
}
import strategicprimer.viewer.drivers {
    SPFrame,
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
import java.nio.file {
    JPath=Path
}
import strategicprimer.model.xmlio {
    readMap
}
"A GUI to let a user manage workers."
SPFrame&PlayerChangeListener advancementFrame(IWorkerModel model,
        MenuBroker menuHandler) {
    IMapNG map = model.map;
    IWorkerTreeModel treeModel = WorkerTreeModelAlt(map.currentPlayer, model);
    JTree&UnitMemberSelectionSource&UnitSelectionSource tree = workerTree(treeModel,
        map.players, () => model.map.currentTurn, false);
    WorkerCreationListener newWorkerListener = WorkerCreationListener(treeModel,
        createIDFactory(map));
    tree.addUnitSelectionListener(newWorkerListener);
    JobTreeModel jobsTreeModel = JobTreeModel();
    tree.addUnitMemberListener(jobsTreeModel);
    JPanel&AddRemoveSource jobAdditionPanel = itemAdditionPanel("job");
    jobAdditionPanel.addAddRemoveListener(jobsTreeModel);
    JPanel&AddRemoveSource skillAdditionPanel = itemAdditionPanel("skill");
    skillAdditionPanel.addAddRemoveListener(jobsTreeModel);
    tree.addUnitMemberListener(levelListener);
    value jobsTreeObject = jobsTree(jobsTreeModel);
    jobsTreeObject.addSkillSelectionListener(levelListener);
    value hoursAdditionPanel = skillAdvancementPanel();
    jobsTreeObject.addSkillSelectionListener(hoursAdditionPanel);
    hoursAdditionPanel.addLevelGainListener(levelListener);
    TreeExpansionOrderListener expander = TreeExpansionHandler(tree);
    menuHandler.register((event) => expander.expandAll(), "expand all");
    menuHandler.register((event) => expander.collapseAll(), "collapse all");
    menuHandler.register((event) => expander.expandSome(2), "expand unit kinds");
    expander.expandAll();
    FormattedLabel playerLabel = FormattedLabel("%s's Units:", "");
    object retval
            extends SPFrame("Worker Advancement", model.mapFile, Dimension(640, 480))
            satisfies PlayerChangeListener{
        shared actual void playerChanged(Player? old, Player newPlayer) {
            playerLabel.setArgs(newPlayer.name);
            treeModel.playerChanged(old, newPlayer);
        }
        shared actual String windowName = "Worker Advancement";
        shared actual void acceptDroppedFile(JPath file) =>
                model.addSubordinateMap(readMap(file), file);
        shared actual Boolean supportsDroppedFiles = true;
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
    retval.playerChanged(null, map.currentPlayer);
    retval.jMenuBar = workerMenu(menuHandler.actionPerformed, retval, model);
    retval.pack();
    return retval;
}
