package models

import play.api.libs.json._

case class OrderDetails(
  orderDetailID: Long,
  orderID: Long,
  productQuantity: Int,
  productID: Long,
  orderDetailPriceNet: Double,
  orderDetailPriceGross: Double)

object OrderDetails {
  implicit val orderDetailsFormat = Json.format[OrderDetails]
}