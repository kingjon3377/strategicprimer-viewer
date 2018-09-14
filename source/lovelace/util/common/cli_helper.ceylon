shared T? chooseFromList<T>(String? header, String prompt, Boolean auto,
        [String, T]* choices) {
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

shared void printList(String* list) {
    for (i->string in list.indexed) {
        print("``i``. ``string``");
    }
}

void writePrompt(String prompt) {
    process.write(prompt);
    if (exists last = prompt.last, !last.whitespace) {
        process.write(" ");
    }
}

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

"Ask the user a yes-or-no question."
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
