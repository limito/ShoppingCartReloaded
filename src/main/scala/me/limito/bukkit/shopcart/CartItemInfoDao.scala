package me.limito.bukkit.shopcart

import java.sql.{ResultSet, Connection}

class CartItemInfoDao(dataSource: JdbcDataSource, config: DatabaseConfig) {
  private val selectStatement = s"SELECT * FROM ${config.table} WHERE `${config.columnOwner}`= ?"

  def getItems(playerName: String, server: Int): List[CartItemInfo] = {
    withConnection(
      conn => {
        val query = conn.prepareStatement(selectStatement)
        query.setString(1, playerName)
        val resultSet = query.executeQuery()
        parseResultSet(resultSet)
      }
    )
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
  private def parseResultSet(rs: ResultSet, list: List[CartItemInfo]): List[CartItemInfo] = if (rs.next()) parseInfo(rs) :: list else list

  private def parseInfo(rs: ResultSet):CartItemInfo = {
    val id = rs.getInt(config.columnId)
    val itemType = rs.getString(config.columnType)
    val item = rs.getString(config.columnItem)
    val owner = rs.getString(config.columnOwner)
    val amount = rs.getInt(config.columnAmount)
    val extra = rs.getString(config.columnExtra)
    new CartItemInfo(id, itemType, item, owner, amount, extra)
  }
}
