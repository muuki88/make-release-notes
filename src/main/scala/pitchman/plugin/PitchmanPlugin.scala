package pitchman.plugin

import sbt._
import sbt.Keys.{ baseDirectory, organization, name, target, commands, streams }
import sbt.complete.DefaultParsers._
import sbt.complete.Parser
import pitchman.model.{ TargetLanguage, MarkDown }
import pitchman.github.{ ReleaseNotes, GithubProject }
import java.nio.charset.Charset

object PitchmanPlugin extends AutoPlugin {


  object autoImport {
    val Pitchman = config("pitchman")

    val releaseNotesLanguage = settingKey[TargetLanguage]("Language rendered to (e.g. Markdown)")
    val renderReleaseNotes = taskKey[File]("Create release notes")

    val releasenotesFilename = settingKey[String]("The filename which contains the release notes. Directory is target in Pitchman")
    val releaseNotesPreviousTag = settingKey[String]("The previous tag. Starting point for git log")
    val releaseNotesCurrentTag = settingKey[String]("The current tag. Ending for git log")

    val renderCommiters = settingKey[Boolean]("Renders list of commiters?")
    val renderPullRequests = settingKey[Boolean]("Renders list of pull requests?")
    val renderFixedIssues = settingKey[Boolean]("Renders list of fix commits?")

    // default values for the tasks and settings
    lazy val baseReleaseNotesSettings: Seq[sbt.Def.Setting[_]] = Seq(
      renderReleaseNotes := {
        implicit val language = releaseNotesLanguage.value
        val fixCommits = renderFixedIssues.value
        val pullRequests = renderPullRequests.value
        val commiters = renderCommiters.value

        val dir = baseDirectory.value
        val user = (organization in Pitchman).value
        val repo = (name in Pitchman).value

        val previous = releaseNotesPreviousTag.value
        val current = releaseNotesCurrentTag.value

        streams.value.log.info("Creating release notes with settings:")
        streams.value.log.info(s"  Directory: $dir")
        streams.value.log.info(s"  Github   : $user/$repo")
        streams.value.log.info(s"  Tags     : $previous -> $current")
        // Do the rendering
        val notes = new ReleaseNotes(dir, previous, current, GithubProject(user, repo))

        val out = new StringBuilder

        if (fixCommits) out append s"${notes.renderFixedIssues}\n"
        if (pullRequests) out append s"${notes.renderPullRequests}\n"
        if (commiters) out append s"${notes.renderCommitterList}\n"

        val file = (target in Pitchman).value / releasenotesFilename.value
        streams.value.log.success(s"Creating release notes file: $file")

        IO delete file
        IO write (file, out.toString, Charset.forName("UTF-8"), false)
        streams.value.log.success("Created release notes")
        file
      },
      releaseNotesLanguage := MarkDown,
      renderCommiters := true,
      renderPullRequests := true,
      renderFixedIssues := true,
      releaseNotesPreviousTag := "",
      releaseNotesCurrentTag := "",
      releasenotesFilename := "releaseNotes.md",
      commands += PitchmanCommand.releaseNotesCommand
    ) ++ inConfig(Pitchman)(Seq(
        name <<= name in Compile,
        organization <<= organization in Compile,
        target <<= target in Compile
      ))
  }

  import autoImport._

  // This plugin is automatically enabled for projects which are JvmModules.
  override def trigger = allRequirements

  override val projectSettings = baseReleaseNotesSettings

}

object PitchmanCommand {

  /**
   * usage:
   * {{
   *   releaseNotes v1.0 v2.0
   * }}
   */
  def releaseNotesParser(state: State): Parser[(String, String)] = (Space ~> NotSpace) ~ (Space ~> NotSpace) map {
    case (prev, curr) => (prev, curr)
  }

  val releaseNotesHelp = Help("releaseNotes",
    "release <git tag> <git tag>" -> "Creates release notes for this project",
    """|releaseNotes <previous git tag> <current git tag>
       |
       |Creates the release notes based on
       |1. renderCommiters|renderPullRequests|renderFixedIssues true/false
       |2. targetLanguage (by default MarkDown)
       |3. git log between previous and current git tag
       |4. git repo (name in Pitchman, organization in Pitchman)
       |5. prints out the release notes""".stripMargin
  )

  def releaseNotesAction(state: State, tags: (String, String)): State = {
    s"""set releaseNotesPreviousTag := "${tags._1}"""" ::
      s"""set releaseNotesCurrentTag := "${tags._2}"""" ::
      "renderReleaseNotes" ::
      state
  }

  val releaseNotesCommand =
    Command("releaseNotes", releaseNotesHelp)(releaseNotesParser)(releaseNotesAction)

}
