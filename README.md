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
up-to-date ones will be available from the [GitHub releases
page](https://github.com/kingjon/strategicprimer-viewer/releases). These
include Windows executables, (archives containing) Mac "application"
bundles, and cross-platform "Java archive" (JAR) files.

### Development

The viewer is under active development, as you can see by reading [the monthly
development reports on the author's blog](
https://shinecycle.wordpress.com/tag/assistive/) or examining the commits in
this repository.  Interested developers or users can help by reporting issues,
requesting features, or contributing patches.

Changes to the project are automatically tested using Travis Continuous Integration:
[![Build Status](https://travis-ci.org/kingjon3377/strategicprimer-viewer.svg?branch=master)](https://travis-ci.org/kingjon3377/strategicprimer-viewer)
