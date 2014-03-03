package me.limito.bukkit.shopcart

import org.bukkit.plugin.java.JavaPlugin
import java.sql.DriverManager

class ShoppingCartReloaded extends JavaPlugin {
  val requestManager: RequestManager = new RequestManager(this)
  var dao: CartItemInfoDao = _

  override def onEnable() {
    val connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/shopcart", "root",  "")
    val dbConfig = new DatabaseConfig("shopcart", "id", "type", "item", "player", "amount", "extra")
    dao = new CartItemInfoDao(connection, dbConfig)

    dao.getItems("limito", 0) foreach(println)
  }

  override def onDisable() {

  }
}