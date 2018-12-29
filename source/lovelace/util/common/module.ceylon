"This is a collection of utility functions, annotations, and objects that don't require
 any Java-only types in their signatures or (eclipse/ceylon#6986) Java AWT or Swing in
 their implementation.

 First of all, there are a few annotations:

 - [[randomlyGenerated]] is an annotation to apply to a parameter of a test method; it
   supplies the test method with random numbers (currently it supports [[Integer]],
   [[Float]], and [[ceylon.whole::Whole]] parameters, and on the JVM additionally
   [[ceylon.decimal::Decimal]] numbers).
 - The [[enumeratedParameter]] annotation is also one applied to parameters of test
   methods; it takes a metamodel reference to a type with enumerated case values, and
   supplies the test method with those values.
 - [[todo]] is an annotation to document TODO items. At present it has no actual effect;
   it is essentially a comment that an IDE's \"find references\" feature can search on.

 There are a number of miscellaneous top-level classes, objects, and methods:

 - [[singletonRandom]] is a [[ceylon.random::Random]] instance shared by an entire
   application.
 - [[readFileContents]] reads the contents of a file into a String. If `ceylon.file`
   supports the platform on which the code is running and the provided filename denotes a
   file that exists in the filesystem, its contents are read and returned. If
   `ceylon.file` is not supported or the file does not exist in the filesystem, but the
   file is present as a resource in the provided module, its contents are returned. If the
   file is not found in either place, [[null]] is returned.
 - The [[TypeStream]] class is a [[stream|Iterable]] of all the
   [[types|ceylon.language.meta.model::ClassOrInterface]] that a given object satisfies.
   It is primarily useful for cases where a caller uses a [[Map]] by type to sort objects,
   but the objects may be subclasses of the types it is concerned with.
 - [[whichDiffer]] takes a [[stream|Iterable]] of pairs (two-element [[tuples|Tuple]]),
   and prints (using a provided method reference) which of them is the first in which the
   two elements are not equal to each other. It is primarily intended for use in
   \"printf() debugging\".
 - [[silentListener]] converts a no-argument method reference to one taking a single
   argument of a given type. In my code, it is primarily used for AWT/Swing event
   listeners that don't actually use the event object.
 - The [[assertAny]] function is the converse of [[ceylon.test::assertAll]]: it is given a
   series of lambdas that may contain assertions, and it returns without error so long as
   at least *one* of the provided methods does so (and none of the others throws an
   exception other than [[AssertionError]]). If *all* of the methods fail with
   `AssertionError`s, it throws a [[ceylon.test.engine::MultipleFailureException]]
   wrapping those errors.
 - [[anythingEqual]] is a wrapper around [[Object.equals]] that takes [[Anything]] as its
   argument, so as to not require its callers to check them against [[null]] first.
 - [[entryBy]] is a function to produce a function: given two accessor method references
   on a base type, it produces a function from that base type to an [[Entry]] using those
   accessors to produce its key and item.
 - [[isNumeric]] tests whether a [[String]] represents an integer, and [[parseInt]]
   parses an [[Integer]] from a [[String]]. On the JVM platform, the string may contain
   commas (if that feature is not desired, just use [[Integer.parse]]!)
 - The [[as]] function takes a value and a type; if the given value is of the given type,
   it returns it *as* that type, and if not it returns [[null]].
 - Given a method reference and arguments matching its signature, the [[defer]] function
   produces a no-argument method that invokes the provided function with the provided
   arguments.
 - Given a no-argument method reference, the [[invoke]] method invokes it immediately.
   This is primarily intended to be used with [[Iterable.each]] on streams of lambdas.
 - [[numberComparator]] is a [[Comparator]] for [[Numbers|Number]] of unknown or varied
   types. In the default implementation, [[Integer]], [[Float]], and
   [[ceylon.whole::Whole]] numbers are supported; on the JVM, [[ceylon.decimal::Decimal]]
   number are also supported.
 - [[comparingOn]] is sort of a sepcialized [[compose]] for comparators. Given an accessor
   method reference for a base type, and a function to compare two objects of the field
   type, produces a function comparing two objects of the base type on the given field.
 - An [[Accumulator]] holds a mutable value (of any [[Summable]] type), accepts
   modifications to it (by addition), and reports its current value. The sole
   implementation of the [[Accumulator]] interface in this module is the [[IntHolder]]
   class, which is specialized for [[Integer]] counts.
 - A [[DelayedRemovalMap]] is a [[ceylon.collection::MutableMap]] that only actually
   executes the removal of elements when its [[coalesce|DelayedRemovalMap.coalesce]]
   method is called, to avoid concurrent-modification errors. The [[IntMap]] class is the
   only implementation of the [[DelayedRemovalMap]] interface in this module, and is
   specialized for [[Integer]] keys.
 - The [[Comparator]] interface (which will probably be removed soon, as
   [[numberComparator]] is its sole use, and doesn't need the interface) specifies a
   [[Comparator.compare]] method comparing two objects of a specified type.
 - The [[Reorderable]] interface describes list-like collections that can be reordered;
   its [[reorder|Reorderable.reorder]] method moves an item from one index to another.
 - The [[ArraySet]] class is a [[Set]] backed by an [[ceylon.collection::ArrayList]], for
   performance reasons.
 - The [[IteratorWrapper]] class is a trivial wrapper around an [[Iterator]] to satisfy
   the [[Iterable]], always returning the same object in its
   [[iterator|IteratorWrapper.iterator]] method. This is most often used to allow an
   iterator to be iterated in for-each loops.
 - The [[NonNullCorrespondence]] interface is a partial specialization of the
   [[Correspondence]] interface that guarantees it won't return [[null]] unless its `Item`
   type includes it.
 - The [[simpleSet]] and [[simpleMap]] functions are trivial wrappers around the
   [[set]] and [[map]] functions (from `ceylon.language`) that take the initial elements
   as a variadic argument instead of as an Iterable; in my testing, I found indications
   that using the `ceylon.language` methods caused the compiler to produce an inner class
   in each and every caller.
 - The [[EnumCounter]] class is intended to count references to enumerated objects,
   though it does ot do any sort of check that its type parameter is an enumerated type.
   For every object ever passed to its [[count|EnumCounter.count]] or
   [[countMany|EnumCounter.countMany]] methods, it keeps a running total of the number of
   times it has been passed that object.
 - Given a function and an expected value, [[matchingValue]] produces a predicate that
   applies the function to its own argument and returns true if and only if it produces
   the expected value. It is intended to be used with [[Iterable.filter]] and the like.
 - The [[narrowedStream]] function turns a stream of [[entries|Entry]] into a stream of
   entries narrowed to the given type parameters. This is necessary because [[Entry]]
   does not have the special handling to set its type parameters to the objects' precise
   types that [[Tuple]] does, and so calling [[Iterable.narrow]] on a stream of Entries,
   unless with a type parameter that completely covers their nominal type, always returns
   the empty stream.
 - [[cliHelper]] is an encapsulation of a collection of methods to help a command-line
   interface interact with the user. Unlike the \"heavier\" version in the
   `strategicprimer.drivers.common` module (which I actually use), all of its methods
   rely exclusively on [[ceylon.language::print]] for output and
   [[ceylon.language::process.readLine]] for input.
 - [[entryMap]] is a helper method for using [[Iterable.map]] with [[entries|Entry]].
   Given two functions, it returns a function that takes an entry and applies the two
   functions to its key and item to produce a new entry, which it returns.

 Some of the members of this package are minimally functional replacements for
 JVM-specific types, or SDK types that don't quite meet my needs:

 - The [[MalformedXMLException]] class is used where I used to use
   [[javax.xml.stream::XMLStreamException]], and most often wraps it; I would use
   [[ParseException]] in such cases in XML-reading code, but it doesn't allow us to set
   the underlying platform-native exception as its cause.
 - The [[MissingFileException]] class is used to indicate that a file that was supposed to
   be opened was not present, and is intended to wrap and replace platform-specific
   exceptions so only the very lowest level of code I write has to explicitly depend on
   the platform-specific module.
 - The [[PathWrapper]] class is a wrapper around a filename, replacing
   [[ceylon.file::Path]] until the Ceylon SDK makes that module nominally-cross-platform."
suppressWarnings("doclink")
by("Jonathan Lovelace")
license("GPL-3")
todo("Once Ceylon bug #6986 is fixed, combine with `lovelace.util.jvm` in one module,
      with only the parts requiring the JVM marked as `native`.",
     "After that, and once things have stabilized somewhat, move to a separate repository
      and perhaps publish to the Herd.")
module lovelace.util.common "0.1.0" {
    value ceylonVersion = "1.3.3";
    shared import ceylon.collection ceylonVersion;
    shared import ceylon.test ceylonVersion;
    import ceylon.whole ceylonVersion;
    native("jvm")
    import ceylon.decimal ceylonVersion;
    shared import ceylon.random ceylonVersion;
    native("jvm") import java.base "8";
    import ceylon.logging ceylonVersion;
    native("jvm") import ceylon.file ceylonVersion;
}
