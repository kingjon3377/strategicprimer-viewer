import strategicprimer.model.map {
	Point
}
void run() {
	value arguments = process.arguments;
	assert (exists firstArg = arguments.first, is Integer num = Integer.parse(firstArg));
}