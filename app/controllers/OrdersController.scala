package controllers

import java.text.SimpleDateFormat
import java.util.Calendar

import javax.inject.Inject
import models.daos._
import play.api.libs.json.Json
import play.api.mvc.{ MessagesAbstractController, MessagesControllerComponents }

import scala.concurrent.ExecutionContext

class OrdersController @Inject() (
  ordersDAO: OrdersDAO,
  usersDAO: UsersDAO,
  productsDAO: ProductsDAO,
  orderDetailsDAO: OrderDetailsDAO,
  cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def getOrders = Action.async { implicit request =>
    ordersDAO.all().map { orders =>
      Ok(Json.toJson(orders))
    }
  }

  def getOrderById(id: Long) = Action.async { implicit request =>
    ordersDAO.getById(id).map { order =>
      Ok(Json.toJson(order))
    }
  }

  def getOrderByUser(id: Long) = Action.async { implicit request =>
    ordersDAO.getByUserId(id).map { order =>
      Ok(Json.toJson(order))
    }
  }

  def getOrderDetailsByOrderId(id: Long) = Action.async { implicit request =>
    orderDetailsDAO.getByOrderId(id).map({ orderDetails =>
      Ok(Json.toJson(orderDetails))
    })
  }

  def addOrder = Action.async { implicit request =>
    val userID = request.body.asJson.get("userID").as[Long]
    val orderAddress = request.body.asJson.get("orderAddress").as[String]
    val productQuantity = request.body.asJson.get("productQuantity").as[Int]
    val productID = request.body.asJson.get("productID").as[Long]
    val orderDetailsPriceNet = request.body.asJson.get("orderDetailsPriceNet").as[Double]
    val orderDetailsPriceGross = request.body.asJson.get("orderDetailsPriceGross").as[Double]
    val dateFormat = new SimpleDateFormat("YYYY-MM-dd")

    ordersDAO.create(userID, orderAddress, dateFormat.format(Calendar.getInstance().getTime)).map { order =>
      orderDetailsDAO.create(order.orderID, productQuantity, productID, orderDetailsPriceNet, orderDetailsPriceGross)
    }.map {
      _ => Ok(Json.toJson("order_created"))
    }
  }

  def editOrder(id: Long) = Action.async { implicit request =>
    val userID = request.body.asJson.get("userID").as[Long]
    val orderAddress = request.body.asJson.get("orderAddress").as[String]
    val productQuantity = request.body.asJson.get("productQuantity").as[Int]
    val productID = request.body.asJson.get("productID").as[Long]
    val orderDetailsPriceNet = request.body.asJson.get("orderDetailsPriceNet").as[Double]
    val orderDetailsPriceGross = request.body.asJson.get("orderDetailsPriceGross").as[Double]
    val orderDate = request.body.asJson.get("orderDate").as[String]
    val orderShipped = request.body.asJson.get("orderShipped").as[Boolean]
    ordersDAO.update(id, userID, orderAddress, orderDate, orderShipped).map { order =>
      orderDetailsDAO.update(id, productQuantity, productID, orderDetailsPriceNet, orderDetailsPriceGross)
    }.map {
      orderDetail => Ok(Json.toJson("Ok"))
    }
  }

  def deleteOrder(id: Long) = Action.async { implicit request =>
    ordersDAO.delete(id).map { order =>
      orderDetailsDAO.delete(id)
    }.map {
      _ => Ok(Json.toJson(id))
    }
  }

}
