package controllers

import javax.inject.Inject
import models.daos.UsersDAO
import play.api.libs.json.Json
import play.api.mvc.{ MessagesAbstractController, MessagesControllerComponents }

import scala.concurrent.{ ExecutionContext, Future }

class UsersController @Inject() (
  usersDAO: UsersDAO,
  cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def getUsers = Action.async { implicit request =>
    usersDAO.all().map { users =>
      Ok(Json.toJson(users))
    }
  }

  def getUserById(id: Long) = Action.async { implicit request =>
    usersDAO.getByID(id).map { user =>
      Ok(Json.toJson(user))
    }
  }

  def getUserBySocialId(id: String) = Action.async { implicit request =>
    usersDAO.getBySocialID(id).map { user =>
      Ok(Json.toJson(user))
    }
  }

  def addUser = Action.async { implicit request =>
    val socialID = request.body.asJson.get("userSocialID").as[String]
    val email = request.body.asJson.get("userEmail").as[String]
    val firstName = request.body.asJson.get("userFirstname").as[String]
    val lastName = request.body.asJson.get("userLastname").as[String]
    val address = request.body.asJson.get("userAddress").as[String]
    val password = request.body.asJson.get("userPassword").as[String]
    val isAdmin = request.body.asJson.get("isAdmin").as[Boolean]
    usersDAO.create(Option(socialID), email, firstName, lastName, Option(address), Option(password), isAdmin).map { user =>
      Ok(Json.toJson(user))
    }
  }

  def editUser(id: Long) = Action.async { implicit request =>
    val socialID = request.body.asJson.get("userSocialID").as[String]
    val email = request.body.asJson.get("userEmail").as[String]
    val firstName = request.body.asJson.get("userFirstname").as[String]
    val lastName = request.body.asJson.get("userLastname").as[String]
    val address = request.body.asJson.get("userAddress").as[String]
    val password = request.body.asJson.get("userPassword").as[String]
    val isAdmin = request.body.asJson.get("isAdmin").as[Boolean]
    usersDAO.update(id, Option(socialID), email, firstName, lastName, Option(address), Option(password), isAdmin).map { user =>
      Ok(Json.toJson(user))
    }
  }

  def deleteUser(id: Long) = Action.async { implicit request =>
    usersDAO.delete(id).map { user =>
      Ok(Json.toJson(user))
    }
  }

}
