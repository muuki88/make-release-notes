package pitchman.github.detectors

import pitchman.model._
import pitchman.detectors.Detector
import pitchman.github.GithubProject

/**
 * Detects commits which mark an issue as fixed an thus close it
 */
class FixCommits(project: GithubProject) extends Detector[(Commit, Seq[Issue])] {

  val IssueNum = ".*\\#([0-9]+).*".r

  def apply(commit: Commit): Option[(Commit, Seq[Issue])] = if (isFix(commit)) {
    val text = commit.header + commit.body
    val issues = text.split("[\r\n]+").collect {
      case IssueNum(num) => Issue(num.toInt, s"https://github.com/${project.user}/${project.project}/issues/${num}")
    }
    Some((commit, issues))
  } else {
    None
  }

  def isFix(commit: Commit): Boolean = {
    val text = (commit.header + commit.body).toLowerCase
    (text contains "fix") && !(text contains "merge pull request")
  }

}