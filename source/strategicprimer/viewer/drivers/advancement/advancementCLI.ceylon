import strategicprimer.drivers.common {
    DriverFailedException,
    IDriverModel,
    DriverUsage,
    ParamCount,
    SimpleCLIDriver,
    IDriverUsage,
    SPOptions
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.map {
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
import strategicprimer.model.map.fixtures.mobile {
	IWorker,
	IUnit
}
import ceylon.collection {
	ArrayList,
	MutableList
}
import strategicprimer.model.map.fixtures.mobile.worker {
	Job,
	IJob,
	ProxyWorker,
	ISkill,
	ProxyJob,
	Skill
}
import lovelace.util.common {
	todo
}
import lovelace.util.jvm {
	singletonRandom
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"The worker-advancement CLI driver."
todo("Move most implementation stuff out into a class that takes the CLI as a class parameter, then
      *doesn't* pass it around in callback style.")
shared object advancementCLI satisfies SimpleCLIDriver {
	"Let the user add hours to a Skill or Skills in a Job."
	void advanceJob(IJob job, ICLIHelper cli) {
		MutableList<ISkill> skills = ArrayList{ *job };
		cli.loopOnMutableList<ISkill>(skills, (clh, List<ISkill> list) => clh.chooseFromList(
			list, "Skills in Job:", "No existing Skills.", "Skill to advance: ", false),
		"Select another Skill in this Job? ",
		(MutableList<ISkill> list, ICLIHelper clh) {
			String skillName = clh.inputString("Name of new Skill: ");
			job.addSkill(Skill(skillName, 0, 0));
			list.clear();
			for (skill in job) {
				list.add(skill);
			}
			return list.find((item) => skillName == item.name);
		}, (ISkill skill, clh) {
			Integer oldLevel = skill.level;
			Integer hours = clh.inputNumber("Hours of experience to add: ");
			// TODO: Make frequency of leveling checks (i.e. size of hour-chunks to add at
			// a time) configurable. This is correct (per documentation before I added
			// support for workers to the map format) for ordinary experience, but workers
			// learning or working under a more experienced mentor can get multiple
			// "hours" per hour, and they should only check for a level with each
			// *actual* hour.
			for (hour in 0:hours) {
				skill.addHours(1, singletonRandom.nextInteger(100));
				if (skill.level != oldLevel) {
					clh.println("Worker(s) gained a level in ``skill.name``");
				}
			}
		});
	}
	"Let the user add experience to a worker."
	void advanceSingleWorker(IWorker worker, ICLIHelper cli) {
		MutableList<IJob> jobs = ArrayList { *worker };
		cli.loopOnMutableList(jobs, (clh, List<IJob> list) => clh.chooseFromList(list,
			"Jobs in worker:", "No existing Jobs.", "Job to advance: ", false),
		"Select another Job in this worker? ",
		(MutableList<IJob> list, clh) {
			String jobName = clh.inputString("Name of new Job: ");
			worker.addJob(Job(jobName, 0));
			list.clear();
			for (job in worker) {
				list.add(job);
			}
			return list.find((item) => jobName == item.name);
		}, advanceJob);
	}
	"Let the user add experience in a single Skill to all of a list of workers."
	void advanceWorkersInSkill(String jobName, String skillName, ICLIHelper cli,
		IWorker* workers) {
		Integer hours = cli.inputNumber("Hours of experience to add: ");
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
							job.filter((skl) => "miscellaneous" != skl.name).sequence(),
							"Skill to gain level in:", "No other skill", "Chosen skill:", false);
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
							cli.println("``worker.name`` gained ``count`` levels in ``name``");
						}
					}
				} else {
					cli.println("``worker.name`` gained a level in ``skill.name``");
				}
			}
		}
	}
	"Ensure that there is a Job by the given name in each worker, and return a collection of
	 those Jobs."
	{IJob*} getWorkerJobs(String jobName, IWorker* workers) =>
			set { for (worker in workers) worker.getJob(jobName) };
	"Let the user add experience in a given Job to all of a list of workers."
	void advanceWorkersInJob(String jobName, ICLIHelper cli, IWorker* workers) {
		{IJob*} jobs = getWorkerJobs(jobName, *workers);
		MutableList<ISkill> skills = ArrayList {
			for (skill in ProxyJob(jobName, false, *workers)) skill
		};
		cli.loopOnMutableList(skills, (clh, List<ISkill> list) => clh.chooseFromList(list,
			"Skills in Jobs:", "No existing skills.", "Skill to advance: ", false),
		"Select another Skill in this Job? ",
		(MutableList<ISkill> list, clh) {
			String skillName = clh.inputString("Name of new Skill: ");
			for (job in jobs) {
				job.addSkill(Skill(skillName, 0, 0));
			}
			skills.clear();
			for (skill in ProxyJob(jobName, false, *workers)) {
				skills.add(skill);
			}
			return skills.find((item) => skillName == item.name);
		}, (ISkill skill, clh) =>
				advanceWorkersInSkill(jobName, skill.name, clh, *workers));
	}
	"Let the user add experience to a worker or workers in a unit."
	void advanceWorkersInUnit(IUnit unit, ICLIHelper cli) {
		IWorker[] workers = [for (member in unit) if (is IWorker member) member];
		if (cli.inputBooleanInSeries("Add experience to workers individually? ")) {
			cli.loopOnList(workers, (clh, List<IWorker> list) => clh.chooseFromList(list,
				"Workers in unit:", "No unadvanced workers remain.", "Chosen worker: ",
				false),
			"Choose another worker? ", advanceSingleWorker);
		} else if (workers.empty) {
			cli.println("No workers in unit.");
		} else {
			MutableList<IJob> jobs = ArrayList { *ProxyWorker.fromUnit(unit) };
			cli.loopOnMutableList(jobs,
				(ICLIHelper clh, List<IJob> list) => clh.chooseFromList(
					list, "Jobs in workers:", "No existing jobs.",
					"Job to advance: ", false),
				"Select another Job in these workers? ",
				(MutableList<IJob> list, ICLIHelper clh) {
					String jobName = clh.inputString("Name of new Job: ");
					for (worker in workers) {
						worker.addJob(Job(jobName, 0));
					}
					list.clear();
					for (job in ProxyWorker.fromUnit(unit)) {
						list.add(job);
					}
					return list.find((item) => jobName == item.name);
				}, (IJob job, clh) => advanceWorkersInJob(job.name, clh, *workers));
			}
		}
	"Let the user add experience to a player's workers."
	void advanceWorkers(IWorkerModel model, Player player, ICLIHelper cli) {
		IUnit[] units = [*model.getUnits(player).filter((unit) => !unit.narrow<IWorker>().empty)];
		cli.loopOnList(units, (clh, List<IUnit> list) => clh.chooseFromList(list,
			"``player.name``'s units:", "No unadvanced units remain.",
			"Chosen unit: ", false),
		"Choose another unit? ", advanceWorkersInUnit);
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
        Player[] playerList = [*workerModel.players];
        try {
            cli.loopOnList(playerList,
                        (clh, List<Player> list) => clh.chooseFromList(list,
                            "Available players:", "No players found.", "Chosen player: ",
                            false),
                "Select another player? ",
                        (Player player, clh) => advanceWorkers(workerModel, player, clh));
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
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
}
