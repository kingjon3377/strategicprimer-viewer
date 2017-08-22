## Strategic Primer Assistive Programs

This repository contains a suite of assistive programs for players and Judges
of the strategy game
[Strategic Primer](https://shinecycle.wordpress.com/archives/strategic-primer).
The flagship application is a map viewer; players may also find the
worker-management app useful. Other programs are primarily designed for use by
the Judge in maintaining the master map.

### Features

The viewer reads and writes a custom XML-based map format that (except for
"seeing the forest among the trees") is also quite human-readable (for those of
a somewhat technical background). Maps contain a number of tiles of various
terrain types, each of which can contain any number of various kinds of
"fixtures," including units, fortresses, animals, orchards, and dragons, among
many others.

### Getting the Program

Old pre-compiled binaries are available from the
[BitBucket downloads
page](https://bitbucket.org/kingjon/strategicprimer-viewer/downloads), and more
up-to-date ones are available from the [GitHub releases
page](https://github.com/kingjon/strategicprimer-viewer/releases). These
include Windows executables, (archives containing) Mac "application"
bundles, and cross-platform "Java archive" (JAR) files.

### Development

The viewer is under active development, as you can see by reading [the monthly
development reports on the author's blog](
https://shinecycle.wordpress.com/tag/assistive/) or examining the commits in
this repository.  Interested developers or users can help by reporting issues,
requesting features, or contributing patches.

Changes to the project are automatically tested using Travis Continuous
Integration and checked using Codebeat and Codacy static analysis: [![Build
Status](https://travis-ci.org/kingjon3377/strategicprimer-viewer.svg?branch=master)
](https://travis-ci.org/kingjon3377/strategicprimer-viewer) [![codebeat
badge](https://codebeat.co/badges/ab6b17f0-cc69-44ab-9d94-a8d5fb40e613)
](https://codebeat.co/projects/github-com-kingjon3377-strategicprimer-viewer)
[![Codacy
Badge](https://api.codacy.com/project/badge/Grade/f8855a279a494e4eb532ee94e37f1ac8)
](https://www.codacy.com/app/kingjon3377/strategicprimer-viewer)

### Dependencies and Building from Source

To build the apps from source, a few dependencies need to be installed:

- A working [Ceylon](https://ceylon-lang.org) compiler, version 1.3.3 or later.
- A Java development kit compatible with Java 8
- The "Window menu" implementation by "mickleness." Until [his Pumpernickel
  project](https://github.com/mickleness/pumpernickel) makes a release onto
  Maven, download [the JAR he committed into his
  repository](https://github.com/mickleness/pumpernickel/raw/master/pump-release/com/pump/pump-swing/1.0.00/pump-swing-1.0.00.jar)
  and import it into Ceylon as `com.pump.swing/1.0.00` using `ceylon
  import-jar`.

With those, you should be able to build the project with `ceylon compile` and
run it with `ceylon run strategicprimer.viewer`.

To reproduce my process of building a release, however, and to run the tests
more easily (since simply running `ceylon test` runs into [a bug in the Ceylon
runtime](https://github.com/ceylon/ceylon/issues/6986)), you should install the
following additional dependencies:

- [Apache Ant](http://ant.apache.org/)
- [The JarBundler Ant task](https://github.com/UltraMixer/JarBundler)
- To build any platform-native app, to render the icon into the various sizes
  that the various tools want, you need
  [ImageMagick](http://www.imagemagick.org/) (or, conceivably,
  [GraphicsMagick](http://www.graphicsmagick.org/); we use the `convert`
  command).
- To build the Windows `.exe`, you need
  [Launch4J](https://sourceforge.net/projects/launch4j/) and the `icotool`
  command provided by [icoutils](http://www.nongnu.org/icoutils/).
- To build the Mac `.app`, you need [the "universal Java application
  stub"](https://github.com/tofi86/universalJavaApplicationStub) and the
  `png2icns` command provided by
  [libicns](https://sourceforge.net/projects/icns/)
- To build the Mac DMG, in addition to what's needed for the `.app`, you need a
  `mkisofs` command; this is typically provided by the
  [`cdrtools`](https://sourceforge.net/projects/cdrtools/) package.

(`tar`, `bzip`, and the like should already be installed on any system I can
imagine trying to build a release from.)

Once you have these dependencies installed, use Ant to build the software.
There are several build targets in the build script (which is `build.xml`, the
file Ant looks to by default):

- `clean` removes any compiled code from prior builds.
- `cleanall` calls `clean`, and additionally removes any generated
  documentation HTML files.
- `build` compiles the software, delegating to `ceylon compile`.
- `doc` generates HTML documentation from the source---or will once that Ceylon
  runtime bug I mentioned is fixed
- `test` runs all the tests that are in modules that are known not to cause
  `ceylon test` to crash.
- `jar` packages the entire compiled code, and all runtime dependencies, into a
  JAR.
- `exe` turns that JAR into a Windows-native executable.
- `app` packages the JAR into a Mac OS application (which it additionally
  packages into a tarball, since a `.app` is a directory with its contents
  arranged a particular way, so it can be distributed over the Internet).
- `dmg` packages that `.app` Mac application into a Mac DMG.
- `dist` packages up the source, compiled modules, dependencies, and just about
  everything else (but not the JAR or either of the "native" apps) into a
  tarball.
- `release` is a convenience target: it calls `dmg`, `exe`, and `dist`.

### Running the Program

If you have an EXE or `.app`, it should behave like a standard platform-native
application. If you have a JAR, if file associations are properly set up you
can double-click it, but if you want to run any of the apps other than the map
viewer, the worker-management app, or the exploration GUI, you'll need to call
it from the command line: `java -jar /path/to/viewer-0.4.${version}.jar
-options /path/to/map.xml` .

If you've compiled the apps from source, there's a simpler way: `ceylon run
strategicprimer.viewer -options /path/to/map.xml`. (If one of the options
happens to be one that the Ceylon runtime itself uses, pass `--` somewhere
before that.)
