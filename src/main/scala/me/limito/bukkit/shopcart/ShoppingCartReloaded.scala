package me.limito.bukkit.shopcart

import database.{CartItemInfoDao, JdbcDataSource, DatabaseConfig}
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import org.bukkit.configuration.file.YamlConfiguration
import java.util.logging.Level
import org.bukkit.command.{Command, CommandSender}
import request._
import scala.Predef.augmentString
import org.bukkit.entity.Player

class ShoppingCartReloaded extends JavaPlugin {
  ShoppingCartReloaded.instance = this

  val requestManager: RequestManager = new RequestManager(this)
  var lang: Lang = _
  var dao: CartItemInfoDao = _
  var dataSource: JdbcDataSource = _

  override def onEnable() {
    reload()
    getServer.getPluginCommand("cart").setExecutor(this)
  }

  override def onDisable() {
    requestManager.shutdown()
  }

  def reload() {
    if (dataSource != null)
      dataSource.shutdown()

    lang = new Lang()
    dao = null

    loadMessages()
    loadItemNames()
    loadEnchantmentNames()
    initDatabase()
  }

  def loadMessages() {
    try {
      val config = loadYamlOrDefault("messages.yml")
      lang.read(config)
    } catch {
      case e: Exception => getLogger.log(Level.SEVERE, s"Error loading messages", e)
    }
  }

  def loadItemNames() {
    try {
      val config = loadYamlOrDefault("items.yml")
      lang.readItems(config)
    } catch {
      case e: Exception => getLogger.log(Level.SEVERE, s"Error loading item names", e)
    }
  }

  def loadEnchantmentNames() {
    try {
      val config = loadYamlOrDefault("enchantments.yml")
      lang.readEnchantments(config.getConfigurationSection("enchantments"))
    } catch {
      case e: Exception => getLogger.log(Level.SEVERE, s"Error loading enchantment names", e)
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
    args match {
      case Array("reload") if sender.hasPermission("cart.reload") => reload(); true
      case Array("get", itemId, itemAmount) =>
        val req = new RequestItemGive(sender, itemId.toInt, itemAmount.toInt)
        requestManager.handleRequest(req)
        true
      case Array("get", itemId) =>
        val req = new RequestItemGive(sender, itemId.toInt, Int.MaxValue)
        requestManager.handleRequest(req)
        true
      case Array("all") =>
        val req = new RequestGiveAll(sender)
        requestManager.handleRequest(req)
        true
      case Array("put") if sender.isInstanceOf[Player] =>
        val stack = sender.asInstanceOf[Player].getItemInHand
        if (stack != null && stack.getTypeId > 0) {
          val req = new RequestPutItem(sender, sender.getName, stack.clone(), stack.getAmount)
          requestManager.handleRequest(req)
        }
        true
      case Array() =>
        val req = new RequestItemsList(sender)
        requestManager.handleRequest(req)
        true
      case _ => false
    }
  }

  private def loadYamlOrDefault(resource: String): YamlConfiguration = {
    val file = copyDefaultIfNeeded(resource)
    YamlConfiguration.loadConfiguration(file)
  }

  private def copyDefaultIfNeeded(resource: String): File = {
    val file = new File(getDataFolder, resource)
    if (!file.exists()) {
      saveResource(resource, false)
    }
    file
  }
}

object ShoppingCartReloaded {
  var instance: ShoppingCartReloaded = _
}