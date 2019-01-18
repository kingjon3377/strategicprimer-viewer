import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import lovelace.util.common {
    singletonRandom,
    todo,
    matchingValue
}
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker,
    IUnit
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    ProxyWorker,
    Job,
    IJob,
    ProxyJob,
    ISkill,
    Skill
}

"Logic extracted from the advancement CLI so it can be used by the turn-running CLI as
 well."
shared class AdvancementCLIHelper(ICLIHelper cli) satisfies LevelGainSource {
    MutableList<LevelGainListener> levelListeners = ArrayList<LevelGainListener>();

    shared actual void addLevelGainListener(LevelGainListener listener) =>
        levelListeners.add(listener);
    shared actual void removeLevelGainListener(LevelGainListener listener) =>
        levelListeners.remove(listener);

    void fireLevelEvent(String workerName, String jobName, String skillName,
        Integer gains, Integer currentLevel) {
        for (listener in levelListeners) {
            listener.level(workerName, jobName, skillName, gains, currentLevel);
        }
    }

    "Let the user add hours to a Skill or Skills in a Job."
    void advanceJob(String workerName, IJob job, Boolean allowExpertMentoring) {
        MutableList<ISkill> skills = ArrayList{ elements = job; };
        while (true) {
            value chosen = cli.chooseFromList(skills, "Skills in Job:",
                "No existing Skills.", "Skill to advance: ", false);
            ISkill skill;
            if (exists temp = chosen.item) {
                skill = temp;
            } else if (chosen.key <= skills.size) {
                if (exists skillName = cli.inputString("Name of new Skill: ")) {
                    job.addSkill(Skill(skillName, 0, 0));
                    skills.clear();
                    skills.addAll(job);
                    if (exists temp = skills.find(matchingValue(skillName,
                        ISkill.name))) {
                        skill = temp;
                    } else {
                        cli.println("Select the new item at the next prompt.");
                        continue;
                    }
                } else {
                    return;
                }
            } else {
                break;
            }
            Integer oldLevel = skill.level;
            Integer hours = cli.inputNumber("Hours of experience to add: ") else 0;
            if (allowExpertMentoring) {
                Integer hoursPerHour = cli.inputNumber("'Hours' between hourly checks: ")
                else 0;
                variable Integer remaining;
                if (hoursPerHour > 0) {
                    remaining = hours;
                } else {
                    remaining = 0;
                }
                while (remaining > 0) {
                    skill.addHours(Integer.largest(remaining, hoursPerHour),
                        singletonRandom.nextInteger(100));
                    remaining -= hoursPerHour;
                }
            } else {
                for (hour in 0:hours) {
                    skill.addHours(1, singletonRandom.nextInteger(100));
                }
            }
            if (skill.level != oldLevel) {
                String count = (skill.level - oldLevel == 1) then "a level"
                    else (skill.level - oldLevel).string + " levels";
                cli.println("``workerName`` gained ``count`` in ``skill.name``");
                fireLevelEvent(workerName, job.name, skill.name, skill.level - oldLevel,
                    skill.level);
            }
            if (exists continuation =
                cli.inputBoolean("Select another Skill in this Job?"), continuation) {
                // continue;
            } else {
                break;
            }
        }
    }

    "Let the user add experience to a worker."
    void advanceSingleWorker(IWorker worker, Boolean allowExpertMentoring) {
        MutableList<IJob> jobs = ArrayList { elements = worker; };
        while (true) {
            value chosen = cli.chooseFromList(jobs,
                "Jobs in worker:", "No existing Jobs.", "Job to advance: ", false);
            IJob job;
            if (exists temp = chosen.item) {
                job = temp;
            } else if (chosen.key <= jobs.size ) {
                String? jobName = cli.inputString("Name of new Job: ");
                if (!jobName exists) {
                    return;
                }
                assert (exists jobName);
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
            advanceJob(worker.name, job, allowExpertMentoring);
            if (exists continuation =
                    cli.inputBoolean("Select another Job in this worker?"),
                continuation) {
                // continue;
            } else {
                break;
            }
        }
    }

    "Let the user add experience in a single Skill to all of a list of workers."
    todo("Support expert mentoring")
    void advanceWorkersInSkill(String jobName, String skillName, IWorker* workers) {
        Integer hours = cli.inputNumber("Hours of experience to add: ") else 0;
        for (worker in workers) {
            IJob job = worker.getJob(jobName);
            ISkill skill = job.getSkill(skillName);
            Integer oldLevel = skill.level;
            skill.addHours(hours, singletonRandom.nextInteger(100));
            if (skill.level != oldLevel) {
                if (oldLevel == 0, skill.name == "miscellaneous") {
                    Boolean? chooseAnother =
                        cli.inputBooleanInSeries("``worker.name`` gained ``skill.
                                level`` level(s) in miscellaneous, choose another skill?",
                            "misc-replacement");
                    switch (chooseAnother)
                    case (null) { return; }
                    case (false) { continue; }
                    case (true) {}
                    MutableList<String> gains = ArrayList<String>();
                    for (i in 0:skill.level) {
                        ISkill replacement;
                        Integer->ISkill? choice = cli.chooseFromList(
                            job.select(not(matchingValue("miscellaneous", ISkill.name))),
                            "Skill to gain level in:", "No other skill", "Chosen skill:",
                            false);
                        if (exists chosenSkill = choice.item) {
                            replacement = chosenSkill;
                            replacement.addHours(100, 0);
                        } else if (exists replacementName =
                                cli.inputString("Skill to gain level in: ")) {
                            replacement = Skill(replacementName, 1, 0);
                            job.addSkill(replacement);
                            gains.add(replacementName);
                        } else {
                            return;
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
                        fireLevelEvent(worker.name, job.name, name, count,
                            job.getSkill(name).level);
                    }
                } else {
                    cli.println("``worker.name`` gained a level in ``skill.name``");
                    fireLevelEvent(worker.name, job.name, skill.name,
                        skill.level - oldLevel, skill.level);
                }
            }
        }
    }

    "Ensure that there is a Job by the given name in each worker, and return a
     collection of those Jobs."
    // FIXME: Test that this actually works when some workers lack the Job!
    {IJob*} getWorkerJobs(String jobName, IWorker* workers) =>
        workers.map(shuffle(IWorker.getJob)(jobName)).distinct;

    "Let the user add experience in a given Job to all of a list of workers."
    void advanceWorkersInJob(String jobName, IWorker* workers) {
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
                if (exists skillName = cli.inputString("Name of new Skill: ")) {
                    for (job in jobs) {
                        job.addSkill(Skill(skillName, 0, 0));
                    }
                    skills.clear();
                    skills.addAll(ProxyJob(jobName, false, *workers));
                    if (exists temp =
                            skills.find(matchingValue(skillName, ISkill.name))) {
                        skill = temp;
                    } else {
                        cli.println("Select the new item at the next prompt.");
                        continue;
                    }
                } else {
                    return;
                }
            } else {
                break;
            }
            advanceWorkersInSkill(jobName, skill.name, *workers);
            if (exists continuation =
                    cli.inputBoolean("Select another Skill in this Job?"), continuation) {
                // continue;
            } else {
                break;
            }
        }
    }

    "Let the user add experience to a worker or workers in a unit."
    shared void advanceWorkersInUnit(IUnit unit, Boolean allowExpertMentoring) {
        MutableList<IWorker> workers = ArrayList { elements = unit.narrow<IWorker>(); };
        Boolean? individualAdvancement =
            cli.inputBooleanInSeries("Add experience to workers individually? ");
        switch (individualAdvancement)
        case (true) {
            while (!workers.empty, exists chosen = cli.chooseFromList(workers,
                    "Workers in unit:", "No unadvanced workers remain.",
                    "Chosen worker: ", false).item) {
                workers.remove(chosen);
                advanceSingleWorker(chosen, allowExpertMentoring);
                if (exists continuation = cli.inputBoolean("Choose another worker?"),
                        continuation) {
                    // continue;
                } else {
                    break;
                }
            }
        } case (false) {
            if (workers.empty) {
                cli.println("No workers in unit.");
            } else {
                MutableList<IJob> jobs =
                    ArrayList { elements = ProxyWorker.fromUnit(unit); };
                while (true) {
                    value chosen = cli.chooseFromList(jobs, "Jobs in workers:",
                        "No existing jobs.", "Job to advance: ", false);
                    IJob job;
                    if (exists temp = chosen.item) {
                        job = temp;
                    } else if (chosen.key<=jobs.size) {
                        String? jobName = cli.inputString("Name of new Job: ");
                        if (!jobName exists) {
                            return;
                        }
                        assert (exists jobName);
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
                    advanceWorkersInJob(job.name, *workers);
                    if (exists continuation =
                            cli.inputBoolean("Select another Job in these workers?"),
                        continuation) {
                        // continue;
                    } else {
                        break;
                    }
                }
            }
        }
        case (null) {}
    }
}
