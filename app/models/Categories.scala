package models

import play.api.libs.json._

case class Categories(
  categoryID: Long,
  categoryName: String)

object Categories {
  implicit val categoriesFormat = Json.format[Categories]
}