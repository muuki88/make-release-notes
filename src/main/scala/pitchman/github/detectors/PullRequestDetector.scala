package pitchman.github.detectors

import pitchman.detectors.Detector
import pitchman.github.GithubProject
import pitchman.model._

/**
 * Detecting commits which merged a pull request
 */
class PullRequestDetector(project: GithubProject) extends Detector[PullRequest] {

  val PullRequestRegex = "(Merge pull request )#([0-9]+)( from )(\\w*)/([a-z0-9/\\-_]*)(\\s*.*\\s*)".r

  def apply(commit: Commit): Option[PullRequest] = if (isPullRequest(commit)) {
    (commit.header + commit.body) match {
      case PullRequestRegex(_, num, _, user, repo, header) => Some(PullRequest(num.toInt, commit, GithubProject(user, repo), header))
      case _ => None
    }
  } else {
    None
  }

  def isPullRequest(commit: Commit): Boolean = commit.merge && (commit.header contains "Merge pull request")
}