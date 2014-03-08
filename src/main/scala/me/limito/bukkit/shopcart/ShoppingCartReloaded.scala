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
    loadMessages()
    initDatabase()

    getServer.getPluginCommand("cart").setExecutor(this)
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

  def initDatabase() {
    val dbConfig = loadDatabaseConfig()

    val dataSource = new JdbcDataSource(dbConfig.url, dbConfig.username,  dbConfig.password, 4)
    dao = new CartItemInfoDao(dataSource, dbConfig)
  }

  def loadDatabaseConfig(): DatabaseConfig = {
    saveDefaultConfig()
    val section = getConfig.getConfigurationSection("db")

    val url = section.getString("url")
    val username = section.getString("username")
    val password = section.getString("password")
    val table = section.getString("table")

    val columnSection = section.getConfigurationSection("column")
    val cId = columnSection.getString("id")
    val cPlayer = columnSection.getString("player")
    val cType = columnSection.getString("type")
    val cItem = columnSection.getString("item")
    val cAmount = columnSection.getString("amount")
    val cExtra = columnSection.getString("extra")

    new DatabaseConfig(url, username, password, table, cId, cType, cItem, cPlayer, cAmount, cExtra)
  }

  override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
    val req = new RequestItemsList(requestManager, sender)
    requestManager.handleRequest(req)
    true
  }
}