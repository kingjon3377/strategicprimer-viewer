"An interface for applets, subcommands that the user chooses between in certqin CLI apps."
shared interface Applet<Arguments=[]> given Arguments satisfies Anything[] {
    "Ways that this applet may be invoked."
    shared formal {String+} commands;
    "What this applet does: a description presented to the user."
    shared formal String description;
    "What should happen when the user calls for this applet."
    shared formal Anything(*Arguments) invoke;
}
