"Possible numbers of (non-option) parameters a driver might shared want."
shared class ParamCount of none | one | two | atLeastOne | atLeastTwo | anyNumber {
    "None at all."
    shared new none { }

    "Exactly one."
    shared new one { }

    "Exactly two."
    shared new two { }

    "One or more."
    shared new atLeastOne { }

    "Two or more."
    shared new atLeastTwo { }

    "Zero or more."
    shared new anyNumber { }
}
