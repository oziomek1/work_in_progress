package models.daos

import javax.inject.{ Inject, Singleton }
import models.Orders
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class OrdersDAO @Inject() (dbConfigProvider: DatabaseConfigProvider, val usersDAO: UsersDAO)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private val orders = TableQuery[OrdersTable]

  def all(): Future[Seq[Orders]] = db.run {
    orders.result
  }

  def getById(id: Long): Future[Seq[Orders]] = db.run {
    orders.filter(_.orderID === id).result
  }

  def create(userID: Long, address: String, date: String, shipped: Boolean = false): Future[Orders] = db.run {
    (orders.map(o => (o.userID, o.orderAddress, o.orderDate, o.orderShipped))
      returning orders.map(_.orderID)
      into {
        case ((userID, address, date, shipped), id) =>
          Orders(id, userID, address, date, shipped)
      }) += ((userID, address, date, shipped))
  }

  def delete(orderID: Long): Future[Int] = db.run {
    orders.filter(_.orderID === orderID).delete
  }

  def update(orderID: Long, userID: Long, orderAddress: String, orderDate: String, orderShipped: Boolean) = db.run {
    orders.filter(_.orderID === orderID)
      .map(ord => (ord.userID, ord.orderAddress, ord.orderDate, ord.orderShipped))
      .update((userID, orderAddress, orderDate, orderShipped))
  }

  def getByUserId(userId: Long): Future[Seq[Orders]] = db.run {
    orders.filter(_.userID === userId).result
  }

  class OrdersTable(tag: Tag) extends Table[Orders](tag, "orders") {
    def orderID = column[Long]("orderID", O.PrimaryKey, O.AutoInc)
    def orderAddress = column[String]("orderAddress")
    def orderDate = column[String]("orderDate")
    def orderShipped = column[Boolean]("orderShipped")
    def userID = column[Long]("userID")

    def * = (orderID, userID, orderAddress, orderDate, orderShipped) <> ((Orders.apply _).tupled, Orders.unapply)
  }
}
