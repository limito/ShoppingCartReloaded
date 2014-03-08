package me.limito.bukkit.shopcart

import java.sql.{Statement, PreparedStatement, ResultSet, Connection}

class CartItemInfoDao(dataSource: JdbcDataSource, config: DatabaseConfig) {
  private val columnNames = s"`${config.columnId}`,`${config.columnType}`,`${config.columnItem}`,`${config.columnOwner}`,`${config.columnAmount}`,`${config.columnExtra}`"

  private val insertStatement = s"INSERT INTO `${config.table}`($columnNames) VALUES (DEFAULT, ?, ?, ?, ?, ?)"
  private val selectAllStatement = s"SELECT $columnNames FROM ${config.table} WHERE `${config.columnOwner}`= ?"
  private val selectStatementById = s"SELECT $columnNames FROM ${config.table} WHERE `${config.columnId}`= ?"
  private val updateAmountStatement = s"UPDATE ${config.table} SET `${config.columnAmount}`=? WHERE `${config.columnId}`= ?"

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
          items foreach (item => {
            query.setInt(1, item.amount)
            query.setLong(2, item.id)
            query.executeUpdate()
          })
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
    val id = rs.getInt(config.columnId)
    val itemType = rs.getString(config.columnType)
    val item = rs.getString(config.columnItem)
    val owner = rs.getString(config.columnOwner)
    val amount = rs.getInt(config.columnAmount)
    val extra = rs.getString(config.columnExtra)
    new CartItemInfo(id, itemType, item, owner, amount, extra)
  }
}
