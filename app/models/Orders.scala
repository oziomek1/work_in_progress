package models

import play.api.libs.json._

case class Orders(
  orderID: Long,
  userID: Long,
  orderAddress: String,
  orderDate: String,
  orderShipped: Boolean)

object Orders {
  implicit val ordersFormat = Json.format[Orders]
}
