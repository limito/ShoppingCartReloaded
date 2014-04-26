package me.limito.bukkit.shopcart.database

import me.limito.bukkit.shopcart.items.CartItemInfo
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.dao.{DaoManager, Dao}
import scala.collection.JavaConverters._
import com.j256.ormlite.table.{TableUtils, DatabaseTableConfig}
import com.j256.ormlite.stmt.SelectArg

class CartItemInfoDao(connSource: JdbcConnectionSource, config: DatabaseConfig) extends DaoHelper {
  var dao: Dao[CartItemInfo, Long] = _

  setupStatements()
  def setupStatements() {
    val columns = config.columns
    val fieldsList = List(
      idConfig("id", columns("id")),
      fieldConfig("itemType", columns("type"), "item"),
      fieldConfig("item", columns("item"), nullable = false),
      fieldConfig("owner", columns("player"), nullable = false),
      fieldConfig("amount", columns("amount"), nullable = false),
      fieldConfig("extra", columns("extra"), nullable = true)
    )
    val serverFieldList = if (config.serverName.isDefined) List(fieldConfig("server", columns("server"), nullable = false)) else Nil
    val fullFieldList = (fieldsList ::: serverFieldList).asJava

    val tableConfig = new DatabaseTableConfig(classOf[CartItemInfo], config.table, fullFieldList)
    TableUtils.createTableIfNotExists(connSource, tableConfig)
    dao = DaoManager.createDao(connSource, tableConfig)
  }

  def addItem(info: CartItemInfo): Long = withExceptionHandling {
    if (config.serverName.isDefined && info.server == null)
      info.server = config.serverName.get

    dao.create(info)
    info.id
  }

  def getItemInfos(playerName: String): List[CartItemInfo] =  withExceptionHandling {
    val queryBuilder = dao.queryBuilder()
    val ownerWhere = queryBuilder.where().
      eq(config.columns("player"), new SelectArg(playerName))
    config.serverName match {
      case Some(name) => ownerWhere.and().eq(config.columns("server"), new SelectArg(config.serverName.get))
      case None => ownerWhere
    }
    dao.query(queryBuilder.prepare()).asScala.toList
  }

  def getItemInfoById(id: Long): Option[CartItemInfo] = withExceptionHandling {
    val queryBuilder = dao.queryBuilder()
    if (config.serverName.isDefined) {
      queryBuilder.where().
        eq(config.columns("server"), new SelectArg(config.serverName.get)).
        and().
        eq(config.columns("id"), new SelectArg(id))
    }
    dao.query(queryBuilder.prepare()).asScala.headOption
  }

  def updateItems(items: List[CartItemInfo]) = withExceptionHandling {
    items.foreach(item => if (item.amount > 0) dao.update(item) else dao.delete(item))
  }
}
