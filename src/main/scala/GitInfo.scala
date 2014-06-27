

case class GithubProject(user: String, project: String)

case class Commit(sha: String, author: String, header: String, body: String, merge: Boolean) {
  def trimmedHeader = header take 80
  override def toString = " * " + sha + " (" + author + ") " + header + " - " + body.take(5) + " ..."
}

case class PullRequest(num: Int, commit: Commit, fork: GithubProject, header: String) {
  def trimmedHeader = header take 80
}

/** Global functions for dealing with git. */
object GitHelper {

  val Sha = "([a-f0-9]{7} )(.*)".r

  def processGitCommits(gitDir: java.io.File, previousTag: String, currentTag: String): IndexedSeq[Commit] = {
    processGitCommits(gitDir, previousTag, currentTag, true) ++ processGitCommits(gitDir, previousTag, currentTag, false)
  }
  def htmlEncode(s: String) = org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(s)

  private def processGitCommits(gitDir: java.io.File, previousTag: String, currentTag: String, merges: Boolean): IndexedSeq[Commit] = {
    import sys.process._
    val mergeConstraint = if (merges) "--merges" else "--no-merges"
    val gitFormat = "%h %s %b" // sha, subject and body
    val log = Process(Seq("git", "--no-pager", "log",
      s"${previousTag}..${currentTag}",
      "--format=format:" + gitFormat,
      "--topo-order",
      mergeConstraint), gitDir).lines

    log.foldLeft(Seq.empty[String]) {
      case (commits, Sha(sha, msg)) => commits :+ (sha + msg)
      case (commits, line)          => commits.head + line; commits
    }.map(_.split(" ", 2)).collect {
      case Array(sha, title) =>
        val (author :: body) = Process(Seq("git", "--no-pager", "show", sha, "--format=format:%aN%n%b", "--quiet"), gitDir).lines.toList
        Commit(sha, author, title, body mkString "\n", merges)
    }.toVector
  }
}

/**
 * This class is able to grab and render information pulled
 * from github commit messages, including:
 *
 *    * Contributors by commit
 *    * Issues fixes since <previous tag>
 *    * All commits with links.
 */
class GitInfo(
  gitDir: java.io.File,
  val previousTag: String,
  val currentTag: String,
  val ghproject: GithubProject = GithubProject("scala", "scala"),
  val issueDetector: IssueDetector = ScalaIssueDetector)(implicit targetLanguage: TargetLanguage) {
  import GitHelper._
  val commits = processGitCommits(gitDir, previousTag, currentTag)

  /** All the authors + their # of commits */
  val authors: Seq[(String, Int)] = {
    val grouped: Vector[(String, Int)] = commits
      .filter(!_.merge) // no merges
      .groupBy(_.author) // by author
      .map { case (a, c) => a -> c.length } { collection.breakOut }
    (grouped sortBy (_._2)).reverse
  }
  /** A list of all commits that we think fixed something. */
  val fixCommits = commits filter (c => (issueDetector isFixCommit c) && !(issueDetector isPullRequest c))

  val pullRequests = commits filter (issueDetector isPullRequest _) map (issueDetector pullRequest _) sortBy (_.num)

  private def commitShaLink(sha: String) =
    targetLanguage.createHyperLink(s"https://github.com/${ghproject.user}/${ghproject.project}/commit/${sha}", sha)

  private def blankLine(): String = targetLanguage.blankLine()
  private def header4(msg: String): String = targetLanguage.header4(msg)

  /** Renders a table of authors + # of commits, sorted by commits */
  def renderCommitterList: String = {
    val sb = new StringBuffer
    sb append blankLine()
    sb append header4("A big thank you to all the contributors!")
    sb append targetLanguage.tableHeader("#", "Author")
    for ((author, count) <- authors)
      sb append targetLanguage.tableRow(count.toString, author)
    sb append targetLanguage.tableEnd
    sb.toString
  }

  /** Renders a list of all commits. */
  def renderCommitList: String = {
    val sb = new StringBuilder
    sb append blankLine()
    sb append header4("Complete commit list!")
    sb append targetLanguage.tableHeader("sha", "Title")
    for (commit <- commits)
      sb append targetLanguage.tableRow(commitShaLink(commit.sha), commit.trimmedHeader)
    sb append targetLanguage.tableEnd
    sb.toString
  }

  /** renders a list of all fixed commits with links if we have em. */
  def renderFixedIssues: String = {
    val sb = new StringBuilder
    sb append blankLine()
    sb append header4(s"Commits and the issues they fixed since ${previousTag}")
    sb append targetLanguage.tableHeader("Issue(s)", "Commit", "Message")
    for (commit <- fixCommits) {
      sb append targetLanguage.tableRow(issueDetector.issueLink(commit), commitShaLink(commit.sha), commit.trimmedHeader)
    }
    sb append targetLanguage.tableEnd
    sb append blankLine()
    sb.toString
  }

  /** renders a list of all pull requests merged */
  def renderPullRequests: String = {
    val sb = new StringBuilder
    sb append blankLine()
    sb append header4(s"Pull requests since ${previousTag}")
    sb append targetLanguage.tableHeader("Pull Request(s)", "Commit", "Message")
    for (pr <- pullRequests) {
      sb append targetLanguage.tableRow(issueDetector.issueLink(pr.commit), commitShaLink(pr.commit.sha), pr.trimmedHeader)
    }
    sb append targetLanguage.tableEnd
    sb append blankLine()
    sb.toString
  }

}