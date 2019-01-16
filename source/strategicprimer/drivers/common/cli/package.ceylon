"This package contains the [[ICLIHelper]] interface, along with the [[CLIHelper]]
 implementation of it and [[its test suite|cliTests]].

 The [[ICLIHelper]] encapsulates input and output streams, allowing automated testing of
 command-line apps and graphical wrappers around them (or, in other words, simplifying the
 creation of testable interactive command-line user interfaces). If two methods did not
 use types from the `strategicprimer.model.common` module in their signatures, this
 interface would move to the `lovelace.util.common` package.

 # Using a CLI Helper

 Once you have an instance of an implementation of the [[ICLIHelper]] interface (see
 discussion of the [[CLIHelper]] class below), it offers several methods.

 ## Output

 The first, and simplest, methods it offers simply print strings to the output stream that
 it includes. The [[print()|ICLIHelper.print]] method prints it without modification; the
 [[println()|ICLIHelper.println]] method adds a newline, and allows you to call it without
 any argument to print just the newline.

 ## Input Primitives

 The next few methods allow you to ask the user for input and receive the user's response.
 Because these operate on input streams, an \"end-of-file\" condition is always a
 possibility, so all of these methods are declared as possibly returning [[null]], which
 they do if and only if the stream returns EOF.

 The simplest method is [[inputString()|ICLIHelper.inputString]], which takes a prompt as
 its sole parameter, prints it to the output stream (padding it with whitespace if it
 doesn't end with a whitespace character), reads a sigle line of input from the input
 stream, trims any leading or trailing whitespace from the input, and returns the trimmed
 input line.

 The [[inputNumber()|ICLIHelper.inputNumber]] and
 [[inputDecimal()|ICLIHelper.inputDecimal]] methods build on the
 [[inputString|ICLIHelper.inputString]] method, repeatedly prompting and reading input
 until the input is successfully parsed as a non-negative integer (in
 [[inputNumber|ICLIHelper.inputNumber]]) or non-negative decimal number (in
 [[inputDecimal|ICLIHelper.inputDecimal]]). Both methods abort, returning [[null]], on
 EOF.

 The [[inputBoolean()|ICLIHelper.inputBoolean]] method is somewhat similar. It repeatedly
 prompts the user until it can successfully parse the input as a boolean value. However,
 it goes beyond merely looking for \"true\" and \"false\"; it also accepts \"yes\",
 \"no\", and the first letter of any of the four words it looks for.

 ## Input Conveniences

 The remainder of the methods in the interface use the input primitives to simplify some
 common but complicated tasks callers had previously used them for.

 The simplest is the [[inputPoint()|ICLIHelper.inputPoint]] method, which prompts the user
 for a row and a column (both integers) and returns the
 [[strategicprimer.model.common.map::Point]] representing that row-column pair. If EOF
 occurs while reading either number, this method returns [[null]].

 ### [[inputBooleanInSeries|ICLIHelper.inputBooleanInSeries]]

 The [[inputBooleanInSeries()|ICLIHelper.inputBooleanInSeries]] method extends the
 [[inputBoolean()|ICLIHelper.inputBoolean]] method with two features: handling of \"quit\"
 and the notion of Boolean \"series\".

 First, the method takes a Callable parameter (a \"ternary predicate\") that is passed the
 user's input whenever it turns out to not be any of the words the method is specifically
 looking for. If this method (which by default always returns [[false]]) returns a Boolean
 value, the loop continues as normal. If it returns [[null]], however, that [[null]] is
 returned, just as if the input stream had reported an EOF.

 Second, the method takes a \"key\", by default the prompt. In addition to the inputs
 accepted by [[inputBoolean()|ICLIHelper.inputBoolean]], it accepts \"always\", \"ya\",
 \"never\", and \"na\". The former two cause the method to return [[true]], and the latter
 two [[false]], and then every subsequent time the method is called with the same key, it
 prints the prompt and immediately returns the same [[Boolean]] value as before.

 ### Choosing From Lists

 The [[chooseFromList|ICLIHelper.chooseFromList]] and
 [[chooseStringFromList|ICLIHelper.chooseStringFromList]] methods present a list of items
 to the user and ask him or her to choose one.
 [[chooseFromList|ICLIHelper.chooseFromList]] requires items in the list to satisfy the
 [[strategicprimer.model.common.map::HasName]] (when printing the list, it prints each
 item's [[name|strategicprimer.model.common.map::HasName.name]] field) and allows the list
 itself to be either a [[Sequential]] or a [[List]].
 [[chooseStringFromList|ICLIHelper.chooseStringFromList]] requires the list to be a
 [[Sequential]] of [[Strings|String]]. The two methods are otherwise identical in
 behavior.

 In addition to the first parameter, the list, and two prompts (a
 [[description|ICLIHelper.chooseFromList.description]] to print before printing the list
 and a [[prompt|ICLIHelper.chooseFromList.prompt]] to prompt the user with after printing
 the list), it takes [[a string to print if the list is
 empty|ICLIHelper.chooseFromList.none]] and [[a Boolean
 parameter|ICLIHelper.chooseFromList.auto]]. If that final parameter is [[true]] and the
 list contains exactly one item, the method returns that item without actually prompting
 the user.

 These methods return an [[Entry]] containing the index of the user's chosen item and the
 item itself. On EOF they return an index of -2 and an item of [[null]]. If the user
 enters an index greater than the last index in the list, that index is accepted (that is
 both the only way other than triggering an EOF to abort, and commonly used by apps to
 indicate that the user wants to add something to the list) but the item is [[null]].

 # Creating a CLI Helper

 At least once in an app that uses this interface, ideally *exactly* once, the code needs
 to create an object that satisfies it. To do that, create an instance of the
 [[CLIHelper]] class. It takes two parameters, the input and output streams to read from
 and write to. The input stream must be a method reference to a method that takes no
 parameters and returns an object of a type no wider than the union of [[String]] and
 [[null]]; by default [[process.readLine]] is used. The output stream must be a method
 reference to a method that takes a single [[String]] as its parameter; by default,
 [[process.write]] is used."
// TODO: cover Applet and AppletChooser as well
shared package strategicprimer.drivers.common.cli;
