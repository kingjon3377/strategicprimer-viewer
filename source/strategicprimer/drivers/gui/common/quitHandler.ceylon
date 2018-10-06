"An object to hold the action to take when the user tells the app to quit."
shared object quitHandler { // TODO: Remove this/these once eclipse/ceylon#7396 fixed
    suppressWarnings("expressionTypeNothing")
    void defaultQuitHandler() => process.exit(0);
    shared variable Anything() handler = defaultQuitHandler;
}