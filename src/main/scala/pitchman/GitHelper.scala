package pitchman

import pitchman.model._

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
        Commit(sha, Author(author), title, body mkString "\n", merges)
    }.toVector
  }
}