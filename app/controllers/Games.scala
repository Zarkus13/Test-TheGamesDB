package controllers

import play.api.mvc._
import play.api.libs.ws.WS
import play.api.Play
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import models.Game
import scala.xml.{NodeSeq, Elem}
import java.text.SimpleDateFormat
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

/**
 * Created by alexis on 13/04/14.
 */
object Games extends Controller {

  lazy val baseUrl = Play.current.configuration.getString("games.db.base.url").get
  lazy val searchGamesUrl = baseUrl + "/GetGamesList.php"
  lazy val getGameUrl = baseUrl + "/GetGame.php"

  def search(term: String) = Action.async {

    def tag(tag: String)(implicit xml: NodeSeq) = (xml \ tag).text

    WS.url(searchGamesUrl)
      .withQueryString(
        "name" -> term
      ).get map { resp =>

      val ids = resp.xml \\ "Game" \ "id" map { id =>
        id.text
      }

      val games = ids.map { id =>
        WS.url(getGameUrl)
          .withQueryString(
            "id" -> id
          ).get map { resp =>

          val dateFormat = new SimpleDateFormat("dd/MM/yyyy")

          ((resp.xml \\ "Data") flatMap { data =>
            val baseImgUrl = (data \ "baseImgUrl").text

            (data \\ "Game") map {
              implicit game =>
                Game(
                  tag("id").toLong,
                  tag("GameTitle"),
                  tag("Platform"),
                  tag("PlatformId").toLong,
                  tag("ReleaseDate") match {
                    case "" => null
                    case s: String => dateFormat.parse(s)
                  },
                  tag("Overview"),
                  tag("Youtube"),
                  baseImgUrl + (game \ "boxart" \ "@thumb").text,
                  Nil
                )
            }
          }).apply(0)
        }
      }

      Ok(views.html.games(
        Await.result(Future.sequence(games), Duration.Inf),
        term
      ))
    }
  }

  def get(id: Long) = Action.async {
    WS.url(getGameUrl)
      .withQueryString(
        "id" -> id.toString
      ).get map { resp =>

      Ok(resp.xml)
    }
  }

}
