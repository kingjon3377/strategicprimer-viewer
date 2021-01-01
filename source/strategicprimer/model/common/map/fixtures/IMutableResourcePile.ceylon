import strategicprimer.model.common.map {
    HasMutableImage
}

shared interface IMutableResourcePile satisfies IResourcePile&HasMutableImage {
    "What specific kind of thing is in the resource pile."
    shared actual formal variable String contents;

    "How much of that thing is in the pile, including units."
    shared actual formal variable Quantity quantity;

    "The turn on which the resource was created."
    shared actual formal variable Integer created;

    "Clone the object."
    shared actual formal IMutableResourcePile copy(Boolean zero);
}
