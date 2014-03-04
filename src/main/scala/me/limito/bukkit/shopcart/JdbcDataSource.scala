package me.limito.bukkit.shopcart

import java.util.concurrent.{TimeUnit, LinkedBlockingQueue}
import java.sql.{DriverManager, Connection}
import java.util.concurrent.atomic.AtomicInteger

class JdbcDataSource(val url: String, val username: String, val password: String, val poolSize: Int) {
  private val freeConnections = new LinkedBlockingQueue[PooledConnection]()
  private val connCounter = new AtomicInteger()

  def connection(): Connection = synchronized {
    var connection: PooledConnection = null
    while (connection == null) {
      connection = freeConnections.poll(50, TimeUnit.MILLISECONDS)
      if (connection != null && !checkConnection(connection)) {
        connection = null
        connCounter.decrementAndGet()
      }

      if (connection == null && connCounter.get() < poolSize)
        connection = newPoolConnection()
    }
    connection
  }

  def returnConnection(conn: PooledConnection) {
    freeConnections.add(conn)
  }

  private def newPoolConnection(): PooledConnection = {
    val conn = new PooledConnection(this, newConnection())
    connCounter.incrementAndGet()
    conn
  }

  private def newConnection() = DriverManager.getConnection(url, username,  password)
  private def checkConnection(conn: PooledConnection) = conn.isValid(0)
}