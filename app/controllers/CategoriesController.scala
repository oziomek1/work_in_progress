package controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import models.daos.CategoriesDAO
import play.api.libs.json.Json
import play.api.mvc.{ MessagesAbstractController, MessagesControllerComponents }
import utils.auth.DefaultEnv

import scala.concurrent.ExecutionContext

class CategoriesController @Inject() (
  categoriesDAO: CategoriesDAO,
  cc: MessagesControllerComponents,
  silhouette: Silhouette[DefaultEnv])(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def getCategories = silhouette.SecuredAction.async { implicit request =>
    categoriesDAO.all().map { categories =>
      Ok(Json.toJson(categories))
    }
  }

  def getCategoryById(categoryID: Long) = Action.async { implicit request =>
    categoriesDAO.getById(categoryID).map { category =>
      Ok(Json.toJson(category))
    }
  }

  def getCategoryByName(name: String) = Action.async { implicit request =>
    categoriesDAO.getByName(name).map { category =>
      Ok(Json.toJson(category))
    }
  }

  def addCategory = Action.async { implicit request =>
    val categoryName = request.body.asJson.get("categoryName").as[String]

    categoriesDAO.create(categoryName).map {
      category => Ok(Json.toJson(category))
    }
  }

  def deleteCategory(id: Long) = Action.async { implicit request =>
    categoriesDAO.delete(id).map { category =>
      Ok(Json.toJson(category))
    }
  }

  def editCategory(id: Long) = Action.async { implicit request =>
    val categoryName = request.body.asJson.get("categoryName").as[String]

    categoriesDAO.update(id, categoryName).map {
      category => Ok(Json.toJson(category))
    }
  }

}
