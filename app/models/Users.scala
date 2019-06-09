package models

import play.api.libs.json._

case class Users(
  userID: Long,
  userSocialID: Option[String],
  userEmail: String,
  userFirstname: String,
  userLastname: String,
  userAddress: Option[String],
  userPassword: Option[String],
  isAdmin: Boolean)

object Users {
  implicit val usersFormat = Json.format[Users]
}