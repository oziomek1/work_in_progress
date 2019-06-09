package models.daos

import javax.inject.{ Inject, Singleton }
import models.Users
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class UsersDAO @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  val users = TableQuery[UsersTable]

  def all(): Future[Seq[Users]] = db.run { users.result }

  def getByID(id: Long): Future[Seq[Users]] = db.run {
    users.filter(_.userID === id).result
  }

  def getBySocialID(id: String): Future[Seq[Users]] = db.run {
    users.filter(_.userSocialID === id).result
  }

  def create(socialID: Option[String] = None, email: String, firstName: String, lastName: String, address: Option[String] = None, password: Option[String] = None, isAdmin: Boolean) = db.run {
    (users.map(u => (u.userSocialID, u.userEmail, u.userFirstname, u.userLastname, u.userAddress, u.userPassword, u.isAdmin))
      returning users.map(_.userID)
      into {
        case ((socialID, email, firstName, lastName, address, password, isAdmin), id) => Users(id, socialID, email, firstName, lastName, address, password, isAdmin)
      }) += ((socialID, email, firstName, lastName, address, password, isAdmin))
  }

  def delete(userID: Long): Future[Int] = db.run {
    users.filter(_.userID === userID).delete
  }

  def update(userID: Long, socialID: Option[String], email: String, firstName: String, lastName: String, address: Option[String], password: Option[String], isAdmin: Boolean): Future[Int] = db.run {
    users.filter(_.userID === userID)
      .map(usr => (usr.userSocialID, usr.userEmail, usr.userFirstname, usr.userLastname, usr.userAddress, usr.userPassword, usr.isAdmin))
      .update((socialID, email, firstName, lastName, address, password, isAdmin))
  }

  class UsersTable(tag: Tag) extends Table[Users](tag, "users") {
    def userID = column[Long]("userID", O.PrimaryKey, O.AutoInc)
    def userSocialID = column[Option[String]]("userSocialID")
    def userEmail = column[String]("userEmail")
    def userFirstname = column[String]("userFirstname")
    def userLastname = column[String]("userLastname")
    def userAddress = column[Option[String]]("userAddress")
    def userPassword = column[Option[String]]("userPassword")
    def isAdmin = column[Boolean]("isAdmin")

    def * = (userID, userSocialID, userEmail, userFirstname, userLastname, userAddress, userPassword, isAdmin) <> ((Users.apply _).tupled, Users.unapply)
  }
}
