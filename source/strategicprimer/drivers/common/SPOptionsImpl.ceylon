import ceylon.collection {
    MutableMap,
    HashMap
}
"""The command-line options passed by the user. At this point we assume that if any option
   is passed to an app more than once, the subsequent option overrides the previous, and
   any option passed without argument has an implied argument of "true"."""
shared class SPOptionsImpl({<String->String>*} existing = [])
        satisfies SPOptions&KeyedCorrespondenceMutator<String, String> {
    MutableMap<String, String> options = HashMap<String, String>();
    options.putAll(existing);
    shared void addOption(String option, String argument = "true") {
        if ("false" == argument) {
            options.remove(option);
        } else {
            options[option] = argument;
        }
    }
    shared actual Boolean hasOption(String option) => options.defines(option);
    shared actual String getArgument(String option) => options[option] else "false";
    shared actual SPOptionsImpl copy() => SPOptionsImpl(options);
    shared actual String string {
        StringBuilder builder = StringBuilder();
        for (key->val in options) {
            if (val == "true") {
                builder.append(key);
            } else {
                builder.append("``key``=``val``");
            }
            builder.appendNewline();
        }
        return builder.string;
    }
    shared actual Iterator<String->String> iterator() => options.iterator();
    shared actual Boolean defines(String key) => options.defines(key);
    shared actual String get(String key) => options[key] else "false";
    shared actual void put(String key, String item) => addOption(key, item);
}
