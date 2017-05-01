import ceylon.collection {
    MutableList,
    MutableSet,
    ArrayList,
    HashSet
}

import strategicprimer.model.map {
    IFixture
}
import strategicprimer.model.map.fixtures {
    UnitMember
}
import strategicprimer.model.map.fixtures.mobile {
    IWorker,
    IUnit,
    ProxyFor
}
import ceylon.logging {
    logger,
    Logger
}
Logger log = logger(`module strategicprimer.model`);
"An IWorker implementation to make the UI able to operate on all of a unit's workers at
 once."
shared class ProxyWorker satisfies UnitMember&IWorker&ProxyFor<IWorker> {
    static Boolean comparison(Anything one, Anything two) {
        if (exists one) {
            if (exists two) {
                return one == two;
            } else {
                return false;
            }
        } else {
            return !two exists;
        }
    }
    "If false, this is representing all the workers in a single unit; if true, it is
     representing the corresponding workers in corresponding units in different maps."
    shared actual Boolean parallel;
    "The proxy Jobs."
    MutableList<IJob&ProxyFor<IJob>> proxyJobs = ArrayList<IJob&ProxyFor<IJob>>();
    "The jobs we're proxying for."
    MutableSet<String> jobNames = HashSet<String>();
    "The workers being proxied."
    MutableList<IWorker> workers = ArrayList<IWorker>();
    "Cached stats for the workers. Null if there are no workers being proxied or if they
     do not share identical stats."
    variable WorkerStats? statsCache;
    new noop(Boolean parallelProxy) {
        parallel = parallelProxy;
        statsCache = null;
    }
    shared new fromUnit(IUnit unit) {
        parallel = false;
        statsCache = null;
        for (member in unit) {
            if (is IWorker member) {
                WorkerStats? tempStats = member.stats;
                WorkerStats? priorStats = statsCache;
                if (workers.empty) {
                    statsCache = tempStats;
                } else if (!comparison(tempStats, priorStats)) {
                    statsCache = null;
                }
                workers.add(member);
                for (job in member) {
                    jobNames.add(job.name);
                }
            }
        }
        for (job in jobNames) {
            proxyJobs.add(ProxyJob(job, false, *workers));
        }
    }
    shared new fromWorkers(IWorker* proxiedWorkers) {
        parallel = true;
        statsCache = null;
        for (worker in proxiedWorkers) {
            WorkerStats? tempStats = worker.stats;
            WorkerStats? priorStats = statsCache;
            if (workers.empty) {
                statsCache = tempStats;
            } else if (!comparison(tempStats, priorStats)) {
                statsCache = null;
            }
            workers.add(worker);
            for (job in worker) {
                jobNames.add(job.name);
            }
        }
        for (job in jobNames) {
            proxyJobs.add(ProxyJob(job, true, *proxiedWorkers));
        }
    }
    shared actual IWorker copy(Boolean zero) {
        ProxyWorker retval = ProxyWorker.noop(parallel);
        for (worker in workers) {
            retval.addProxied(worker.copy(zero));
        }
        return retval;
    }
    shared actual Integer id {
        if (parallel) {
            return workers.map(IWorker.id)
                .reduce<Integer>((left, right) =>
                    (left == right) then left else -1) else -1;
        } else {
            return -1;
        }
    }
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is ProxyWorker fixture) {
            return parallel == fixture.parallel && proxyJobs == fixture.proxyJobs;
        } else {
            return false;
        }
    }
    shared actual Iterator<IJob> iterator() => proxyJobs.iterator();
    shared actual Boolean addJob(IJob job) {
        if (jobNames.contains(job.name)) {
            return false;
        } else {
            value proxy = ProxyJob(job.name, parallel, *workers);
            jobNames.add(proxy.name);
            proxyJobs.add(proxy);
            return true;
        }
    }
    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        report("isSubset called on ProxyWorker");
        return false;
    }
    shared actual void addProxied(IWorker item) {
        if (is Identifiable item, item === this) {
            return;
        }
        WorkerStats? tempStats = item.stats;
        WorkerStats? priorStats = statsCache;
        if (workers.empty) {
            statsCache = tempStats;
        } else if (exists tempStats) {
            if (exists priorStats) {
                if (tempStats != priorStats) {
                    statsCache = null;
                }
            }
        } else if (exists priorStats) {
            statsCache = null;
        }
        workers.add(item);
        MutableList<IJob> proxyJobsTemp = ArrayList<IJob> { *proxyJobs };
        for (job in item) {
            String name = job.name;
            if (jobNames.contains(name)) {
                for (proxyJob in proxyJobs) {
                    if (proxyJob.name == name) {
                        proxyJob.addProxied(job);
                        proxyJobsTemp.remove(proxyJob);
                    }
                }
            } else {
                jobNames.add(name);
                proxyJobs.add(ProxyJob(name, parallel, *workers));
            }
        }
        for (proxyJob in proxyJobs) {
            // FIXME: This can't be right!
            String name = proxyJob.name;
            IJob job = Job(name, 0);
            proxyJob.addProxied(job);
        }
    }
    shared actual Iterable<IWorker> proxied => {*workers};
    shared actual String defaultImage {
        variable String? retval = null;
        for (worker in workers) {
            if (exists temp = retval) {
                if (temp != worker.defaultImage) {
                    return "worker.png";
                }
            } else {
                retval = worker.defaultImage;
            }
        }
        return retval else "worker.png";
    }
    shared actual String image {
        variable String? retval = null;
        for (worker in workers) {
            if (exists temp = retval) {
                if (temp != worker.image) {
                    return "";
                }
            } else {
                retval = worker.image;
            }
        }
        return retval else "";
    }
    assign image {
        log.warn("image setter called on a ProxyWorker");
        for (worker in workers) {
            worker.image = image;
        }
    }
    shared actual String kind {
        variable String? retval = null;
        for (worker in workers) {
            if (exists temp = retval) {
                if (temp != worker.kind) {
                    return "proxied";
                }
            } else {
                retval = worker.kind;
            }
        }
        return retval else "proxied";
    }
    shared actual String name {
        variable String? retval = null;
        for (worker in workers) {
            if (exists temp = retval) {
                if (temp != worker.name) {
                    return "proxied";
                }
            } else {
                retval = worker.name;
            }
        }
        return retval else "proxied";
    }
    shared actual String race => kind;
    shared actual IJob getJob(String jobName) {
        for (job in proxyJobs) {
            if (job.name == jobName) {
                return job;
            }
        }
        value retval = ProxyJob(jobName, parallel, *workers);
        jobNames.add(jobName);
        proxyJobs.add(retval);
        return retval;
    }
    shared actual WorkerStats? stats => statsCache;
}