// TODO: Encapsulate these methods in an object

"Ask the user to choose an item from the given list; return the item the user
 chooses, or null if the process fails for any reason or the list was empty."
shared Element? chooseFromList<Element>(
        "The header to print before the list. If null, no header is printed."
        String? header, 
        "The prompt to print to ask the user for his or her choice."
        String prompt,
        "If true, and there is only one item in the list, return it without
         prompting the user."
        Boolean auto,
        "The list for the user to choose from, defined as [[tuples|Tuple]] of
         user-friendly descriptions and the objects themselves."
        [String, Element]* choices) {
    if (nonempty choices) {
        if (exists header) {
            print(header);
        }
        if (auto, choices.size == 1) {
            value [desc, item] = choices.first;
            print("Automatically choosing only item, ``desc``");
            return item;
        } else {
            printList(*choices.map(Tuple.first));
            if (exists [description, choice] = choices[inputNumber(prompt)]) {
                return choice;
            } else {
                return null;
            }
        }
    } else {
        return null;
    }
}

"Print a list of [[strings|String]] to the standard output, each starting on a
 new line with its index in the list prepended."
shared void printList(String* list) {
    for (i->string in list.indexed) {
        print("``i``. ``string``");
    }
}

"Print a prompt to the standard output, adding a space if the provided prompt
 does not end with whitespace."
void writePrompt(String prompt) {
    process.write(prompt);
    if (exists last = prompt.last, !last.whitespace) {
        process.write(" ");
    }
}

"Ask the user to enter a nonnegative integer. Loops until one is provided on
 [[the standard input|process.readLine]]."
shared Integer inputNumber(String prompt) {
    variable Integer retval = -1;
    while (retval < 0) {
        writePrompt(prompt);
        if (exists input = process.readLine(),
                is Integer temp = Integer.parse(input.replace(",", ""))) {
            retval = temp;
        } else {
            retval = -1;
        }
    }
    return retval;
}

"""Ask the user a yes-or-no question. Returns [[true]] if "yes", "true", "y",
   or "t" is provided on [[the standard input|process.readLine]], returns 
   [[false]] if "no", "false", "n", or "f" is provided, and on any other input
   asks again and again (loops) until an acceptable input is provided. (Those
   answers are parsed case-insensitively.)"""
shared Boolean inputBoolean(String prompt) {
    while (true) {
        writePrompt(prompt);
        switch (process.readLine()?.lowercased)
        case ("yes" | "true" | "y" | "t") { return true; }
        case ("no" | "false" | "n" | "f") { return false; }
        else {
            print("Please enter 'yes', 'no', 'true', or 'false',");
            print("or the first character of any of those.");
        }
    }
}
