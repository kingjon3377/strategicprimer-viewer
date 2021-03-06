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
    ListenedButton,
    BorderedPanel,
    verticalSplit,
    horizontalSplit,
    InterpolatedLabel
}

import strategicprimer.model.common.idreg {
    createIDFactory,
    IDRegistrar
}
import strategicprimer.model.common.map {
    IMapNG,
    Player,
    PlayerImpl
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
    IWorkerTreeModel
}
import strategicprimer.drivers.common {
    PlayerChangeListener,
    ModelDriver,
    IWorkerModel
}
import strategicprimer.model.impl.xmlio {
    mapIOHelper
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    MenuBroker
}
import lovelace.util.common {
    silentListener,
    defer,
    todo
}

"A GUI to let a user manage workers."
todo("Try to convert/partially convert back to a class")
SPFrame&PlayerChangeListener advancementFrame(IWorkerModel model,
        MenuBroker menuHandler, ModelDriver driver) {
    IMapNG map = model.map;
    IWorkerTreeModel treeModel = WorkerTreeModelAlt(model);
    IDRegistrar idf = createIDFactory(map);

    JTree&UnitMemberSelectionSource&UnitSelectionSource tree = workerTree(treeModel,
        model.players, defer(compose(IMapNG.currentTurn, IWorkerModel.map), [model]),
        false, idf);

    WorkerCreationListener newWorkerListener = WorkerCreationListener(treeModel,
        idf);

    tree.addUnitSelectionListener(newWorkerListener);

    JobTreeModel jobsTreeModel = JobTreeModel(model);
    tree.addUnitMemberListener(jobsTreeModel);

    JPanel&AddRemoveSource jobAdditionPanel = itemAdditionPanel("job");
    jobAdditionPanel.addAddRemoveListener(jobsTreeModel);

    JPanel&AddRemoveSource skillAdditionPanel = itemAdditionPanel("skill");
    skillAdditionPanel.addAddRemoveListener(jobsTreeModel);

    tree.addUnitMemberListener(levelListener);

    value jobsTreeObject = JobsTree(jobsTreeModel);
    jobsTreeObject.addSkillSelectionListener(levelListener);

    value hoursAdditionPanel = SkillAdvancementPanel(model);
    tree.addUnitMemberListener(hoursAdditionPanel);
    jobsTreeObject.addSkillSelectionListener(hoursAdditionPanel);
    hoursAdditionPanel.addLevelGainListener(levelListener);

    TreeExpansionOrderListener expander = TreeExpansionHandler(tree);
    menuHandler.register(silentListener(expander.expandAll), "expand all");
    menuHandler.register(silentListener(expander.collapseAll), "collapse all");
    menuHandler.register((event) => expander.expandSome(2), "expand unit kinds");
    expander.expandAll();

    InterpolatedLabel<[Player]> playerLabel =
            InterpolatedLabel<[Player]>(compose(shuffle(curry(plus<String>))("'s Units:"),
                Player.name), [PlayerImpl(-1, "An Unknown Player")]);

    object retval extends SPFrame("Worker Advancement", driver,
                Dimension(640, 480), true,
                (file) => model.addSubordinateMap(mapIOHelper.readMap(file))) // TODO: Convert lambda to method-reference using compose()
            satisfies PlayerChangeListener {
        shared actual void playerChanged(Player? old, Player newPlayer) {
            playerLabel.arguments = [newPlayer];
            treeModel.playerChanged(old, newPlayer);
        }

        shared actual String windowName = "Worker Advancement";
    }

    JLabel html(String string) => JLabel("<html><p align=\"left\">``string``</p></html>");
    retval.contentPane = horizontalSplit(BorderedPanel.verticalPanel(playerLabel,
            JScrollPane(tree), ListenedButton("Add worker to selected unit ...",
                newWorkerListener)),
        verticalSplit(BorderedPanel.verticalPanel(html("Worker's Jobs and Skills:"),
            JScrollPane(jobsTreeObject), null), BorderedPanel.verticalPanel(null,
                BorderedPanel.verticalPanel(BorderedPanel.verticalPanel(
                        html("Add a job to the worker:"), null, jobAdditionPanel), null,
                    BorderedPanel.verticalPanel(html("Add a Skill to the selected Job:"),
                        null, skillAdditionPanel)), hoursAdditionPanel), 0.5, 0.3));

    retval.playerChanged(null, model.currentPlayer);
    retval.jMenuBar = workerMenu(menuHandler.actionPerformed, retval, driver);
    retval.pack();
    return retval;
}
