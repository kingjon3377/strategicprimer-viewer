import java.util {
    EventListener
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map.fixtures.mobile {
    IWorker
}
"An interface for things that want to accept a new user-created worker."
todo("Drop this interface, as the only implementer is also the only user of that method?")
interface NewWorkerListener satisfies EventListener {
    "Add the new worker."
    shared formal void addNewWorker(IWorker worker);
}