package me.limito.bukkit.shopcart

import org.bukkit.plugin.java.JavaPlugin
import java.sql.DriverManager
import java.io.File
import org.bukkit.configuration.file.YamlConfiguration
import java.util.logging.Level
import org.bukkit.command.{Command, CommandSender, CommandExecutor}

class ShoppingCartReloaded extends JavaPlugin {
  val requestManager: RequestManager = new RequestManager(this)
  val lang = new Lang
  var dao: CartItemInfoDao = _

  override def onEnable() {
    val dataSource = new JdbcDataSource("jdbc:mysql://localhost:3306/shopcart", "root",  "", 4)
    val dbConfig = new DatabaseConfig("shopcart", "id", "type", "item", "player", "amount", "extra")
    dao = new CartItemInfoDao(dataSource, dbConfig)

    dao.getItems("limito", 0) foreach(println)

    getServer.getPluginCommand("cart").setExecutor(this)

    loadMessages()
  }

  override def onDisable() {

  }

  def loadMessages() {
    try {
      val fileName = "messages.yml"

      saveResource(fileName, false)
      val file = new File(getDataFolder, fileName)
      val config = YamlConfiguration.loadConfiguration(file)
      lang.read(config)
    } catch {
      case e: Exception => getLogger.log(Level.SEVERE, s"Error loading messages", e)
    }
  }

  override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
    val req = new RequestItemsList(requestManager, sender)
    requestManager.handleRequest(req)
    true
  }
}