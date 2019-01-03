"This module is a collection of utility methods, classes, and objects that either require
 Java-only types in their signatures or AWT or Swing in their implementation.

 One large category of classes and methods in this module is \"common controls\",
 extensions to the AWT/Swing widget set.

 - The [[FileChooser]] class is a wrapper around the [[Swing|javax.swing::JFileChooser]]
   and [[AWT|java.awt::FileDialog]] file-choosers. On most platforms, the Swing
   file-chooser is close enough to the native widget in appearance and functionality; on
   macOS it is decidedly *not*, so to conform to the platform's HIG we must use the AWT
   equivalent. This class leaves the choice of which one to use to its callers, but
   abstracts over the differences between them.
 - The [[listenedButton]] method constructs a [[javax.swing::JButton]] and adds
   [[listeners|java.awt.event::ActionListener]] to it in (as far as callers are concerned)
   one operation.
 - The [[showErrorDialog]] method shows an error dialog to the user; it's a trivial
   wrapper around [[javax.swing::JOptionPane.showMessageDialog]].
 - The [[FunctionalGroupLayout]] class is an extension to the [[javax.swing::GroupLayout]]
   class that adds additional methods to make initialization of a component using the
   layout less verbose and more functional in style.
 - The [[ImprovedComboBox]] class extends [[javax.swing::JComboBox]] to make pressing the
   Tab key do what one expects: if the pop-up list is visible, treat it like the user had
   pressed Enter as well as changing focus.
 - The [[StreamingLabel]] is a label (actually now a [[javax.swing::JEditorPane]]) that
   can easily be written (appended) to. The [[LabelTextColor]] class enumerates colors for
   use with it.
 - The [[BoxPanel]] interface, instances of which must be created using the [[boxPanel]]
   method (or methods which call it, such as the one described below), is a
   [[panel|javax.swing::JPanel]] laid out by a [[javax.swing::BoxLayout]] and providing
   helper methods for adding elastic and rigid areas between components.
 - The [[centeredHorizontalBox]] method creates a [[BoxPanel]] laid out on the line axis,
   with an elastic area at each end and a small (currently two-pixel) rigid area between
   each component and the next.
 - The [[BorderedPanel]] is a [[panel|javax.swing|JPanel]] laid out by a
   [[java.awt::BorderLayout]], with helper methods (or rather attributes) to assign
   components to its different sectors. (This especially helpful because the layout
   requires [[java.lang::String]] sector-identifiers, which get spuriously coverted to the
   [[ceylon.language::String]] type, causing runtime failures unless converted back via
   [[java.lang::Types.nativeString]].)
 - The [[verticalSplit]] and [[horizontalSplit]] methods are simple wrappers around the
   [[javax.swing::JSplitPane]] constructor that don't require the caller to remember
   whether [[true]] means a horizontal or a vertical split and also take the divider
   location and resize weight as parameters in the same operation as the two components
   it contains.
 - The [[createHotKey]] method sets up a hot-key for an action that doesn't call a *menu*
   item.
 - The [[createAccelerator]] method creates a [[javax.swing::KeyStroke]] representing a
   hot-key accelerator, and takes the desired modifiers using the [[HotKeyModifier]]
   enumerated type.
 - The [[createMenuItem]] creates a menu item, taking its text, mnemonic key, description
   for accessibility software, listener, and accelerators in one operation.
 - The [[InterpolatedLabel]] class is a [[javax.swing::JLabel]] that takes a
   String-interpolation function to produce its text.
 - The [[ActionWrapper]] class is a wrapper around an [[java.awt.event::ActionListener]]
   (or equivalent Ceylon [[Callable]]) that extends [[javax.swing::AbstractAction]], for
   the exceedingly common case of a JDK method taking an [[javax.swing::Action]] as a
   parameter when we don't need more functionality than a single method accepting an
   [[java.awt.event::ActionEvent]].
 - The [[ReorderableListModel]] class extends the [[javax.swing::DefaultListModel]] class
   to implement the [[lovelace.util.common::Reorderable]] interface.
 - The [[ListModelWrapper]] class wraps a [[javax.swing::ListModel]] to meet the Ceylon
   [[List]] interface.

 - The [[platform]] object encapsulates a number of utility constants and functions that
   differ between Mac and non-Mac platforms, including the usual shortcut-key modifier as
   two constants (the numeric mask and a descriptive String) and a method to test whether
   it is pressed in an event, and a method to make a series of buttons segmented on the
   Mac platform (which is a no-op on other platforms).
 - The [[decimalize]] function takes a number (as of this writing anything other than an
   [[Integer]], [[Float]], [[ceylon.whole::Whole]], or [[ceylon.decimal::Decimal]] will
   cause an assertion failure, and a floating-point number cannot be infinite or `NaN`)
   and, if it is not already a [[ceylon.decimal::Decimal]], gets a representation of it
   in that type and returns it. This is an extension of [[ceylon.decimal::decimalNumber]]
   to accept `Decimal` numbers, and nominally any [[Number]], as input.
 - The [[ComponentParentStream]] class is a [[stream|Iterable]] of a given component and
   its [[ancestors|java.awt::Component.parent]] until a component's parent is [[null]]. If
   a component is a [[javax.swing::JPopupMenu]] and its parent is [[null]], the component
   that invoked it is added to the stream instead; if a component is a
   [[javax.swing::JMenu]] and its parent is null, its
   [[popupMenu|javax.swing::JMenu.popupMenu]] member is added to the stream instead.
 - The [[ConvertingIterable]] class wraps a Java [[java.lang::Iterable]] or
   [[java.util::Iterator]], of any or no generic type, into a Ceylon
   [[ceylon.language::Iterable]], asserting that each element it produces is of the
   desired type.
 - Similarly, the [[EnumerationWrapper]] class wraps a Java [[java.util::Enumeration]];
   the way it asserts that each item returned is of the desired type instead of requiring
   the wrapped enumeration to be declared with the proper generic type is even more
   essential here, because in practice APIs that use Enumeration rather than
   [[java.lang::Iterable]] never parameterize it.
 - The [[IntTransferable]] class is an implementation of the
   [[java.awt.datatransfer::Transferable]] interface that transfers a single [[Integer]].
 - The [[TypesafeXMLEventReader]] class is a wrapper around an
   [[javax.xml.stream::XMLEventReader]] (which it will produce itself if the caller passes
   in a [[java.io::Reader]] instead) that satisfies the Ceylon [[Iterable]] interface, and
   also closes the provided reader (if it is [[java.io::Closeable]]) before returning
   [[finished]] for the first time. Callers can also pass in additional methods to call
   before first returning [[finished]]."
license("GPL-3")
native("jvm")
module lovelace.util.jvm "0.1.1" {
    value ceylonVersion = "1.3.3";
    value javaVersion = "8";
    import ceylon.collection ceylonVersion;
    shared import java.base javaVersion;
    shared import java.desktop javaVersion;
    shared import lovelace.util.common "0.1.1";
    import ceylon.interop.java ceylonVersion;
    // TODO: Uncomment tests once Ceylon bug #6986 is fixed
//    import ceylon.test ceylonVersion;
    shared import javax.xml javaVersion;
    import ceylon.logging ceylonVersion;
    shared import ceylon.buffer ceylonVersion;
    shared import ceylon.decimal ceylonVersion;
    import ceylon.whole ceylonVersion;
}
