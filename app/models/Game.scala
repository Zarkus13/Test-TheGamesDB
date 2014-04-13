package models

import java.util.Date

/**
 * Created by alexis on 13/04/14.
 */
case class Game(
  id: Long,
  title: String,
  platform: String,
  platformId: Long,
  releaseDate: Date,
  overview: String,
  youtubeUrl: String,
  boxImageUrl: String,
  fanartsUrl: List[String])
