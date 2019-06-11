package models

import java.util.UUID

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import play.api.libs.json._

import scala.util.{ Failure, Success, Try }

/**
 * The user object.
 *
 * @param userID The unique ID of the user.
 * @param loginInfo The linked login info.
 * @param firstName Maybe the first name of the authenticated user.
 * @param lastName Maybe the last name of the authenticated user.
 * @param fullName Maybe the full name of the authenticated user.
 * @param address Maybe the address of the authenticated user.
 * @param email Maybe the email of the authenticated provider.
 * @param avatarURL Maybe the avatar URL of the authenticated provider.
 * @param activated Indicates that the user has activated its registration.
 */
case class User(
  userID: UUID,
  loginInfo: LoginInfo,
  firstName: Option[String],
  lastName: Option[String],
  fullName: Option[String],
  address: Option[String],
  email: Option[String],
  isAdmin: Boolean,
  avatarURL: Option[String],
  activated: Boolean) extends Identity {

  /**
   * Tries to construct a name.
   *
   * @return Maybe a name.
   */
  def name = fullName.orElse {
    firstName -> lastName match {
      case (Some(f), Some(l)) => Some(f + " " + l)
      case (Some(f), None) => Some(f)
      case (None, Some(l)) => Some(l)
      case _ => None
    }
  }

}
object User {
  implicit val reader = Json.reads[User]
  implicit val writer = Json.writes[User]

  implicit val loginInfoReader = Json.reads[LoginInfo]
  implicit val loginInfoWriter = Json.writes[LoginInfo]

  implicit object UserWrites extends OWrites[User] {
    def writes(user: User): JsObject = {
      Json.obj(
        "userID" -> user.userID,
        "loginInfo" -> Json.obj(
          "providerID" -> user.loginInfo.providerID,
          "providerKey" -> user.loginInfo.providerKey
        ),
        "fullName" -> user.fullName,
        "email" -> user.email,
        "firstName" -> user.firstName,
        "lastName" -> user.lastName,
        "address" -> user.address,
        "isAdmin" -> user.isAdmin,
        "avatarURL" -> user.avatarURL,
        "activated" -> user.activated
      )
    }
  }
  implicit object UserReads extends Reads[User] {
    def reads(json: JsValue): JsResult[User] = json match {
      case user: JsObject =>
        Try {
          val userID = (user \ "_userID" \ "$ouserID").as[UUID]

          val providerId = (user \ "loginInfo" \ "providerID").as[String]
          val providerKey = (user \ "loginInfo" \ "providerKey").as[String]

          val fullName = (user \ "fullName").asOpt[String]
          val email = (user \ "email").asOpt[String]
          val firstName = (user \ "firstName").asOpt[String]
          val lastName = (user \ "lastName").asOpt[String]
          val address = (user \ "address").asOpt[String]
          val isAdmin = (user \ "isAdmin").as[Boolean]
          val avatarURL = (user \ "avatarURL").asOpt[String]
          val activated = (user \ "activated").as[Boolean]

          JsSuccess(
            new User(
              userID,
              new LoginInfo(providerId, providerKey),
              fullName,
              email,
              firstName,
              lastName,
              address,
              isAdmin,
              avatarURL,
              activated
            )
          )
        } match {
          case Success(value) => value
          case Failure(cause) => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

}