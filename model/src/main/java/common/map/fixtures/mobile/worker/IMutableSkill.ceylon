"Extension of [[ISkill]] adding mutator methods."
shared interface IMutableSkill satisfies ISkill {
    "Add hours of training or experience."
    shared formal void addHours(
            "The number of hours to add."
            Integer hours,
            "If less than or equal to the total number of hours after the addition, level
             up and zero the hours instead."
            Integer condition);
}
