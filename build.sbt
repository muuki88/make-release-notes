name := "scala-release-note-generator"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies += "org.pegdown" % "pegdown" % "1.2.0"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.1"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.2.0"

{
  require(sys.props("file.encoding") == "UTF-8", "Please rerun with -Dfile.encoding=UTF-8")
  Nil
}
