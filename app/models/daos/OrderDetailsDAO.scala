package models.daos

import javax.inject.{ Inject, Singleton }
import models.OrderDetails
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class OrderDetailsDAO @Inject() (dbConfigProvider: DatabaseConfigProvider, val ordersDAO: OrdersDAO, val productsDAO: ProductsDAO)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private val orderDetails = TableQuery[OrderDetailsTable]

  def all(): Future[Seq[OrderDetails]] = db.run {
    orderDetails.result
  }

  def getById(id: Long): Future[Seq[OrderDetails]] = db.run {
    orderDetails.filter(_.orderDetailID === id).result
  }

  def getByOrderId(id: Long): Future[Seq[OrderDetails]] = db.run {
    orderDetails.filter(_.orderID === id).result
  }

  def create(orderID: Long, productQuantity: Int, productID: Long, orderDetailPriceNet: Double, orderDetailPriceGross: Double): Future[OrderDetails] = db.run {
    (orderDetails.map(o => (o.orderID, o.productQuantity, o.productID, o.orderDetailPriceNet, o.orderDetailPriceGross))
      returning orderDetails.map(_.orderDetailID)
      into {
        case ((orderID, productQuantity, productID, orderDetailPriceNet, orderDetailPriceGross), id) =>
          OrderDetails(id, orderID, productQuantity, productID, orderDetailPriceNet, orderDetailPriceGross)
      }) += ((orderID, productQuantity, productID, orderDetailPriceNet, orderDetailPriceGross))
  }

  def update(orderID: Long, productQuantity: Int, productID: Long,
    orderDetailPriceNet: Double, orderDetailPriceGross: Double) = db.run {
    orderDetails.filter(_.orderID === orderID)
      .map(ord => (ord.orderID, ord.productQuantity, ord.productID, ord.orderDetailPriceNet, ord.orderDetailPriceGross))
      .update((orderID, productQuantity, productID, orderDetailPriceNet, orderDetailPriceGross))
  }

  def delete(orderDetailID: Long): Future[Int] = db.run {
    orderDetails.filter(_.orderDetailID === orderDetailID).delete
  }

  class OrderDetailsTable(tag: Tag) extends Table[OrderDetails](tag, "orderDetails") {
    def orderDetailID = column[Long]("orderDetailID", O.PrimaryKey, O.AutoInc)
    def orderID = column[Long]("orderID")
    def productQuantity = column[Int]("productQuantity")
    def productID = column[Long]("productID")
    def orderDetailPriceNet = column[Double]("orderDetailPriceNet")
    def orderDetailPriceGross = column[Double]("orderDetailPriceGross")

    def * = (orderDetailID, orderID, productQuantity, productID, orderDetailPriceNet, orderDetailPriceGross) <> ((OrderDetails.apply _).tupled, OrderDetails.unapply)
  }
}