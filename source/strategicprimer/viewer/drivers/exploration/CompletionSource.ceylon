"An interface for objects that can tell others when they've finished something."
interface CompletionSource {
    "Call the given function when we finish something in future."
    shared formal void addCompletionListener(Anything() listener);
    "Stop calling the given function on completion."
    shared formal void removeCompletionListener(Anything() listener);
}