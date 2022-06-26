## Strategic Primer Assistive Programs

[![GitHub release](https://img.shields.io/github/release/kingjon3377/strategicprimer-viewer.svg?label=stable&style=plastic) ![GitHub Release Date](https://img.shields.io/github/release-date/kingjon3377/strategicprimer-viewer.svg?style=plastic)](https://github.com/kingjon3377/strategicprimer-viewer/releases/latest)
[![GitHub release](https://img.shields.io/github/release-pre/kingjon3377/strategicprimer-viewer.svg?label=latest&style=plastic) ![GitHub (Pre-)Release Date](https://img.shields.io/github/release-date-pre/kingjon3377/strategicprimer-viewer.svg?style=plastic)](https://github.com/kingjon3377/strategicprimer-viewer/releases)

This repository contains a suite of assistive programs for players and Judges
of the strategy game
[Strategic Primer](https://strategicprimer.wordpress.com).
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

Pre-compiled binaries are available from the [GitHub releases
page](https://github.com/kingjon/strategicprimer-viewer/releases). These
include Windows executables, (archives containing) Mac "application"
bundles, and cross-platform "Java archive" (JAR) files.

### Development

As the campaign this suite was developed to support has come to a close, after
version 0.4.9019 development efforts will turn to supporting a major overhaul
of the design of the game, in preparation for possible future campaigns and/or
eventual publication, and will be even more intermittent than usual. Version
0.4.9019 has been released to ensure players in the 2009-2022 campaign can
review their final results if they desire, and point releases may be released
in that series to fix any bugs that are reported, but future versions (probably
starting with 0.5.0 to indicate the severity of the break) will certainly be
significantly incompatible with past versions.

Past development activities have been recorded in "monthly" development reports
on [the game's
blog](https://strategicprimer.wordpress.com/category/development-reports) and, before that, 
[on the author's main blog](https://shinecycle.wordpress.com/tag/assistive/).
Interested developers or users can help by reporting issues, requesting
features, or contributing patches; discussion of the game itself, rather than
the assistive programs that will support it, is welcome on [the
list](https://groups.io/g/strategicprimer) or in private communications.

Changes to this project are automatically tested using Github Actions continuous
integration: [![Build
Status](https://github.com/kingjon3377/strategicprimer-viewer/actions/workflows/build.yml/badge.svg)
](https://github.com/kingjon3377/strategicprimer-viewer/actions/workflows/build.yml)

### Dependencies and Building from Source

For building from source for local use, the only dependency that needs to be
installed is [Maven](https://maven.apache.org/); run `mvn compile` from the
directory containing this README file.

To build packages (including JAR files, due to the limitations of Maven and the
developer's inexperience with advanced features of that ecosystem), a few more
dependencies must also be installed:

- The `convert` command from [ImageMagick](https://www.imagemagick.org) (or,
  conceivably, [GraphicsMagick](https://www.graphicsmagick.org))
- The `png2icns` command provided by [libicns](https://sourceforge.net/projects/icns).
- A `mkisofs` command, typically provided by the
  [cdrtools](https://sourceforge.net/projects/cdrtools)
- `tar` built with support for `bzip2` compression.

### Running the Program

If you have an EXE or `.app`, it should behave like a standard platform-native
application. If you have a JAR, if file associations are properly set up you
can double-click it, but if you want to run any of the apps other than the map
viewer, the worker-management app, or the exploration GUI, you'll need to call
it from the command line: `java -jar /path/to/viewer-0.4.${version}.jar
subcommand [-options] /path/to/map.xml` .

If you've compiled the apps from source, and haven't built a JAR, you'll need
to set the classpath appropriately and ensure that your PATH points to a
sufficiently recent version of Java, then execute `java drivers.Main`.
