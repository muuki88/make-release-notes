# Release Note Generator - Pitchman

This projects helps you generating release notes for the awesome
[Github Release Notes](https://github.com/blog/1547-release-your-software) feature.

## Running

1. Update the highlights notes in `hand-written.md`.
2. run `sbt -Dfile.encoding=UTF-8 console`, and then:

```bash
runMain pitchman.github.Generator -gh user/repo -p v1.0 -n v1.1 -l /home/user/git/repo
```

## Contributing

Feel free to improve. Make sure to sign the Typesafe CLA.
