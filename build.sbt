import scalariform.formatter.preferences._

name := "pitchman-release-notes-generator"

organization := "de.mukis"

scalaVersion in Global := "2.10.4"

//crossScalaVersions := Seq("2.10.4", "2.11.1")

sbtVersion in Global := {
  scalaBinaryVersion.value match {
    case "2.10" | "2.11" => "0.13.5"
    case "2.9.2" => "0.12.4"
  }
}

sbtPlugin := true

libraryDependencies ++= Seq(
    "org.pegdown" % "pegdown" % "1.2.0",
    "org.apache.commons" % "commons-lang3" % "3.1",
    "com.github.scopt" %% "scopt" % "3.2.0",
    "com.typesafe.play" %% "play-ws" % "2.3.0",   
    // tests
    "org.scalatest" %% "scalatest" % "2.2.0" % "test"
)

{
  require(sys.props("file.encoding") == "UTF-8", "Please rerun with -Dfile.encoding=UTF-8")
  Nil
}

scalacOptions in Compile += "-deprecation"

site.settings

com.typesafe.sbt.SbtSite.SiteKeys.siteMappings <+= (baseDirectory) map { dir => 
  val nojekyll = dir / "src" / "site" / ".nojekyll"
  nojekyll -> ".nojekyll"
}

site.sphinxSupport()

ghpages.settings

git.remoteRepo := "git@github.com:muuki88/make-release-notes.git"

publishMavenStyle := false

scriptedSettings

scriptedLaunchOpts <+= version apply { v => "-Dproject.version="+v }

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, false)
  .setPreference(FormatXml, true)
  .setPreference(SpaceInsideBrackets, false)
  .setPreference(IndentWithTabs, false)
  .setPreference(SpaceInsideParentheses, false)
  .setPreference(MultilineScaladocCommentsStartOnFirstLine, false)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(CompactStringConcatenation, false)
  .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, false)
  .setPreference(IndentPackageBlocks, true)
  .setPreference(CompactControlReadability, false)
  .setPreference(SpacesWithinPatternBinders, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 40)
  .setPreference(DoubleIndentClassDeclaration, false)
  .setPreference(PreserveSpaceBeforeArguments, false)
  .setPreference(SpaceBeforeColon, false)
  .setPreference(RewriteArrowSymbols, false)
  .setPreference(IndentLocalDefs, false)
  .setPreference(IndentSpaces, 2)
  //.setPreference(AreserveDanglingCloseParenthesis, true)

