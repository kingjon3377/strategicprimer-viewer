"Given a method reference and arguments matching its signature, returns a no-arg method
 that invokes the provided method with the provided arguments."
shared Return() defer<Return, Args>(Return(*Args) method, Args args)
		given Args satisfies Anything[] => () => method(*args);