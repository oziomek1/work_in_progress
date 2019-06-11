package models.daos

import javax.inject.{ Inject, Singleton }
import models.Products
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ProductsDAO @Inject() (dbConfigProvider: DatabaseConfigProvider, val categoriesDAO: CategoriesDAO)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private val products = TableQuery[ProductsTable]

  def all(): Future[Seq[Products]] = db.run {
    products.result
  }

  def getById(id: Long): Future[Seq[Products]] = db.run {
    products.filter(_.productID === id).result
  }

  def getByCategory(id: Long): Future[Seq[Products]] = db.run {
    products.filter(_.categoryID === id).result
  }

  def getByName(name: String): Future[Seq[Products]] = db.run {
    products.filter(_.productName === name).result
  }

  def create(name: String, description: String, imageURL: String, category: Long, priceNet: Double, priceGross: Double): Future[Products] = db.run {
    (products.map(p => (p.productName, p.productDescription, p.productImageURL, p.categoryID, p.productPriceNet, p.productPriceGross))
      returning products.map(_.productID)
      into {
        case ((name, description, imageURL, category, priceNet, priceGross), id) => Products(id, name, description, imageURL, category, priceNet, priceGross)
      }) += ((name, description, imageURL, category, priceNet, priceGross))
  }

  def delete(productID: Long): Future[Int] = db.run {
    products.filter(_.productID === productID).delete
  }

  def update(productID: Long, productName: String, productImageURL: String, categoryID: Long, productDescription: String, productPriceNet: Double,
    productPriceGross: Double): Future[Int] = db.run {
    products.filter(_.productID === productID)
      .map(prod => (prod.productName, prod.productDescription, productImageURL, prod.categoryID, prod.productPriceNet, prod.productPriceGross))
      .update((productName, productDescription, productImageURL, categoryID, productPriceNet, productPriceGross))
  }

  class ProductsTable(tag: Tag) extends Table[Products](tag, "products") {
    def productID = column[Long]("productID", O.PrimaryKey, O.AutoInc)
    def productName = column[String]("productName")
    def productDescription = column[String]("productDescription")
    def productImageURL = column[String]("productImageURL")
    def productPriceNet = column[Double]("productPriceNet")
    def productPriceGross = column[Double]("productPriceGross")
    def categoryID = column[Long]("categoryID")

    def * = (productID, productName, productDescription, productImageURL, categoryID, productPriceNet, productPriceGross) <> ((Products.apply _).tupled, Products.unapply)
  }
}
