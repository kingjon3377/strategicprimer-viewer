import java.util {
    EventListener
}

"An interface for objects that want to start something when another object has finished
 whatever it's doing." // TODO: Why not just replace with Anything()?
interface CompletionListener satisfies EventListener {
    "Stop waiting for the thing being listened to, because it's finished."
    shared formal void finished();
}
