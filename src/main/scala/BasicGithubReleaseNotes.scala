import java.io.File

object BasicGithubReleaseNotes extends App {
  val GhProjectString = new util.matching.Regex("([^/]+)/(.+)")

  case class Config(project: GithubProject, previousTag: String, nextTag: String, location: File)

  val parser = new scopt.OptionParser[Config]("github-release-notes") {
    head("github-release-notes", "1.0")
    opt[String]("github-page") abbr ("gh") required () action {
      case (GhProjectString(user, project), c) => c.copy(project = GithubProject(user, project))
    } text ("the github url: user/repo")

    opt[String]('p', "prev") required () action { (tag, c) => c.copy(previousTag = tag)
    } text ("the previous tag to compare with, e.g v1.0")

    opt[String]('n', "next") required () action { (tag, c) => c.copy(nextTag = tag)
    } text ("the next tag to compare with, e.g v1.1")

    opt[File]('l', "location") required () valueName ("<file>") action { (location, c) =>
      c.copy(location = location)
    } validate { f =>
      if (!f.exists) {
        failure(s"location $f doesn't exist")
      } else if (!f.isDirectory) {
        failure(s"location $f is not a directory")
      } else {
        success
      }
    } text ("the location of your git repository")

    help("help") text ("-gh user/repo -p v1.0 -n v1.1 -l /home/user/git/your-project")
  }
  // parser.parse returns Option[C]
  parser.parse(args, Config(GithubProject("", ""), "", "", null)) map { config =>
    generateNotes(config)
  } getOrElse {
    // arguments are bad, error message will have been displayed
    sys.exit(0)
  }

  def generateNotes(config: Config) {
    implicit val language = MarkDown

    val info = new GitInfo(config.location, config.previousTag, config.nextTag, config.project, GithubIssueDetector(config.project))
    println(info.renderFixedIssues)
    //println(info.renderCommitList)
    println(info.renderCommitterList)
  }
}