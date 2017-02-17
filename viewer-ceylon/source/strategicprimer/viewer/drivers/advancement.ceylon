import controller.map.drivers {
    DriverFailedException
}
import controller.map.misc {
    ICLIHelper,
    MenuBroker,
    PlayerChangeMenuListener,
    WindowCloser,
    IDFactoryFiller
}
import model.misc {
    IDriverModel
}
import model.workermgmt {
    WorkerModel,
    IWorkerModel,
    WorkerTreeModelAlt,
    IWorkerTreeModel,
    JobTreeModel
}
import java.io {
    IOException
}
import model.map {
    Player,
    IMapNG
}
import java.util {
    JList = List
}
import model.map.fixtures.mobile {
    IUnit,
    IWorker
}
import ceylon.interop.java {
    JavaList,
    CeylonCollection
}
import ceylon.collection {
    ArrayList,
    HashMap,
    MutableMap
}
import model.map.fixtures.mobile.worker {
    ProxyWorker,
    IJob,
    Job,
    ISkill,
    Skill,
    ProxyJob
}
import util {
    SingletonRandom { singletonRandom = random }
}
import javax.swing {
    SwingUtilities,
    JLabel,
    JScrollPane
}
import view.worker {
    WorkerTree,
    WorkerCreationListener,
    LevelListener,
    JobsTree,
    SkillAdvancementPanel,
    TreeExpansionHandler,
    WorkerMenu
}
import view.util {
    DriverQuit,
    ItemAdditionPanel,
    SPFrame,
    BorderedPanel,
    SplitWithWeights,
    ListenedButton,
    FormattedLabel,
    TreeExpansionOrderListener
}
import strategicprimer.viewer.about {
    aboutDialog
}
import model.listeners {
    PlayerChangeListener
}
import java.awt {
    Dimension
}
"Let the user add hours to a Skill or Skills in a Job."
void advanceJob(IJob job, ICLIHelper cli) {
    JList<ISkill> skills = JavaList(ArrayList(0, 1.0, { *job }));
    cli.loopOnMutableList(skills, (clh) => clh.chooseFromList(skills, "Skills in Job:",
            "No existing Skills.", "Skill to advance: ", false),
        "Select another Skill in this Job? ",
        (JList<ISkill> list, ICLIHelper clh) {
            String skillName = clh.inputString("Name of new Skill: ");
            job.addSkill(Skill(skillName, 0, 0));
            list.clear();
            for (skill in job) {
                list.add(skill);
            }
            return list.stream().filter((item) => skillName == item.name).findAny();
        }, (ISkill skill, clh) {
            Integer oldLevel = skill.level;
            skill.addHours(clh.inputNumber("Hours of experience to add: "),
                singletonRandom.nextInt(100));
            if (skill.level == oldLevel) {
                clh.print("Worker(s) gained a level in ``skill.name``");
            }
        });
}
"Let the user add experience to a worker."
void advanceSingleWorker(IWorker worker, ICLIHelper cli) {
    JList<IJob> jobs = JavaList(ArrayList(0, 1.0, { *worker }));
    cli.loopOnMutableList(jobs, (clh) => clh.chooseFromList(jobs, "Jobs in worker:",
            "No existing Jobs.", "Job to advance: ", false),
        "Select another Job in this worker? ",
        (JList<IJob> list, clh) {
            String jobName = clh.inputString("Name of new Job: ");
            worker.addJob(Job(jobName, 0));
            list.clear();
            for (job in worker) {
                list.add(job);
            }
            return list.stream().filter((item) => jobName == item.name).findAny();
        }, advanceJob);
}
"Ensure that there is a Job by the given name in each worker, and return a collection of
 those Jobs."
{IJob*} getWorkerJobs(String jobName, IWorker* workers) {
    MutableMap<String, IJob> jobs = HashMap<String, IJob>();
    for (worker in workers) {
        if (exists job = worker.getJob(jobName)) {
            jobs.put(worker.name, job);
        } else {
            IJob temp = Job(jobName, 0);
            worker.addJob(temp);
            jobs.put(worker.name, temp);
        }
    }
    return jobs.items;
}
"Let the user add experience in a single Skill to all of a list of workers."
void advanceWorkersInSkill(String jobName, String skillName, ICLIHelper cli,
        IWorker* workers) {
    Integer hours = cli.inputNumber("Hours of experience to add: ");
    for (worker in workers) {
        IJob job;
        if (exists tempJob = worker.getJob(jobName)) {
            job = tempJob;
        } else {
            worker.addJob(Job(jobName, 0));
            assert (exists secondTempJob = worker.getJob(jobName));
            job = secondTempJob;
        }
        ISkill skill;
        if (exists tempSkill = job.getSkill(skillName)) {
            skill = tempSkill;
        } else {
            job.addSkill(Skill(skillName, 0, 0));
            assert (exists secondTempSkill = job.getSkill(skillName));
            skill = secondTempSkill;
        }
        Integer oldLevel = skill.level;
        skill.addHours(hours, singletonRandom.nextInt(100));
        if (skill.level != oldLevel) {
            cli.println("``worker.name`` gained a level in ``skill.name``");
        }
    }
}
"Let the user add experience in a given Job to all of a list of workers."
void advanceWorkersInJob(String jobName, ICLIHelper cli, IWorker* workers) {
    {IJob*} jobs = getWorkerJobs(jobName, *workers);
    JList<ISkill> skills = JavaList(ArrayList(0, 1.0, { for (skill in ProxyJob(jobName, false, *workers)) skill }));
    cli.loopOnMutableList(skills, (clh) => clh.chooseFromList(skills, "Skills in Jobs:",
            "No existing skills.", "Skill to advance: ", false),
        "Select another Skill in this Job? ",
        (JList<ISkill> list, clh) {
            String skillName = clh.inputString("Name of new Skill: ");
            for (job in jobs) {
                job.addSkill(Skill(skillName, 0, 0));
            }
            skills.clear();
            for (skill in ProxyJob(jobName, false, *workers)) {
                skills.add(skill);
            }
            return skills.stream().filter((item) => skillName == item.name).findAny();
        }, (ISkill skill, clh) => advanceWorkersInSkill(jobName, skill.name, clh, *workers));
}
"Let the user add experience to a worker or workers in a unit."
void advanceWorkersInUnit(IUnit unit, ICLIHelper cli) {
    JList<IWorker> workers = JavaList(ArrayList(0, 1.0,
        {for (member in unit) if (is IWorker member) member}));
    if (cli.inputBoolean("Add experience to workers individually? ")) {
        cli.loopOnList(workers, (clh) => clh.chooseFromList(workers, "Workers in unit:",
                "No unadvanced workers remain.", "Chosen worker: ", false),
            "Choose another worker? ", advanceSingleWorker);
    } else if (workers.empty) {
        cli.println("No workers in unit.");
    } else {
        JList<IJob> jobs = JavaList(ArrayList(0, 1.0, { *ProxyWorker(unit) }));
        cli.loopOnMutableList(jobs, (ICLIHelper clh) => clh.chooseFromList(jobs, "Jobs in workers:",
                "No existing jobs.", "Job to advance: ", false),
            "Select another Job in these workers? ",
            (JList<IJob> list, ICLIHelper clh) {
                String jobName = clh.inputString("Name of new Job: ");
                for (worker in workers) {
                    worker.addJob(Job(jobName, 0));
                }
                list.clear();
                for (job in ProxyWorker(unit)) {
                    list.add(job);
                }
                return list.stream().filter((item) => jobName == item.name).findAny();
            }, (IJob job, clh) => advanceWorkersInJob(job.name, clh, *CeylonCollection(workers)));
    }
}
"Let the user add experience to a player's workers."
void advanceWorkers(IWorkerModel model, Player player, ICLIHelper cli) {
    JList<IUnit> units = model.getUnits(player);
    units.removeIf((unit) => !unit.iterator().hasNext());
    cli.loopOnList(units, (clh) => clh.chooseFromList(units, "``player.name``'s units:",
            "No unadvanced units remain.", "Chosen unit: ", false),
        "Choose another unit? ", advanceWorkersInUnit);
}
"The worker-advancement CLI driver."
object advancementCLI satisfies SimpleCLIDriver {
    "Let the user choose a player to run worker advancement for."
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        IWorkerModel workerModel;
        if (is IWorkerModel model) {
            workerModel = model;
        } else {
            workerModel = WorkerModel(model);
        }
        JList<Player> playerList = workerModel.players;
        try {
            cli.loopOnList(playerList,
                (clh) => clh.chooseFromList(playerList, "Available players:",
                    "No players found.", "Chosen player: ", false),
                "Select another player? ",
                (Player player, clh) => advanceWorkers(workerModel, player, clh));
        } catch (IOException except) {
            throw DriverFailedException("I/O error interacting with user", except);
        }
    }
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-a";
        longOption = "--adv";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "View a player's workers and manage their advancement";
        longDescription = """View a player's units, the workers in those units, each
                             worker's Jobs, and his or her level in each Skill in each Job.""";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
}
"A GUI to let a user manage workers."
SPFrame&PlayerChangeListener advancementFrame(IWorkerModel model, MenuBroker menuHandler) {
    IMapNG map = model.map;
    IWorkerTreeModel treeModel = WorkerTreeModelAlt(map.currentPlayer, model);
    WorkerTree tree = WorkerTree.factory(treeModel, map.players().iterator,
        () => model.map.currentTurn, false);
    WorkerCreationListener newWorkerListener = WorkerCreationListener(treeModel,
        IDFactoryFiller.createFactory(map));
    tree.addUnitSelectionListener(newWorkerListener);
    JobTreeModel jobsTreeModel = JobTreeModel();
    tree.addUnitMemberListener(jobsTreeModel);
    ItemAdditionPanel jobAdditionPanel = ItemAdditionPanel("job");
    jobAdditionPanel.addAddRemoveListener(jobsTreeModel);
    ItemAdditionPanel skillAdditionPanel = ItemAdditionPanel("skill");
    skillAdditionPanel.addAddRemoveListener(jobsTreeModel);
    LevelListener levelListener = LevelListener();
    JobsTree jobsTree = JobsTree(jobsTreeModel);
    jobsTree.addSkillSelectionListener(levelListener);
    SkillAdvancementPanel skillAdvancementPanel = SkillAdvancementPanel();
    jobsTree.addSkillSelectionListener(skillAdvancementPanel);
    skillAdvancementPanel.addLevelGainListener(levelListener);
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
    }
    retval.contentPane = SplitWithWeights.horizontalSplit(0.5, 0.5,
        BorderedPanel.verticalPanel(playerLabel,
            JScrollPane(tree), ListenedButton("Add worker to selected unit ...",
                newWorkerListener)),
        SplitWithWeights.verticalSplit(0.5, 0.3,
            BorderedPanel.verticalPanel(
                JLabel("<html><p align=\"left\">Worker's Jobs and Skills:</p></html>"),
                JScrollPane(jobsTree), null),
            BorderedPanel.verticalPanel(null,
                BorderedPanel.verticalPanel(
                    BorderedPanel.verticalPanel(
                        JLabel("<html><p align=\"left\">Add a job to the worker:</p></html>"), null,
                        jobAdditionPanel), null,
                    BorderedPanel.verticalPanel(
                        JLabel("<html><p align=\"left\">Add a Skill to the selected Job:</p></html>"),
                        null, skillAdditionPanel)), skillAdvancementPanel)));
    retval.playerChanged(null, map.currentPlayer);
    retval.jMenuBar = WorkerMenu(menuHandler, retval, model);
    retval.pack();
    return retval;
}
"The worker-advancement GUI driver."
object advancementGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        shortOption = "-a";
        longOption = "--adv";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "View a player's workers and manage their advancement";
        longDescription = """View a player's units, the workers in those units, each
                             worker's Jobs, and his or her level in each Skill in each Job.""";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        IWorkerModel workerModel;
        if (is IWorkerModel model) {
            workerModel = model;
        } else {
            workerModel = WorkerModel(model);
        }
        MenuBroker menuHandler = MenuBroker();
        menuHandler.register(IOHandler(workerModel, options, cli), "load", "save",
            "save as", "new", "load secondary", "save all", "open in map viewer",
            "open secondary map in map viewer");
        PlayerChangeMenuListener pcml = PlayerChangeMenuListener(workerModel);
        menuHandler.register(pcml, "change current player");
        menuHandler.register((event) => DriverQuit.quit(0), "quit");
        SwingUtilities.invokeLater(() {
            SPFrame&PlayerChangeListener frame = advancementFrame(workerModel, menuHandler);
            pcml.addPlayerChangeListener(frame);
            menuHandler.register((event) =>
                    frame.playerChanged(model.map.currentPlayer, model.map.currentPlayer),
                "reload tree");
            menuHandler.register(WindowCloser(frame), "close");
            menuHandler.register((event) =>
                    aboutDialog(frame, frame.windowName).setVisible(true), "about");
            frame.setVisible(true);
        });
    }
}