import strategicprimer.model.common.map.fixtures {
    Quantity
}
"Models of (game statistics for) herding small animals that don't produce any resources
 on a daily basis."
shared class SmallAnimalModel satisfies HerdModel {
    "These animals don't actually produce any resources."
    shared actual Quantity productionPerHead => Quantity(0, "");
    "Since these animals don't produce any resources, the coefficient is said to be zero."
    shared actual Float poundsCoefficient => 0.0;
    "How much time, in minutes, herders must spend if there are this many animals."
    shared actual Integer dailyTimePerHead;

    "A description of the model to show the user."
    shared actual String name;

    """How many turns, at most, should elapse between "extra chores" days."""
    shared Integer extraChoresInterval;

    """How much time, in minutes, must be spent per head on "extra chores" days."""
    shared Integer extraTimePerHead = 30;

    "How much time, in minutes, must be spent on the entire herd or flock each turn,
     regardless of its size, in addition to each herder's time with individual animals."
    shared Integer dailyTimeFloor = 20;

    new delegate (Integer timePerHead, String nomen, Integer extraInterval) {
        name = nomen;
        dailyTimePerHead = timePerHead;
        extraChoresInterval = extraInterval;
    }

    shared new rabbits extends delegate(10, "Rabbits", 4) {}

    shared actual Integer dailyTime(Integer heads) =>
        heads * dailyTimePerHead + dailyTimeFloor;



}
