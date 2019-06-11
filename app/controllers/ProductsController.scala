package controllers

import javax.inject.Inject
import models.daos._
import play.api.libs.json.Json
import play.api.mvc.{ MessagesAbstractController, MessagesControllerComponents }

import scala.concurrent.ExecutionContext

class ProductsController @Inject() (
  productsDAO: ProductsDAO,
  categoriesDAO: CategoriesDAO,
  cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def getProducts = Action.async { implicit request =>
    productsDAO.all().map { products =>
      Ok(Json.toJson(products))
    }
  }

  def getProductById(id: Long) = Action.async { implicit request =>
    productsDAO.getById(id).map { product =>
      Ok(Json.toJson(product))
    }
  }

  def getProductByCategory(id: Long) = Action.async { implicit request =>
    productsDAO.getByCategory(id).map { product =>
      Ok(Json.toJson(product))
    }
  }

  def getProductByName(name: String) = Action.async { implicit request =>
    productsDAO.getByName(name).map { product =>
      Ok(Json.toJson(product))
    }
  }

  def addProduct = Action.async { implicit request =>
    val productName = request.body.asJson.get("productName").as[String]
    val productDescription = request.body.asJson.get("productDescription").as[String]
    val productImageURL = request.body.asJson.get("productImageURL").as[String]
    val categoryID = request.body.asJson.get("categoryID").as[Long]
    val productPriceNet = request.body.asJson.get("productPriceNet").as[Double]
    val productPriceGross = request.body.asJson.get("productPriceGross").as[Double]

    productsDAO.create(productName, productDescription, productImageURL, categoryID, productPriceNet, productPriceGross).map {
      product => Ok(Json.toJson(product))
    }
  }

  def editProduct(id: Long) = Action.async { implicit request =>
    val productName = request.body.asJson.get("productName").as[String]
    val productDescription = request.body.asJson.get("productDescription").as[String]
    val productImageURL = request.body.asJson.get("productImageURL").as[String]
    val productPriceNet = request.body.asJson.get("productPriceNet").as[Double]
    val productPriceGross = request.body.asJson.get("productPriceGross").as[Double]
    val categoryID = request.body.asJson.get("categoryID").as[Long]
    productsDAO.update(id, productName, productDescription, categoryID, productImageURL, productPriceNet, productPriceGross).map { product =>
      Ok(Json.toJson(product))
    }
  }

  def deleteProduct(id: Long) = Action.async { implicit request =>
    productsDAO.delete(id).map { product =>
      Ok(Json.toJson(product))
    }
  }

}
