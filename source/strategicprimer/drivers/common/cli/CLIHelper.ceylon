import ceylon.math.decimal {
    Decimal,
    decimalNumber,
    parseDecimal
}
import ceylon.collection {
    MutableMap,
    HashMap,
    MutableList,
    ArrayList
}
import lovelace.util.jvm {
    isNumeric,
    parseInt
}
import java.io {
    IOException
}
import strategicprimer.model.map {
    HasName
}
import lovelace.util.common {
    todo
}
"A helper class to let help CLIs interact with the user, encapsulating input and output
 streams."
shared sealed class CLIHelper(istream = process.readLine, ostream = process.write)
		satisfies ICLIHelper {
	"A way to read a line at a time, presumably from the user."
	String?() istream;
	"A consumer of output, presumably sending it to the user."
	Anything(String) ostream;
	"The current state of the yes-to-all/no-to-all possibility. Absent if not set,
	 present if set, and the boolean value is what to return."
	MutableMap<String, Boolean> seriesState = HashMap<String, Boolean>();
	"Print a prompt, adding whitespace if the prompt didn't end with it."
	void writePrompt(String prompt) {
		ostream(prompt);
		if (exists last = prompt.last, !last.whitespace) {
			ostream(" ");
		}
	}
	"Ask the user a yes-or-no question."
	shared actual Boolean inputBoolean(String prompt) {
		while (true) {
			String input = inputString(prompt).lowercased;
			switch(input)
			case ("yes"|"true"|"y"|"t") { return true; }
			case ("no"|"false"|"n"|"f") { return false; }
			else {
				ostream("""Please enter "yes", "no", "true", or "false",
				           or the first character of any of those.
				           """);
			}
		}
	}
	"Ask the user to choose an item from the list, and if he does carry out an
	 operation on it and then ask if he wants to do another."
	shared actual void loopOnList<Element>(Element[] list,
			<Integer->Element?>(ICLIHelper, List<Element>) choice, String prompt,
			Anything(Element, ICLIHelper) operation)
			given Element satisfies Object {
		MutableList<Element> temp = ArrayList { *list };
		while (!temp.empty) {
			Integer->Element? chosen = choice(this, temp);
			if (exists item = chosen.item) {
				temp.delete(chosen.key);
				operation(item, this);
				if (!inputBoolean(prompt)) {
					break;
				}
			} else {
				break;
			}
		}
	}
	"Ask the user to choose an item from the list, and if he does carry out an
	 operation on it and then ask if he wants to do another."
	shared actual void loopOnMutableList<Element>(MutableList<Element> list,
			<Integer->Element?>(ICLIHelper, List<Element>) choice, String prompt,
			ListAmendment<Element> addition, Anything(Element, ICLIHelper) operation)
			given Element satisfies Object {
		while (true) {
			value chosen = choice(this, list);
			Element item;
			if (exists temp = chosen.item) {
				item = temp;
			} else if (chosen.key <= list.size ) {
				if (exists temp = addition(list, this)) {
					item = temp;
				} else {
					println("Select the new item at the next prompt.");
					continue;
				}
			} else {
				break;
			}
			operation(item, this);
			if (!inputBoolean(prompt)) {
				break;
			}
		}
	}
	"Print a list of things by name and number."
	void printList<out Element>({Element*} list, String(Element) func) {
		for (index->item in list.indexed) {
			ostream("``index``: ``func(item)``");
			ostream(operatingSystem.newline);
		}
	}
	"Implementation of chooseFromList() and chooseStringFromList()."
	Integer->Element? chooseFromListImpl<Element>({Element*} items, String description,
			String none, String prompt, Boolean auto, String(Element) func)
			given Element satisfies Object {
		if (items.empty) {
			ostream(none);
			ostream(operatingSystem.newline);
			return -1->null;
		}
		ostream(description);
		ostream(operatingSystem.newline);
		if (auto, !items.rest.first exists) {
			assert (exists first = items.first);
			ostream("Automatically choosing only item, ``func(first)``.");
			ostream(operatingSystem.newline);
			return 0->first;
		} else {
			printList(items, func);
			Integer retval = inputNumber(prompt);
			return retval->items.getFromFirst(retval);
		}
	}
	"Have the user choose an item from a list."
	shared actual Integer->Element? chooseFromList<out Element>(
			Element[]|List<Element> list, String description, String none,
			String prompt, Boolean auto) given Element satisfies HasName&Object =>
			chooseFromListImpl<Element>(list, description, none, prompt, auto,
				HasName.name);
	"Read input from the input stream repeatedly until a non-negative integer is entered,
	 then return it."
	shared actual Integer inputNumber(String prompt) {
		variable Integer retval = -1;
		while (retval < 0) {
			writePrompt(prompt);
			if (exists input = istream()) {
				if (isNumeric(input)) {
					assert (exists temp = parseInt(input));
					retval = temp;
				}
			} else {
				throw IOException("Null line of input");
			}
		}
		return retval;
	}
	"Read from the input stream repeatedly until a valid non-negative decimal number is
	 entered, then return it."
	shared actual Decimal inputDecimal(String prompt) {
		variable Decimal retval = decimalNumber(-1);
		Decimal zero = decimalNumber(0);
		while (retval.compare(zero) == smaller) {
			writePrompt(prompt);
			if (exists input = istream()) {
				if (exists temp = parseDecimal(input.trimmed)) {
					retval = temp;
				} else {
					ostream("Invalid number.");
					ostream(operatingSystem.newline);
				}
			} else {
				throw IOException("Null line of input");
			}
		}
		return retval;
	}
	"Read a line of input from the input stream. It is trimmed of leading and trailing
	 whitespace."
	shared actual String inputString(String prompt) {
		writePrompt(prompt);
		return istream()?.trimmed else "";
	}
	"Ask the user a yes-or-no question, allowing yes-to-all or no-to-all to skip further
	 questions."
	todo("Allow caller to specify loop-breaking response like 'quit'")
	shared actual Boolean inputBooleanInSeries(String prompt, String key) {
		if (exists retval = seriesState[key]) {
			writePrompt(prompt);
			ostream((retval) then "yes" else "no");
			ostream(operatingSystem.newline);
			return retval;
		} else {
			while (true) {
				String input = inputString(prompt).lowercased;
				switch(input)
				case ("all"|"ya"|"ta"|"always") {
					seriesState[key] = true;
					return true;
				}
				case ("none"|"na"|"fa"|"never") {
					seriesState[key] = false;
					return false;
				}
				case ("yes"|"true"|"y"|"t") { return true; }
				case ("no"|"false"|"n"|"f") { return false; }
				else {
					ostream(
						"""Please enter "yes", "no", "true", or "false", the first
						   character of any of those, or "all", "none", "always", or
						   "never" to use the same answer for all further questions
						   """);
				}
			}
		}
	}
	"Have the user choose an item from a list."
	shared actual Integer->String? chooseStringFromList(String[] items,
			String description, String none, String prompt, Boolean auto) =>
			chooseFromListImpl<String>(items, description, none, prompt, auto,
					identity);
	"Print the specified string, then a newline."
	shared actual void println(String line) {
		ostream(line);
		ostream(operatingSystem.newline);
	}
	"Print the specified string."
	shared actual void print(String text) => ostream(text);
}
