import controller.map.formatexceptions {
    SPFormatException
}
import controller.map.misc {
    DuplicateIDException
}

"A slightly-customizable warning-handling interface."
shared interface Warning of ignore|warn|die|CustomWarningHandler {
	"Handle a warning, e.g. if a particular map-format construct is deprecated."
	shared formal void handle(Exception warning);
}
shared object warningLevels {
	shared Warning ignore => package.ignore;
	shared Warning warn => package.warn;
	shared Warning die => package.die;
	shared Warning custom(Anything(Exception)|Anything(String) handler =
			process.writeLine) => CustomWarningHandler(handler);
	"The default warning handler. This is provided so that it can in theory be changed later in
	 one place rather than everywhere."
	shared Warning default => warn;
}
"Don't do anything with warnings."
shared object ignore satisfies Warning {
	shared actual void handle(Exception warning) {}
}
"Log each warning, but let them pass."
shared object warn satisfies Warning {
	shared actual void handle(Exception warning) {
		if (is SPFormatException|DuplicateIDException warning) {
			log.warn(warning.message);
		} else {
			log.warn("Warning: ", warning);
		}
	}
}
"Treat warnings as errors."
shared object die satisfies Warning {
	shared actual void handle(Exception warning) {
		throw warning;
	}
}
"A warning handler that takes a user-provided handler."
shared class CustomWarningHandler(Anything(Exception)|Anything(String) handler = process.writeLine)
		satisfies Warning {
	shared actual void handle(Exception warning);
	if (is Anything(Exception) handler) {
		handle = handler;
	} else {
		handle = (Exception warning) {
			if (is SPFormatException warning) {
				handler("SP format warning: ``warning.message``");
			} else {
				handler("Warning: ``warning.message``");
			}
		};
	}
}
