package pitchman.github

import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits._
import pitchman.model._

object GithubAPI {

  def pullrequest(num: Int): PullRequest = {
    val url = s"https://api.github.com/repos/sbt/sbt-native-packager/pulls/$num"
    /*
    WS.url(url).get().map { response =>
      (response.json \ "person" \ "name").as[String]
    }
    */
    ???
  }

}