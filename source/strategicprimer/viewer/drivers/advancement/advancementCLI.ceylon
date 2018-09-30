import strategicprimer.drivers.common {
    DriverFailedException,
    IDriverModel,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    ISPDriver,
    CLIDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.map {
    Player
}
import strategicprimer.drivers.worker.common {
    WorkerModel,
    IWorkerModel
}
import java.io {
    IOException
}
import ceylon.logging {
    logger,
    Logger
}
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker,
    IUnit
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    Job,
    IJob,
    ProxyWorker,
    ISkill,
    ProxyJob,
    Skill
}
import lovelace.util.common {
    todo,
    matchingValue,
    singletonRandom
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"The worker-advancement CLI driver."
todo("Move most implementation stuff out into a class that takes the CLI as a class
      parameter, then *doesn't* pass it around in callback style.")
service(`interface ISPDriver`)
shared class AdvancementCLI() satisfies CLIDriver {
    "Let the user add hours to a Skill or Skills in a Job."
    void advanceJob(IJob job, ICLIHelper cli, Boolean allowExpertMentoring) {
        MutableList<ISkill> skills = ArrayList{ elements = job; };
        while (true) {
            value chosen = cli.chooseFromList(skills, "Skills in Job:",
                "No existing Skills.", "Skill to advance: ", false);
            ISkill skill;
            if (exists temp = chosen.item) {
                skill = temp;
            } else if (chosen.key <= skills.size) {
                String skillName = cli.inputString("Name of new Skill: ");
                job.addSkill(Skill(skillName, 0, 0));
                skills.clear();
                skills.addAll(job);
                if (exists temp = skills.find(matchingValue(skillName, ISkill.name))) {
                    skill = temp;
                } else {
                    cli.println("Select the new item at the next prompt.");
                    continue;
                }
            } else {
                break;
            }
            Integer oldLevel = skill.level;
            Integer hours = cli.inputNumber("Hours of experience to add: ") else 0;
            if (allowExpertMentoring) {
                Integer hoursPerHour = cli.inputNumber("'Hours' between hourly checks: ")
                    else 0;
                variable Integer remaining = hours;
                while (remaining > 0) {
                    skill.addHours(Integer.largest(remaining, hoursPerHour),
                        singletonRandom.nextInteger(100));
                    if (skill.level != oldLevel) {
                        cli.println("Worker(s) gained a level in ``skill.name``");
                    }
                    remaining -= hoursPerHour;
                }
            } else {
                for (hour in 0:hours) {
                    skill.addHours(1, singletonRandom.nextInteger(100));
                    if (skill.level != oldLevel) {
                        cli.println("Worker(s) gained a level in ``skill.name``");
                    }
                }
            }
            if (!cli.inputBoolean("Select another Skill in this Job?")) {
                break;
            }
        }
    }
    "Let the user add experience to a worker."
    void advanceSingleWorker(IWorker worker, ICLIHelper cli,
            Boolean allowExpertMentoring) {
        MutableList<IJob> jobs = ArrayList { elements = worker; };
        while (true) {
            value chosen = cli.chooseFromList(jobs,
                "Jobs in worker:", "No existing Jobs.", "Job to advance: ", false);
            IJob job;
            if (exists temp = chosen.item) {
                job = temp;
            } else if (chosen.key <= jobs.size ) {
                String jobName = cli.inputString("Name of new Job: ");
                worker.addJob(Job(jobName, 0));
                jobs.clear();
                jobs.addAll(worker);
                if (exists temp = jobs.find(matchingValue(jobName, IJob.name))) {
                    job = temp;
                } else {
                    cli.println("Select the new item at the next prompt.");
                    continue;
                }
            } else {
                break;
            }
            advanceJob(job, cli, allowExpertMentoring);
            if (!cli.inputBoolean("Select another Job in this worker?")) {
                break;
            }
        }
    }
    "Let the user add experience in a single Skill to all of a list of workers."
    void advanceWorkersInSkill(String jobName, String skillName, ICLIHelper cli,
        IWorker* workers) {
        Integer hours = cli.inputNumber("Hours of experience to add: ") else 0;
        for (worker in workers) {
            IJob job = worker.getJob(jobName);
            ISkill skill = job.getSkill(skillName);
            Integer oldLevel = skill.level;
            skill.addHours(hours, singletonRandom.nextInteger(100));
            if (skill.level != oldLevel) {
                if (oldLevel == 0, skill.name == "miscellaneous",
                    cli.inputBooleanInSeries("``worker.name`` gained ``skill.
                        level`` level(s) in miscellaneous, choose another skill?",
                    "misc-replacement")) {
                    MutableList<String> gains = ArrayList<String>();
                    for (i in 0:skill.level) {
                        ISkill replacement;
                        String replacementName;
                        Integer->ISkill? choice = cli.chooseFromList(
                            job.select(not(matchingValue("miscellaneous", ISkill.name))),
                            "Skill to gain level in:", "No other skill", "Chosen skill:",
                            false);
                        if (exists chosenSkill = choice.item) {
                            replacement = chosenSkill;
                            replacement.addHours(100, 0);
                            replacementName = replacement.name;
                        } else {
                            replacementName = cli.inputString("Skill to gain level in: ");
                            replacement = Skill(replacementName, 1, 0);
                            job.addSkill(replacement);
                            gains.add(replacementName);
                        }
                        job.removeSkill(skill);
                    }
                    for (name->count in gains.frequencies()) {
                        if (count == 1) {
                            cli.println("``worker.name`` gained a level in ``name``");
                        } else {
                            cli.println(
                                "``worker.name`` gained ``count`` levels in ``name``");
                        }
                    }
                } else {
                    cli.println("``worker.name`` gained a level in ``skill.name``");
                }
            }
        }
    }
    "Ensure that there is a Job by the given name in each worker, and return a
     collection of those Jobs."
    {IJob*} getWorkerJobs(String jobName, IWorker* workers) =>
            workers.map(shuffle(IWorker.getJob)(jobName)).distinct;
    "Let the user add experience in a given Job to all of a list of workers."
    void advanceWorkersInJob(String jobName, ICLIHelper cli, IWorker* workers) {
        {IJob*} jobs = getWorkerJobs(jobName, *workers);
        MutableList<ISkill> skills = ArrayList {
            elements = ProxyJob(jobName, false, *workers);
        };
        while (true) {
            value chosen = cli.chooseFromList(skills,
                "Skills in Jobs:", "No existing skills.", "Skill to advance: ", false);
            ISkill skill;
            if (exists temp = chosen.item) {
                skill = temp;
            } else if (chosen.key <= skills.size ) {
                String skillName = cli.inputString("Name of new Skill: ");
                for (job in jobs) {
                    job.addSkill(Skill(skillName, 0, 0));
                }
                skills.clear();
                skills.addAll(ProxyJob(jobName, false, *workers));
                if (exists temp = skills.find(matchingValue(skillName, ISkill.name))) {
                    skill = temp;
                } else {
                    cli.println("Select the new item at the next prompt.");
                    continue;
                }
            } else {
                break;
            }
            advanceWorkersInSkill(jobName, skill.name, cli, *workers);
            if (!cli.inputBoolean("Select another Skill in this Job?")) {
                break;
            }
        }
    }
    "Let the user add experience to a worker or workers in a unit."
    void advanceWorkersInUnit(IUnit unit, ICLIHelper cli, Boolean allowExpertMentoring) {
        MutableList<IWorker> workers = ArrayList { elements = unit.narrow<IWorker>(); };
        if (cli.inputBooleanInSeries("Add experience to workers individually? ")) {
            while (!workers.empty, exists chosen = cli.chooseFromList(workers,
                    "Workers in unit:", "No unadvanced workers remain.",
                    "Chosen worker: ", false).item) {
                workers.remove(chosen);
                advanceSingleWorker(chosen, cli, allowExpertMentoring);
                if (!cli.inputBoolean("Choose another worker?")) {
                    break;
                }
            }
        } else if (workers.empty) {
            cli.println("No workers in unit.");
        } else {
            MutableList<IJob> jobs = ArrayList { elements = ProxyWorker.fromUnit(unit); };
            while (true) {
                value chosen = cli.chooseFromList(jobs, "Jobs in workers:",
                    "No existing jobs.", "Job to advance: ", false);
                IJob job;
                if (exists temp = chosen.item) {
                    job = temp;
                } else if (chosen.key <= jobs.size ) {
                    String jobName = cli.inputString("Name of new Job: ");
                    for (worker in workers) {
                        worker.addJob(Job(jobName, 0));
                    }
                    jobs.clear();
                    jobs.addAll(ProxyWorker.fromUnit(unit));
                    if (exists temp = jobs.find(matchingValue(jobName, IJob.name))) {
                        job = temp;
                    } else {
                        cli.println("Select the new item at the next prompt.");
                        continue;
                    }
                } else {
                    break;
                }
                advanceWorkersInJob(job.name, cli, *workers);
                if (!cli.inputBoolean("Select another Job in these workers?")) {
                    break;
                }
            }
        }
    }
    "Let the user add experience to a player's workers."
    void advanceWorkers(IWorkerModel model, Player player, ICLIHelper cli,
            Boolean allowExpertMentoring) {
        MutableList<IUnit> units = ArrayList {
            elements = model.getUnits(player).filter(
                        (unit) => !unit.narrow<IWorker>().empty); };
        while (!units.empty, exists chosen = cli.chooseFromList(units,
                "``player.name``'s units:", "No unadvanced units remain.", "Chosen unit:",
                false).item) {
            units.remove(chosen);
            advanceWorkersInUnit(chosen, cli, allowExpertMentoring);
            if (!cli.inputBoolean("Choose another unit?")) {
                break;
            }
        }
        for (map->_ in model.allMaps) {
            model.setModifiedFlag(map, true);
        }
    }
    "Let the user choose a player to run worker advancement for."
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        IWorkerModel workerModel;
        if (is IWorkerModel model) {
            workerModel = model;
        } else {
            workerModel = WorkerModel.copyConstructor(model);
        }
        MutableList<Player> playerList = ArrayList { elements = workerModel.players; };
        try {
            while (!playerList.empty, exists chosen = cli.chooseFromList(playerList,
                    "Available players:", "No players found.", "Chosen player:",
                    false).item) {
                playerList.remove(chosen);
                advanceWorkers(workerModel, chosen, cli,
                    options.hasOption("--allow-expert-mentoring"));
                if (!cli.inputBoolean("Select another player?")) {
                    break;
                }
            }
        } catch (IOException except) {
            throw DriverFailedException(except, "I/O error interacting with user");
        }
    }
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-a", "--adv"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "View a player's workers and manage their advancement";
        longDescription = """View a player's units, the workers in those units, each
                             worker's Jobs, and his or her level in each Skill in each
                             Job.""";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptionsTemp = [ "--current-turn=NN", "--allow-expert-mentoring" ];
    };
}
