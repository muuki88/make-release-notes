package pitchman.model

case class Commit(sha: String, author: Author, header: String, body: String, merge: Boolean) {
  def trimmedHeader = header take 80
  override def toString = s" * $sha( ${author.name} ) $header - ${body.take(5)} ..."
}