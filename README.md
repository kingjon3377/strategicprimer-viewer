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

### Dependencies

To build the apps from source, several dependencies need to be installed:

- A Java development kit compatible with Java 8
- [Apache Ant](http://ant.apache.org/)
- [JUnit](http://junit.org/) version 4 or later (and its dependency the
  [Hamcrest](https://hamcrest.org/JavaHamcrest/) library, the "core" JAR
  version 1.3 or later)
- The "nullness annotations" provided by [Eclipse](http://www.eclipse.org); if
  you don't have Eclipse installed and don't want to install the full IDE, you
  can download the one needed JAR from Maven Central ([direct
  link](https://central.maven.org/maven2/org/eclipse/jdt/org.eclipse.jdt.annotation/2.0.0/org.eclipse.jdt.annotation-2.0.0.jar)
- The "Window menu" implementation by "mickleness." Until [his Pumpernickel
  project](https://github.com/mickleness/pumpernickel) makes a release onto
  Maven, download [the JAR he committed into his
  repository](https://github.com/mickleness/pumpernickel/raw/master/pump-release/com/pump/pump-swing/1.0.00/pump-swing-1.0.00.jar)
- [The JarBundler Ant task](https://github.com/UltraMixer/JarBundler)
- On platforms other than Mac, the ["Orange
  Extensions"](http://ymasory.github.com/OrangeExtensions/) (and even on Mac it
  can't hurt, without it you may need to pass *some* JAR in its place)
- To generate test-coverage statistics, you need the
  [JaCoCo](http://jacoco.org) code coverage library (and any dependencies it has)

To package the JAR into platform-native apps, you need several other tools:

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

(To do the full packaging I do, you also need `tar` and `bzip2`, of course.)

### Building from Source

Once you have the necessary dependencies installed, use Ant to build the
software. There are several build targets in the build script (which is
`build.xml`, the file Ant looks to by default):

- `clean` removes any Java class files from prior builds.
- `cleanall` calls `clean`, and additionally removes any generated JavaDoc HTML
  files and any log files from running the tests.
- `build` compiles the software, and copies any resources that need to be on
  the classpath into the same directory tree. Once it completes, you can run
  the software by adding `bin` to your classpath and invoking the right class
  on the command line.
- `doc` generates JavaDoc HTML files from the source.
- `test` runs the tests.
- `check` runs static analysis tools: at the moment, just
  [CheckStyle](https://github.com/checkstyle/checkstyle). (And I'm not sure
  whether this task actually works with our current setup.)
- `jar` packages the binaries and resources from the `build` task (and also the
  Window-menu library) into a JAR.
- `exe` turns that JAR into a Windows-native executable.
- `app` packages the JAR into a Mac OS application (which it additionally
  packages into a tarball, since a `.app` is a directory with its contents
  arranged a particular way, so it can be distributed over the Internet).
- `dmg` packages that `.app` Mac application into a Mac DMG
- `dist` packages up the source, compiled class files, and just about
  everything else (but not the JAR or either of the "native" apps) into a
  tarball.
- `release` is a convenience target: it calls `dmg`, `exe`, and `dist`.
- `coverage` is supposed to run the test under JaCoCo to measure how much of
  the code-base the tests "exercise"; however, it's currently disabled because
  of [a bug in the compiler](https://bugs.openjdk.java.net/browse/JDK-8144185)
