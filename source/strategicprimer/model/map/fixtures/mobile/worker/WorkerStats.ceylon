"A class representing a worker's core statistical attributes."
shared class WorkerStats {
    "The basis of stats: every two points more than this is worth +1, and every two points
     less is worth -1."
    static Integer statBasis = 10;
    "The modifier for (effect of) the given stat value: (stat - 10) / 2, always rounding
     down."
    shared static Integer getModifier(Integer stat) => stat / 2 - statBasis / 2;
    "The modifier string for a stat with the given value."
    shared static String getModifierString(Integer stat) {
        Integer modifier = getModifier(stat);
        return (modifier >= 0) then "+``modifier``" else modifier.string;
    }
    "The worker's health."
    shared Integer hitPoints;
    "The worker's max health."
    shared Integer maxHitPoints;
    "The worker's strength."
    shared Integer strength;
    "The worker's dexterity."
    shared Integer dexterity;
    "The worker's constitution."
    shared Integer constitution;
    "The worker's intelligence."
    shared Integer intelligence;
    "The worker's wisdom."
    shared Integer wisdom;
    "The worker's charisma."
    shared Integer charisma;
    "Main constructor, taking all the stats."
    shared new (Integer hp, Integer maxHP, Integer str, Integer dex, Integer con,
            Integer int, Integer wis, Integer cha) {
        hitPoints = hp;
        maxHitPoints = maxHP;
        strength = str;
        dexterity = dex;
        constitution = con;
        intelligence = int;
        wisdom = wis;
        charisma = cha;
    }
    "Constructor for making a stat-adjustments version, so omitting HP."
    shared new factory(Integer str, Integer dex, Integer con,
            Integer int, Integer wis, Integer cha) extends WorkerStats(0, 0, str, dex,
        con, int, wis, cha) {}
    "Takes an existing set of stats and a set of adjustments to produce an adjusted set."
    shared new adjusted(
            "The number to use for [[hitPoints]] and [[maxHitPoints]]"
            Integer hp,
            "A set of base stats to use for the other stats."
            WorkerStats base,
            "A set of adjustments to add to those stats."
            WorkerStats adjustment) extends WorkerStats(hp, hp,
		        base.strength + adjustment.strength, base.dexterity + adjustment.dexterity,
		        base.constitution + adjustment.constitution,
		        base.intelligence + adjustment.intelligence, base.wisdom + adjustment.wisdom,
		        base.charisma + adjustment.charisma) {}
    "Given an RNG, produce a random set of stats, with HP set to 0."
    shared new random(Integer() rng)
            extends factory(rng(), rng(), rng(), rng(), rng(), rng()) {}
    "Clone the object."
    shared WorkerStats copy() => WorkerStats(hitPoints, maxHitPoints, strength, dexterity,
        constitution, intelligence, wisdom, charisma);
    shared actual Boolean equals(Object obj) {
        if (is WorkerStats obj) {
            return hitPoints == obj.hitPoints && maxHitPoints == obj.maxHitPoints &&
                strength == obj.strength && dexterity == obj.dexterity &&
                constitution == obj.constitution && intelligence == obj.intelligence &&
                wisdom == obj.wisdom && charisma == obj.charisma;
        } else {
            return false;
        }
    }
    shared actual Integer hash => hitPoints + maxHitPoints.leftLogicalShift(3) +
            strength.leftLogicalShift(6) + dexterity.leftLogicalShift(9) +
            constitution.leftLogicalShift(12) + intelligence.leftLogicalShift(15) +
            wisdom.leftLogicalShift(18) + charisma.leftLogicalShift(21);
    shared actual String string =>
            "HP: ``hitPoints`` / ``maxHitPoints``
             Str: ``strength``
             Dex: ``dexterity``
             Con: ``constitution``
             Int: ``intelligence``
             Wis: ``wisdom``
             Cha: ``charisma``
             ";
    shared Integer[6] array =>
            [strength, dexterity, constitution, intelligence, wisdom, charisma];
}
