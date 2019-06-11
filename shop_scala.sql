BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "products" (
	"productID"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	"productName"	TEXT NOT NULL,
	"productDescription"	TEXT,
	"categoryID"	INTEGER NOT NULL,
	"productImageURL" TEXT,
	"productPriceNet"	REAL,
	"productPriceGross"	REAL,
	FOREIGN KEY("categoryID") REFERENCES "categories"("categoryID")
);
CREATE TABLE IF NOT EXISTS "orderDetails" (
	"orderDetailID"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	"orderID"	INTEGER NOT NULL,
	"productQuantity"	INTEGER DEFAULT 1,
	"productID"	REAL NOT NULL,
	"orderDetailPriceNet"	REAL,
	"orderDetailPriceGross"	REAL,
	FOREIGN KEY("orderID") REFERENCES "orders"("orderID"),
	FOREIGN KEY("productID") REFERENCES "products"("productID")
);
CREATE TABLE IF NOT EXISTS "users" (
  "userID" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	"userSocialID" TEXT,
	"userEmail"	TEXT NOT NULL,
	"userFirstname"	TEXT,
	"userLastname"	TEXT,
	"userAddress"	TEXT,
	"userPassword" TEXT,
	"isAdmin" INTEGER
);
CREATE TABLE IF NOT EXISTS "orders" (
	"orderID"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	"userID"	TEXT,
	"orderAddress"	INTEGER,
	"orderDate"	NUMERIC,
	"orderShipped"	NUMERIC NOT NULL DEFAULT 0,
	FOREIGN KEY("userID") REFERENCES "users"("userSocialID")
);
CREATE TABLE IF NOT EXISTS "categories" (
	"categoryID"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	"categoryName"	INTEGER NOT NULL
);
COMMIT;
