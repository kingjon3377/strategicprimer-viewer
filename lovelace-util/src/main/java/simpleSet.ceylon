"A wrapper around [[ceylon.language::set]] that won't cause the compiler to make an inner
 class in each and every caller."
shared Set<Element> simpleSet<Element>(Element* elements) given Element satisfies Object
        => set(elements);
