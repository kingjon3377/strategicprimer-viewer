import lovelace.util.common {
    todo
}

"An interface for model elements that have images that can be used to represent them."
shared interface HasImage {
    """The filename of an image to use as an icon if the individual fixture doesn't
       specify a different image (i.e. if [[image]] is empty. This should be constant over
       an instance's lifetime, and with a few exceptions should be constant for all
       instances of a class."""
    todo("Replace this with a centralized registry")
    shared formal String defaultImage;

    "The filename of an image to use as an icon for this particular instance."
    shared formal String image;
}
