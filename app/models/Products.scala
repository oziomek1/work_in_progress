package models

import play.api.libs.json._

case class Products(
  productID: Long,
  productName: String,
  productDescription: String,
  categoryID: Long,
  productPriceNet: Double,
  productPriceGross: Double)

object Products {
  implicit val productsFormat = Json.format[Products]
}