shared Result tryUntilSuccess<Result,Arg,Error>(Result(Arg) method, {Arg*} stream,
        Error() errorConstructor) given Error satisfies Throwable {
    for (item in stream) {
        try {
            return method(item);
        } catch (Error error) {
            // ignore
        }
    }
    throw errorConstructor();
}
