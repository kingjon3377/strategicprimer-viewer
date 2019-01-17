import javax.swing {
    JLabel
}

"A JLabel that takes a String-interpolation function to produce its text."
shared class InterpolatedLabel<Args> extends JLabel
        given Args satisfies Anything[] {
    Callable<String, Args> formatter;
    variable Args args;
    shared new (
            "The function to produce the label's text."
            Callable<String, Args> formatter,
            "The arguments to pass to [[formatter]] to produce the label's
             initial text."
            Args defaultArguments) extends JLabel(formatter(*defaultArguments)) {
        this.formatter = formatter;
        args = defaultArguments;
    }
    "The last set of arguments passed to the label. (Ceylon does not support
     write-only fields.)"
    shared Args arguments => args;
    "Change the arguments and regenerate the label's text."
    assign arguments {
        args = arguments;
        text = formatter(*arguments);
    }
}
