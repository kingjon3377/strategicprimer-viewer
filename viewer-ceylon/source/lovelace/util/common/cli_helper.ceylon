shared T? chooseFromList<T>(String? header, String prompt, Boolean auto,
        [String, T]* choices) {
    if (nonempty choices) {
        if (exists header) {
            print(header);
        }
        if (auto, choices.size == 1) {
            value item = choices.first;
            print("Automatically choosing only item, ``item.first``");
            return item.rest.first;
        } else {
            printList(for (choice in choices) choice.first);
            Integer number = inputNumber(prompt);
            [String, T]? choice = choices[number];
            if (exists choice) {
                [T] rest = choice.rest;
                return rest.first;
            } else {
                return null;
            }
        }
    } else {
        return null;
    }
}

shared void printList(String* list) {
    variable Integer i = 0;
    for (string in list) {
        print("``i``. ``string``");
        i++;
    }
}

shared Integer inputNumber(String prompt) {
    variable Integer retval = -1;
    while (retval < 0) {
        process.write(prompt);
        if (exists input = process.readLine()) {
            value temp = Integer.parse(input.replace(",", ""));
            if (is Integer temp) {
                retval = temp;
            } else {
                retval = -1;
            }
        } else {
            return -1;
        }
    }
    return retval;
}

"Ask the user a yes-or-no question."
shared Boolean inputBoolean(String prompt) {
    while (true) {
        process.write(prompt);
        String? input = process.readLine();
        switch (input?.lowercased)
        case ("yes" | "true" | "y" | "t") { return true; }
        case ("no" | "false" | "n" | "f") { return false; }
        else {
            print("Please enter 'yes', 'no', 'true', or 'false',");
            print("or the first character of any of those.");
        }
    }
}
