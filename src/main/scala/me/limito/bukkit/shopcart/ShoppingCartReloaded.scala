package me.limito.bukkit.shopcart

import me.limito.bukkit.shopcart.database.{ConnectionConfig, CartItemInfoDao, JdbcDataSource, DatabaseConfig}
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import org.bukkit.configuration.file.YamlConfiguration
import java.util.logging.Level
import org.bukkit.command.{Command, CommandSender}
import request._
import scala.Predef.augmentString
import org.bukkit.entity.Player
import collection.JavaConversions._
import me.limito.bukkit.shopcart.optional.nbt.{PowerNBTHelper, NBTHelperStub, NBTHelper}
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.logger.{LocalLog, Logger}

class ShoppingCartReloaded extends JavaPlugin {
  ShoppingCartReloaded.instance = this

  val requestManager: RequestManager = new RequestManager(this)
  var lang: Lang = _
  var dao: CartItemInfoDao = _
  var connectionSource: JdbcConnectionSource = _

  var nbtHelper: NBTHelper = _

  override def onEnable() {
    reload()
    getServer.getPluginCommand("cart").setExecutor(this)
  }

  override def onDisable() {
    requestManager.shutdown()
  }

  def reload() {
    if (connectionSource != null)
      connectionSource.closeQuietly()

    lang = new Lang()
    dao = null

    loadMessages()
    loadItemNames()
    loadEnchantmentNames()
    initDatabase()
    initNbt()
  }

  def loadMessages() {
    try {
      val config = loadYamlAndMerge("messages.yml")
      lang.read(config)
    } catch {
      case e: Exception => getLogger.log(Level.SEVERE, s"Error loading messages", e)
    }
  }

  def loadItemNames() {
    try {
      val config = loadYamlAndMerge("items.yml")
      lang.readItems(config)
    } catch {
      case e: Exception => getLogger.log(Level.SEVERE, s"Error loading item names", e)
    }
  }

  def loadEnchantmentNames() {
    try {
      val config = loadYamlAndMerge("enchantments.yml")
      lang.readEnchantments(config.getConfigurationSection("enchantments"))
    } catch {
      case e: Exception => getLogger.log(Level.SEVERE, s"Error loading enchantment names", e)
    }
  }

  def initDatabase() {
    saveDefaultConfig()
    val section = getConfig.getConfigurationSection("db")

    val ormliteLogFile = new File(getDataFolder, "ormlite.log")
    LocalLog.openLogFile(ormliteLogFile.getAbsolutePath)

    val connConfig = ConnectionConfig.fromYaml(section)
    val dbConfig = DatabaseConfig.fromYaml(section)

    connectionSource = new JdbcConnectionSource(connConfig.url, connConfig.username, connConfig.password)
    dao = new CartItemInfoDao(connectionSource, dbConfig)
  }

  def initNbt() {
    try {
      Class.forName("me.dpohvar.powernbt.nbt.NBTBase")
      nbtHelper = new PowerNBTHelper
      getLogger.info("Detected PowerNBT")
    } catch {
      case _: ClassNotFoundException =>
        getLogger.info("PowerNBT not detected")
        nbtHelper = new NBTHelperStub
    }
  }

  override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
    args match {
      case Array("reload") if sender.hasPermission("cart.reload") => reload()
      case Array("get", itemId, itemAmount) =>
        val req = new RequestItemGive(sender, itemId.toInt, itemAmount.toInt)
        requestManager.handleRequest(req)
      case Array("get", itemId) =>
        val req = new RequestItemGive(sender, itemId.toInt, Int.MaxValue)
        requestManager.handleRequest(req)
      case Array("all") =>
        val req = new RequestGiveAll(sender)
        requestManager.handleRequest(req)
      case Array("put") if sender.isInstanceOf[Player] =>
        val stack = sender.asInstanceOf[Player].getItemInHand
        if (stack != null && stack.getTypeId > 0) {
          val req = new RequestPutItem(sender, sender.getName, stack.clone(), stack.getAmount)
          requestManager.handleRequest(req)
        } else sender.sendMessage(lang.get("cart-put.no-item"))
      case Array() =>
        val req = new RequestItemsList(sender)
        requestManager.handleRequest(req)
      case _ => return false
    }
    true
  }

  private def loadYamlOrDefault(resource: String): YamlConfiguration = {
    val file = copyDefaultIfNeeded(resource)
    YamlConfiguration.loadConfiguration(file)
  }

  private def loadYamlAndMerge(resource: String): YamlConfiguration = {
    val config = loadYamlOrDefault(resource)
    val defaultConfig = YamlConfiguration.loadConfiguration(getClass.getResourceAsStream("/" + resource))
    var somethingMerged = false

    for (key <- defaultConfig.getKeys(true))
      if (!config.contains(key)) {
        config.set(key, defaultConfig.get(key))
        somethingMerged = true
      }
    if (somethingMerged)
      config.save(new File(getDataFolder, resource))
    config
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