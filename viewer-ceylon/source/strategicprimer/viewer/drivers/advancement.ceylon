import controller.map.misc {
    ICLIHelper,
    WindowCloser,
    IDFactoryFiller,
    IDRegistrar
}
import model.misc {
    IDriverModel
}
import model.workermgmt {
    WorkerModel,
    IWorkerModel,
    WorkerTreeModelAlt,
    IWorkerTreeModel,
    JobTreeModel,
    RaceFactory
}
import java.io {
    IOException
}
import model.map {
    Player,
    IMapNG,
    HasName
}
import java.util {
    JList = List
}
import model.map.fixtures.mobile {
    IUnit,
    IWorker,
    Worker
}
import ceylon.interop.java {
    JavaList,
    CeylonCollection,
    CeylonIterable
}
import ceylon.collection {
    ArrayList,
    HashMap,
    MutableMap,
    MutableList
}
import model.map.fixtures.mobile.worker {
    ProxyWorker,
    IJob,
    Job,
    ISkill,
    Skill,
    ProxyJob,
    WorkerStats
}
import util {
    SingletonRandom { singletonRandom = random },
    OnMac
}
import javax.swing {
    SwingUtilities,
    JLabel,
    JScrollPane,
    JTextField,
    JPanel,
    JFrame,
    WindowConstants,
    JComponent,
    JTree,
    JButton,
    BoxLayout
}
import strategicprimer.viewer.about {
    aboutDialog
}
import model.listeners {
    PlayerChangeListener,
    LevelGainSource,
    SkillSelectionListener,
    LevelGainListener,
    UnitSelectionListener,
    NewWorkerListener,
    SkillSelectionSource,
    AddRemoveListener,
    UnitMemberSelectionSource,
    UnitSelectionSource,
    UnitMemberListener
}
import java.awt {
    Dimension,
    GridLayout,
    CardLayout,
    FlowLayout,
    Container
}
import java.awt.event {
    ActionEvent,
    ActionListener
}
import javax.swing.event {
    TreeModelEvent,
    TreeModelListener
}
import lovelace.util.common {
    todo
}
import lovelace.util.jvm {
    listenedButton,
    showErrorDialog,
    centeredHorizontalBox,
    BoxAxis,
    boxPanel,
    BorderedPanel,
    verticalSplit,
    horizontalSplit,
    FormattedLabel
}
import model.map.fixtures {
    UnitMember
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
            throw DriverFailedException(except, "I/O error interacting with user");
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
"A panel to let a user add hours of experience to a Skill."
JPanel&SkillSelectionListener&LevelGainSource skillAdvancementPanel() {
    JTextField hours = JTextField(3);
    JPanel firstPanel = JPanel(FlowLayout());
    firstPanel.add(JLabel("Add "));
    firstPanel.add(hours);
    firstPanel.add(JLabel(" hours to skill?"));
    variable ISkill? skill = null;
    MutableList<LevelGainListener> listeners = ArrayList<LevelGainListener>();
    Anything(ActionEvent) okListener = (ActionEvent event) {
        if (exists local = skill) {
            Integer level = local.level;
            if (is Integer number = Integer.parse(hours.text)) {
                local.addHours(number, singletonRandom.nextInt(100));
            } else {
                showErrorDialog(hours, "Strategic Primer Worker Advancement",
                    "Hours to add must be a number");
                return;
            }
            Integer newLevel = local.level;
            if (newLevel != level) {
                for (listener in listeners) {
                    listener.level();
                }
            }
        }
        // Clear if OK and no skill selected, on Cancel, and after successfully adding skill
        hours.text = "";
    };
    JButton okButton = listenedButton("OK", okListener);
    hours.setActionCommand("OK");
    hours.addActionListener(okListener);
    JButton cancelButton = listenedButton("Cancel",
        (ActionEvent event) => hours.text = "");
    OnMac.makeButtonsSegmented(okButton, cancelButton);
    JPanel secondPanel;
    if (OnMac.systemIsMac) {
        secondPanel = centeredHorizontalBox(okButton, cancelButton);
    } else {
        secondPanel = JPanel(FlowLayout());
        secondPanel.add(okButton);
        secondPanel.add(cancelButton);
    }
    object retval extends JPanel()
            satisfies SkillSelectionListener&LevelGainSource {
        shared actual void selectSkill(ISkill? selectedSkill) {
            skill = selectedSkill;
            if (selectedSkill exists) {
                hours.requestFocusInWindow();
            }
        }
        shared actual void addLevelGainListener(LevelGainListener listener)
                => listeners.add(listener);
        shared actual void removeLevelGainListener(LevelGainListener listener)
                => listeners.remove(listener);
    }
    (retval of Container).layout = BoxLayout(retval, BoxAxis.pageAxis.axis);
    retval.add(firstPanel);
    retval.add(secondPanel);
    retval.minimumSize = Dimension(200, 40);
    retval.preferredSize = Dimension(220, 60);
    retval.maximumSize = Dimension(240, 60);
    return retval;
}
"A listener to keep track of the currently selected unit and listen for new-worker
 notifications, then pass this information on to the tree model."
class WorkerCreationListener(IWorkerTreeModel model, IDRegistrar factory)
        satisfies ActionListener&UnitSelectionListener&NewWorkerListener {
    "The currently selected unit"
    variable IUnit? selectedUnit = null;
    shared actual void addNewWorker(Worker worker) {
        if (exists local = selectedUnit) {
            model.addUnitMember(local, worker);
        } else {
            log.warn("New worker created when no unit selected");
            showErrorDialog(null, "Strategic Primer Worker Advancement",
                "As no unit was selected, the new worker wasn't added to a unit.");
        }
    }
    shared actual void actionPerformed(ActionEvent event) {
        if (event.actionCommand.lowercased.startsWith("add worker")) {
            object frame extends JFrame("Create Worker") {
                defaultCloseOperation = WindowConstants.disposeOnClose;
                JTextField name = JTextField();
                JTextField race = JTextField(RaceFactory.race);
                JTextField hpBox = JTextField();
                JTextField maxHP = JTextField();
                JTextField strength = JTextField();
                JTextField dexterity = JTextField();
                JTextField constitution = JTextField();
                JTextField intelligence = JTextField();
                JTextField wisdom = JTextField();
                JTextField charisma = JTextField();
                JPanel textPanel = JPanel(GridLayout(0, 2));
                void addLabeledField(JPanel panel, String text, JComponent field) {
                    panel.add(JLabel(text));
                    panel.add(field);
                }
                addLabeledField(textPanel, "Worker Name:", name);
                addLabeledField(textPanel, "Worker Race", race);
                JPanel buttonPanel = JPanel(GridLayout(0, 2));
                JButton addButton = listenedButton("Add Worker", (ActionEvent event) {
                    String nameText = name.text.trimmed;
                    String raceText = race.text.trimmed;
                    value hpValue = Integer.parse(hpBox.text.trimmed);
                    value maxHPValue = Integer.parse(maxHP.text.trimmed);
                    value strValue = Integer.parse(strength.text.trimmed);
                    value dexValue = Integer.parse(dexterity.text.trimmed);
                    value conValue = Integer.parse(constitution.text.trimmed);
                    value intValue = Integer.parse(intelligence.text.trimmed);
                    value wisValue = Integer.parse(wisdom.text.trimmed);
                    value chaValue = Integer.parse(charisma.text.trimmed);
                    if (!nameText.empty, raceText.empty, is Integer hpValue,
                            is Integer maxHPValue, is Integer strValue,
                            is Integer dexValue, is Integer conValue,
                            is Integer intValue, is Integer wisValue,
                            is Integer chaValue) {
                        Worker retval = Worker(nameText, raceText, factory.createID());
                        retval.stats = WorkerStats(hpValue, maxHPValue, strValue,
                            dexValue, conValue, intValue, wisValue, chaValue);
                        addNewWorker(retval);
                        setVisible(false);
                        dispose();
                    } else {
                        StringBuilder builder = StringBuilder();
                        if (nameText.empty) {
                            builder.append("Worker needs a name.");
                            builder.appendNewline();
                        }
                        if (raceText.empty) {
                            builder.append("Worker needs a race.");
                            builder.appendNewline();
                        }
                        for ([stat, val] in {["HP", hpValue],
                                ["Max HP", maxHPValue], ["Strength", strValue],
                                ["Dexterity", dexValue], ["Constitution", conValue],
                                ["Intelligence", intValue], ["Wisdom", wisValue],
                                ["Charisma", chaValue]}) {
                            if (is ParseException val) {
                                builder.append("``stat`` must be a number.");
                                builder.appendNewline();
                            }
                        }
                        showErrorDialog(null, "Strategic Primer Worker Advancement",
                            builder.string);
                    }
                });
                buttonPanel.add(addButton);
                JButton cancelButton = listenedButton("Cancel",
                    (ActionEvent event) => dispose());
                buttonPanel.add(cancelButton);
                OnMac.makeButtonsSegmented(addButton, cancelButton);
                JPanel statsPanel = JPanel(GridLayout(0, 4));
                hpBox.text = "8";
                addLabeledField(statsPanel, "HP:", hpBox);
                maxHP.text = "8";
                addLabeledField(statsPanel, "Max HP:", maxHP);
                for ([stat, box] in {["Strength:", strength],
                        ["Intelligence:", intelligence], ["Dexterity:", dexterity],
                        ["Wisdom:", wisdom], ["Constitution:", constitution],
                        ["Charisma:", charisma]}) {
                    box.text = (singletonRandom.nextInt(6) + singletonRandom.nextInt(6) +
                        singletonRandom.nextInt(6) + 3).string;
                    addLabeledField(statsPanel, stat, box);
                }
                contentPane = BorderedPanel.verticalPanel(textPanel, statsPanel, buttonPanel);
                setMinimumSize(Dimension(320, 240));
                pack();
            }
            frame.setVisible(true);
        }
    }
    "Update our currently-selected-unit reference."
    shared actual void selectUnit(IUnit? unit) {
        selectedUnit = unit;
    }
}
"A tree representing a worker's Jobs and Skills."
JTree&SkillSelectionSource jobsTree(JobTreeModel jtModel) {
    object retval extends JTree(jtModel) satisfies SkillSelectionSource {
        MutableList<SkillSelectionListener> listeners =
                ArrayList<SkillSelectionListener>();
        shared actual void addSkillSelectionListener(SkillSelectionListener listener) =>
            listeners.add(listener);
        shared actual void removeSkillSelectionListener(SkillSelectionListener listener) =>
            listeners.remove(listener);
        jtModel.setSelectionModel(selectionModel);
        rootVisible = false;
        variable Integer i = 0;
        while (i < rowCount) {
            expandRow(i);
        }
        showsRootHandles = true;
        selectionModel.addTreeSelectionListener((event) {
            ISkill? retval;
            if (exists selectionPath = event.newLeadSelectionPath,
                    is ISkill component = selectionPath.lastPathComponent) {
                retval = component;
            } else {
                retval = null;
            }
            for (listener in listeners) {
                listener.selectSkill(retval);
            }
        });
    }
    object treeModelListener satisfies TreeModelListener {
        shared actual void treeStructureChanged(TreeModelEvent event) {
            retval.expandPath(event.treePath.parentPath);
            variable Integer i = 0;
            while (i < retval.rowCount) {
                retval.expandRow(i);
                i++;
            }
        }
        shared actual void treeNodesRemoved(TreeModelEvent event) { }
        shared actual void treeNodesInserted(TreeModelEvent event) {
            retval.expandPath(event.treePath);
            retval.expandPath(event.treePath.parentPath);
        }
        shared actual void treeNodesChanged(TreeModelEvent event) =>
                retval.expandPath(event.treePath.parentPath);
    }
    jtModel.addTreeModelListener(treeModelListener);
    return retval;
}
"An interface for UIs (etc.) for adding and removing items in lists."
todo("Combine with other 'source' interfaces now we have reified generics & union types")
interface AddRemoveSource {
    "Add a listener."
    shared formal void addAddRemoveListener(AddRemoveListener listener);
    "Remove a listener."
    shared formal void removeAddRemoveListener(AddRemoveListener listener);
}
"A panel to be the GUI to add items to a list."
todo("Move to lovelace.util?")
JPanel&AddRemoveSource itemAdditionPanel("What we're adding" String what) {
    CardLayout layoutObj = CardLayout();
    MutableList<AddRemoveListener> listeners = ArrayList<AddRemoveListener>();
    JTextField field = JTextField(10);
    object retval extends JPanel(layoutObj) satisfies AddRemoveSource {
        shared actual void addAddRemoveListener(AddRemoveListener listener) =>
                listeners.add(listener);
        shared actual void removeAddRemoveListener(AddRemoveListener listener) =>
                listeners.remove(listener);
    }
    void setPanelSizes(JPanel panel) {
        panel.minimumSize = Dimension(60, 40);
        panel.preferredSize = Dimension(80, 50);
        panel.maximumSize = Dimension(90, 50);
    }
    setPanelSizes(retval);
    JPanel first = boxPanel(BoxAxis.lineAxis);
    first.add(listenedButton("+"), (ActionEvent event) {
        layoutObj.next(retval);
        field.requestFocusInWindow();
    });
    setPanelSizes(first);
    retval.add(first);
    JPanel second = boxPanel(BoxAxis.pageAxis);
    second.add(field);
    void okListener(ActionEvent event) {
        String text = field.text;
        for (listener in listeners) {
            listener.add(what, text);
        }
        layoutObj.first(retval);
        field.text = "";
    }
    field.addActionListener(okListener);
    field.setActionCommand("OK");
    JPanel okPanel = boxPanel(BoxAxis.lineAxis);
    JButton okButton = listenedButton("OK", okListener);
    okPanel.add(okButton);
    JButton cancelButton = listenedButton("Cancel", (ActionEvent event) {
        layoutObj.first(retval);
        field.text = "";
    });
    OnMac.makeButtonsSegmented(okButton, cancelButton);
    okPanel.add(cancelButton);
    second.add(okPanel);
    setPanelSizes(second);
    retval.add(second);
    return retval;
}
"A listener to print a line whenever a worker gains a level."
object levelListener satisfies LevelGainListener&UnitMemberListener&SkillSelectionListener {
    "The current worker."
    variable UnitMember? worker = null;
    "The current skill."
    variable ISkill? skill = null;
    shared actual void selectSkill(ISkill? selectedSkill) => skill = selectedSkill;
    shared actual void memberSelected(UnitMember? old, UnitMember? selected) =>
            worker = selected;
    "Wrapper around [[HasName]].getName() that also handles non-HasName objects using their
     `string` attribute."
    String getName(Object named) {
        if (is HasName named) {
            return named.name;
        } else {
            return  named.string;
        }
    }
    "Notify the user of a gained level."
    shared actual void level() {
        if (exists localWorker = worker, exists localSkill = skill) {
            process.writeLine("``getName(localWorker)`` gained a level in ``getName(
                localSkill)``");
        }
    }
}
"A GUI to let a user manage workers."
SPFrame&PlayerChangeListener advancementFrame(IWorkerModel model, MenuBroker menuHandler) {
    IMapNG map = model.map;
    IWorkerTreeModel treeModel = WorkerTreeModelAlt(map.currentPlayer, model);
    JTree&UnitMemberSelectionSource&UnitSelectionSource tree = workerTree(treeModel,
        CeylonIterable(map.players()), () => model.map.currentTurn, false);
    WorkerCreationListener newWorkerListener = WorkerCreationListener(treeModel,
        IDFactoryFiller.createFactory(map));
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
            extends SPFrame("Worker Advancement", model.mapFile.orElse(null),
                Dimension(640, 480))
            satisfies PlayerChangeListener{
        shared actual void playerChanged(Player? old, Player newPlayer) {
            playerLabel.setArgs(newPlayer.name);
            treeModel.playerChanged(old, newPlayer);
        }
        shared actual String windowName = "Worker Advancement";
    }
    retval.contentPane = horizontalSplit(0.5, 0.5,
        BorderedPanel.verticalPanel(playerLabel,
            JScrollPane(tree), listenedButton("Add worker to selected unit ...",
                newWorkerListener)),
        verticalSplit(0.5, 0.3, BorderedPanel.verticalPanel(
                JLabel("<html><p align=\"left\">Worker's Jobs and Skills:</p></html>"),
                JScrollPane(jobsTreeObject), null),
            BorderedPanel.verticalPanel(null,
                BorderedPanel.verticalPanel(
                    BorderedPanel.verticalPanel(
                        JLabel("<html><p align=\"left\">Add a job to the worker:</p></html>"), null,
                        jobAdditionPanel), null,
                    BorderedPanel.verticalPanel(
                        JLabel("<html><p align=\"left\">Add a Skill to the selected Job:</p></html>"),
                        null, skillAdditionPanel)), hoursAdditionPanel)));
    retval.playerChanged(null, map.currentPlayer);
    retval.jMenuBar = workerMenu(menuHandler.actionPerformed, retval, model);
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
        menuHandler.register((event) => process.exit(0), "quit");
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