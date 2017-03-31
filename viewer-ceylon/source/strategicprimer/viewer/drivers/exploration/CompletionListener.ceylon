import java.util {
    EventListener
}

import lovelace.util.common {
    todo
}
"An interface for objects that want to start something when another object has finished
 whatever it's doing."
todo("Does this really need to be shared?")
shared interface CompletionListener satisfies EventListener {
	"Stop waiting for the thing being listened to, because it's finished."
	shared formal void finished();
}