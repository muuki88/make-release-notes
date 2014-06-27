package pitchman.model

case class PullRequest(num: Int, merge: Commit, fork: Project, header: String) {
  def trimmedHeader = header take 80
}