# Sonar Delta Report Plugin

A simple SonarQube Reporting Plugin for Pretested Commit Analysis

[![Build Status](https://travis-ci.org/mirkosertic/sonardeltareport.svg?branch=master)](https://travis-ci.org/mirkosertic/sonardeltareport) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.mirkosertic.sonardelta/sonar-delta-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.mirkosertic.sonardelta/sonar-delta-plugin)

## System Requirements

* Java >=7
* SonarQube >= 4.5

## Example Report

A file called summary.txt will be written with the following content(example):

```
Coverage by unit tests went from 10 to 11 (+1).

Non Commenting Lines of Code went from 10 to 20 (+10).


Blocker issues went from 1 to 2 (+1).
Critical issues went from 10 to 8 (-2).
Major issues went from 10 to 2 (-8).
Minor issues went from 0 to 0 (+0).
Info issues went from 0 to 0 (+0).

All values are relative to the last official analysis at Jan 1, 1970 1:00:00 AM
```

This file can be embedded into other reporting mechanism such as pretested commit scenarios.
