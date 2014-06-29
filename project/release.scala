import sbt._
import Keys._

import complete.DefaultParsers._
import complete.Parser

/**
 * Copied from the sbt-native-packager project
 */
object Release {

  val versionNumberParser: Parser[String] = {
    val classifier: Parser[String] = ("-" ~ ID) map {
      case (dash, id) => dash + id
    }
    val version: Parser[String] = (Digit ~ chars(".0123456789").* ~ classifier.?) map {
      case ((first, rest), optClass) => ((first +: rest).mkString + optClass.getOrElse(""))
    }
    val complete = (chars("v") ~ token(version, "<version number>")) map {
      case (v, num) => v + num
    }
    complete  
  }

  def releaseParser(state: State): Parser[String] =
    Space ~> versionNumberParser


  val releaseHelp = Help("release",
    "release <git tag>" -> "Runs the release script for a given version number",
    """|release <git tag>
       |
       |Runs our release script.  This will:
       |1. Tag the git repo with the given tag (v<version>).
       |2. Reload the build with the new version number from the git tag.
       |3. publish all the artifacts to sonatype.""".stripMargin
  )


  def releaseAction(state: State, tag: String): State = {
    "test" ::
    // TODO - Signed tags, possibly using pgp keys?
    ("git tag " + tag) ::
    "+ publishSigned" ::
    ("git push origin " + tag) ::
    state
  }

  val releaseCommand = 
    Command("release", releaseHelp)(releaseParser)(releaseAction)

  def settings: Seq[Setting[_]]=
    Seq(commands += releaseCommand)
}