"Wrap a no-argument method to pass it to a listener or other caller that wants to pass it
 a parameter."
shared Anything(Type) silentListener<Type>(Anything() method) =>
                (Type ignored) => method();
