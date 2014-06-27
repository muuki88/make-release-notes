name := "scala-release-note-generator"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
    "org.pegdown" % "pegdown" % "1.2.0",
    "org.apache.commons" % "commons-lang3" % "3.1",
    "com.github.scopt" %% "scopt" % "3.2.0",
    // tests
    "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test"
)

{
  require(sys.props("file.encoding") == "UTF-8", "Please rerun with -Dfile.encoding=UTF-8")
  Nil
}
