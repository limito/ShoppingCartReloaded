package me.limito.bukkit.shopcart.database

import java.sql.{Statement, PreparedStatement, ResultSet, Connection}
import me.limito.bukkit.shopcart.items.CartItemInfo

class CartItemInfoDao(dataSource: JdbcDataSource, config: DatabaseConfig) {
  private val columns = config.columns
  private val columnId = columns("id")
  private val columnType = columns("type")
  private val columnItem = columns("item")
  private val columnPlayer = columns("player")
  private val columnAmount = columns("amount")
  private val columnExtra = columns("extra")
  private val columnServer = columns("server")
  private val table = config.table

  private val columnNames = s"$columnId,$columnType,$columnItem,$columnPlayer,$columnAmount,$columnExtra"

  private var insertStatement: String = _
  private var selectAllStatement: String = _
  private var selectStatementById: String = _
  private var updateAmountStatement: String = _
  private var deleteItemStatement: String = _

  setupStatements()
  def setupStatements() {
    val server = config.serverName.getOrElse(null)

    if (server == null) {
      insertStatement = s"INSERT INTO $table($columnNames) VALUES (DEFAULT, ?, ?, ?, ?, ?)"
      selectAllStatement = s"SELECT $columnNames FROM $table WHERE $columnPlayer = ?"
      selectStatementById = s"SELECT $columnNames FROM $table WHERE $columnId = ?"
      updateAmountStatement = s"UPDATE $table SET $columnAmount = ? WHERE $columnId = ?"
      deleteItemStatement = s"DELETE FROM $table WHERE $columnId = ?"
    } else {
      insertStatement = s"INSERT INTO $table($columnNames, $columnServer) VALUES (DEFAULT, ?, ?, ?, ?, ?, '$server')"
      selectAllStatement = s"SELECT $columnNames FROM $table WHERE $columnPlayer = ? AND $columnServer = '$server'"
      selectStatementById = s"SELECT $columnNames FROM $table WHERE $columnId = ? AND $columnServer = '$server'"
      updateAmountStatement = s"UPDATE $table SET $columnAmount = ? WHERE $columnId = ?"
      deleteItemStatement = s"DELETE FROM $table WHERE $columnId = ?"
    }
  }

  def addItem(info: CartItemInfo): Long = {
    withConnection(
      conn => {
        val query = conn.prepareStatement(insertStatement, Statement.RETURN_GENERATED_KEYS)
        withPrepStatement(query) {
          query.setString(1, info.itemType)
          query.setString(2, info.item)
          query.setString(3, info.owner)
          query.setInt(4, info.amount)
          query.setString(5, info.extra)
          query.executeUpdate()
          val result = query.getGeneratedKeys
          result.next()
          result.getLong(1)
        }
      }
    )
  }

  def getItemInfos(playerName: String, server: Int): List[CartItemInfo] = {
    withConnection(
      conn => {
        val query = conn.prepareStatement(selectAllStatement)
        withPrepStatement(query) {
          query.setString(1, playerName)
          val resultSet = query.executeQuery()
          parseResultSet(resultSet)
        }
      }
    )
  }

  def getItemInfoById(id: Long): Option[CartItemInfo] = {
    withConnection(
      conn => {
        val query = conn.prepareStatement(selectStatementById)
        withPrepStatement(query) {
          query.setLong(1, id)
          val resultSet = query.executeQuery()
          parseResultSet(resultSet).headOption
        }
      }
    )
  }

  def updateItemsAmount(items: List[CartItemInfo]) {
    withConnection(
      conn => {
        val query = conn.prepareStatement(updateAmountStatement)
        withPrepStatement(query) {
          for (item <- items; if item.amount > 0) {
            query.setInt(1, item.amount)
            query.setLong(2, item.id)
            query.executeUpdate()
          }
        }

        val deleteQuery = conn.prepareStatement(deleteItemStatement)
        withPrepStatement(deleteQuery) {
          for (item <- items; if item.amount <= 0) {
            deleteQuery.setLong(1, item.id)
            deleteQuery.executeUpdate()
          }
        }
      }
    )
  }

  private def withPrepStatement[T](statement: PreparedStatement)(action: => T): T = {
    try {
      action
    } finally {
      statement.close()
    }
  }

  private def withConnection[T](action: Connection => T): T = {
    var connection: Connection = null
    try {
      connection = dataSource.connection()
      action(connection)
    } catch {
      case ex: Exception => throw new DaoException(ex)
    } finally {
      if (connection != null)
        connection.close()
    }
  }

  private def parseResultSet(rs: ResultSet): List[CartItemInfo] = parseResultSet(rs, Nil)
  private def parseResultSet(rs: ResultSet, list: List[CartItemInfo]): List[CartItemInfo] = if (rs.next()) parseInfo(rs) :: parseResultSet(rs, list) else list

  private def parseInfo(rs: ResultSet):CartItemInfo = {
    val id = rs.getInt(columnId)
    val itemType = rs.getString(columnType)
    val item = rs.getString(columnItem)
    val player = rs.getString(columnPlayer)
    val amount = rs.getInt(columnAmount)
    val extra = rs.getString(columnExtra)
    new CartItemInfo(id, itemType, item, player, amount, extra)
  }
}
