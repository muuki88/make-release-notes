# Release Note Generator - Pitchman

This projects helps you generating release notes for the awesome
[Github Release Notes](https://github.com/blog/1547-release-your-software) feature.

## SBT Plugin

```
addSbtPlugin("de.mukis" % "pitchman-release-notes-generator" % "0.1")
```

Thanks to autoplugins you can now just run

```
releaseNotes v1.0 v2.0
```

to generate release notes in your target folder.

### Settings

* `releaseNotesLanguage` by default `MarkDown`
* `releasenotesFilename` by default `releaseNotes.md`
* `releaseNotesPreviousTag`, set by the `releaseNotes` command
* `releaseNotesCurrentTag`, set by the `releaseNotes` command
* `renderCommiters`, should a commiter list be rendered. `true` by default
* `renderPullRequests`, should a pull request list be rendered `true` by default
* `renderFixedIssues`, should a fix issues list be rendered `true` by default

Furthermore you can define a few more things in the `Pitchman` scope.

* `target in Pitchman`. Where to put the releaseNotes file. By default `target in Compile`
* `orgnization in Pitchman`. Used as github user, or organization in github/**user**/repo. By default `organization in Compile`
* `name in Pitchman`. Used as github project name in github/user/**repo**. By default `name in Compile`

## Running

1. Update the highlights notes in `hand-written.md`.
2. run `sbt -Dfile.encoding=UTF-8 console`, and then:

```bash
runMain pitchman.github.Generator -gh user/repo -p v1.0 -n v1.1 -l /home/user/git/repo
```

