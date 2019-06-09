package models.daos

import javax.inject.{ Inject, Singleton }
import models.Categories
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class CategoriesDAO @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  val categories = TableQuery[CategoriesTable]

  def all(): Future[Seq[Categories]] = db.run {
    categories.result
  }

  def getById(id: Long): Future[Seq[Categories]] = db.run {
    categories.filter(_.categoryID === id).result
  }

  def getByName(name: String): Future[Seq[Categories]] = db.run {
    categories.filter(_.categoryName === name).result
  }

  def create(name: String): Future[Categories] = db.run {
    (categories.map(c => (c.categoryName))
      returning categories.map(_.categoryID)
      into {
        case ((name), id) => Categories(id, name)
      }) += (name)
  }

  def delete(categoryID: Long): Future[Int] = db.run {
    categories.filter(_.categoryID === categoryID).delete
  }

  def update(categoryID: Long, categoryName: String): Future[Int] = db.run {
    categories.filter(_.categoryID === categoryID).map(cat => cat.categoryName).update(categoryName)
  }

  class CategoriesTable(tag: Tag) extends Table[Categories](tag, "categories") {
    def categoryID = column[Long]("categoryID", O.PrimaryKey, O.AutoInc)
    def categoryName = column[String]("categoryName")

    def * = (categoryID, categoryName) <> ((Categories.apply _).tupled, Categories.unapply)
  }
}
