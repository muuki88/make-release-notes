/**
 * Trait used to detect + create links to issue comments
 * in git commits.
 */
trait IssueDetector {
  def isFixCommit(commit: Commit): Boolean
  def isPullRequest(commit: Commit): Boolean
  def pullRequest(commit: Commit)(implicit targetLanguage: TargetLanguage): PullRequest
  def issueLink(commit: Commit)(implicit targetLanguage: TargetLanguage): String
  def pullRequestLink(pr: PullRequest)(implicit targetLanguage: TargetLanguage): String
}

/**
 * Detector for issue tracking in scala commit messages.
 */
object ScalaIssueDetector extends IssueDetector {
  private val siPattern = java.util.regex.Pattern.compile("(SI-[0-9]+)")
  private def hasFixins(msg: String): Boolean = (
    (msg contains "SI-") /*&& ((msg.toLowerCase contains "fix") || (msg.toLowerCase contains "close"))*/
  )
  private def fixLinks(commit: Commit)(implicit targetLanguage: TargetLanguage): String = {
    val searchString = commit.body + commit.header
    val m = siPattern matcher searchString
    val issues = new collection.mutable.ArrayBuffer[String]
    while (m.find()) {
      issues += (m group 1)
    }
    issues map (si => targetLanguage.createHyperLink(s"https://issues.scala-lang.org/browse/$si", si)) mkString ", "
  }

  def isFixCommit(commit: Commit): Boolean = hasFixins(commit.body + commit.header)
  def isPullRequest(commit: Commit): Boolean = false
  def issueLink(commit: Commit)(implicit targetLanguage: TargetLanguage): String = fixLinks(commit)
  def pullRequest(commit: Commit)(implicit targetLanguage: TargetLanguage): PullRequest = ???
  def pullRequestLink(pr: PullRequest)(implicit targetLanguage: TargetLanguage): String = ???
}

/**
 * Detector for issue tracking in canonical github projects.
 */
case class GithubIssueDetector(project: GithubProject) extends IssueDetector {
  import GithubIssueDetector.{ IssueNum, PullRequestRegex }

  def isFixCommit(commit: Commit): Boolean =
    (commit.header + commit.body).toLowerCase contains "fix" // TODO && contains # ?

  def isPullRequest(commit: Commit): Boolean = commit.merge && (commit.header contains "Merge pull request")

  def issueLink(commit: Commit)(implicit targetLanguage: TargetLanguage): String = {
    (commit.header + commit.body).split("[\r\n]+").collect {
      case IssueNum(num) => targetLanguage.createHyperLink(s"https://github.com/${project.user}/${project.project}/issues/${num}", num)
    } mkString " "
  }

  def pullRequest(commit: Commit)(implicit targetLanguage: TargetLanguage): PullRequest = {
    (commit.header + commit.body) /*.split("[\r\n]+").collect*/ match {
      case PullRequestRegex(_, num, _, user, repo, header) => PullRequest(num.toInt, commit, GithubProject(user, repo), header)
    }
  }

  def pullRequestLink(pr: PullRequest)(implicit targetLanguage: TargetLanguage): String = {
    targetLanguage.createHyperLink(s"https://github.com/${project.user}/${project.project}/issues/${pr.num}", pr.num.toString)
  }
}
object GithubIssueDetector {
  val IssueNum = ".*\\#([0-9]+).*".r
  val PullRequestRegex = "(Merge pull request )#([0-9]+)( from )(\\w*)/([a-z0-9/\\-_]*)(\\s*.*\\s*)".r
}