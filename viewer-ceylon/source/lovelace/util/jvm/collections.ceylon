import ceylon.collection {
    ArrayList
}
import ceylon.math.float {
    random
}
"Return the elements of a list (etc.) in random order, like Java's Collections.shuffle."
by("gdejohn at https://stackoverflow.com/questions/20486670")
shared Element[] shuffle<Element>({Element*} elements, Float() randomizer = random) {
    value shuffled = ArrayList { elements = elements; };
    for (index->element in shuffled.indexed) {
        value randomIndex = (randomizer() * (index +1)).integer;
        if (randomIndex != index) {
            assert (exists randomElement = shuffled[randomIndex]);
            shuffled.set(index, randomElement);
            shuffled.set(randomIndex, element);
        }
    }
    return shuffled.size == 0 then [] else [*shuffled];
}