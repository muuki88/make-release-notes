package pitchman.github

import pitchman.model._
import pitchman.detectors.Detector
import pitchman.GitHelper._
import pitchman.github.detectors.FixCommits
import pitchman.github.detectors.PullRequestDetector

/**
 * Rendering the statistics
 */
class ReleaseNotes(
  val gitDir: java.io.File,
  val previousTag: String,
  val currentTag: String,
  val project: GithubProject)(implicit targetLanguage: TargetLanguage) {

  object Detectors {
    val fixCommits = new FixCommits(project)
    val pullRequests = new PullRequestDetector(project)
  }

  lazy val commits = processGitCommits(gitDir, previousTag, currentTag)
  lazy val fixCommits = commits map Detectors.fixCommits flatten
  lazy val pullRequests = commits map Detectors.pullRequests flatten

  /** All the authors + their # of commits and pull requests */
  lazy val authors: Seq[Author] = {
    val grouped = commits
      .groupBy(_.author.name) // by author
      .map {
        case (name, c) => Author(
          name = name,
          commits = c.filter(!_.merge).size,
          pullRequests = c.filter(_.merge).size
        )
      }.toSeq
    (grouped sortBy (_.commits)).reverse
  }

  /* =============================================================== */

  /** Renders a table of authors + # of commits, sorted by commits */
  def renderCommitterList: String = {
    val sb = new StringBuffer
    sb append blankLine()
    sb append header4("A big thank you to all the contributors!")
    sb append targetLanguage.tableHeader("#", "Author")
    authors foreach {
      case Author(author, commits, prs) => sb append targetLanguage.tableRow(commits.toString, author)
    }
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
    fixCommits foreach {
      case (commit, issues) =>
        val links = issues map (_.link) mkString " "
        sb append targetLanguage.tableRow(links, commitShaLink(commit.sha), commit.trimmedHeader)
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

    pullRequests sortBy (_.num) foreach {
      case pr @ PullRequest(num, merge, fork, header) =>
        sb append targetLanguage.tableRow(pullRequestLink(num), commitShaLink(merge.sha), pr.trimmedHeader)
    }
    sb append targetLanguage.tableEnd
    sb append blankLine()
    sb.toString
  }

  /* =============================================================== */

  private def commitShaLink(sha: String) =
    targetLanguage.createHyperLink(s"https://github.com/${project.user}/${project.project}/commit/${sha}", sha)

  private def pullRequestLink(num: Int) =
    targetLanguage.createHyperLink(s"https://github.com/${project.user}/${project.project}/commit/${num}", s"#${num}")

  private def blankLine(): String = targetLanguage.blankLine()
  private def header4(msg: String): String = targetLanguage.header4(msg)
}