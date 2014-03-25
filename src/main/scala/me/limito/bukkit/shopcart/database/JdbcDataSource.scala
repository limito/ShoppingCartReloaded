package me.limito.bukkit.shopcart.database

import java.util.concurrent.{TimeUnit, LinkedBlockingQueue}
import java.sql.{SQLException, DriverManager, Connection}
import java.util.concurrent.atomic.AtomicInteger
import me.limito.bukkit.shopcart.ShoppingCartReloaded
import java.util.logging.Level
import collection.JavaConversions._

class JdbcDataSource(val config: ConnectionConfig) {
  private val freeConnections = new LinkedBlockingQueue[PooledConnection]()
  private val connCounter = new AtomicInteger()
  private val logger = ShoppingCartReloaded.instance.getLogger

  private var working = true

  def connection(): Connection = this.synchronized {
    var connection: PooledConnection = null
    while (connection == null) {
      if (!working)
        throw new SQLException("Data source is closed")

      connection = freeConnections.poll(50, TimeUnit.MILLISECONDS)
      if (connection != null && !checkConnection(connection)) {
        connection = null
        connCounter.decrementAndGet()
      }

      if (connection == null && connCounter.get() < config.connections)
        connection = newPoolConnection()
    }
    connection
  }

  def returnConnection(conn: PooledConnection) {
    this.synchronized {
      if (working)
        freeConnections.add(conn)
      else
        closeConnection(conn)
    }
  }

  def shutdown() {
    this.synchronized {
      working = false
      freeConnections.foreach(closeConnection)
      connCounter.set(0)
    }
  }

  private def closeConnection(conn: PooledConnection) {
    try {
      conn.base.close()
    } catch {
      case e: Exception => logger.log(Level.WARNING, "Error closing pool connection", e)
    }
  }

  private def newPoolConnection(): PooledConnection = {
    val conn = new PooledConnection(this, newConnection())
    connCounter.incrementAndGet()
    conn
  }

  private def newConnection() = DriverManager.getConnection(config.url, config.username,  config.password)
  private def checkConnection(conn: PooledConnection) = conn.isValid(0)
}