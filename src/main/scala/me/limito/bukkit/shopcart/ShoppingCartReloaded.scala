package me.limito.bukkit.shopcart

import me.limito.bukkit.shopcart.database.{ConnectionConfig, CartItemInfoDao, DatabaseConfig}
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import org.bukkit.configuration.file.YamlConfiguration
import java.util.logging.Level
import org.bukkit.command.{CommandExecutor, Command, CommandSender}
import request._
import collection.JavaConversions._
import me.limito.bukkit.shopcart.optional.nbt.{PowerNBTHelper, NBTHelperStub, NBTHelper}
import com.j256.ormlite.jdbc.{JdbcPooledConnectionSource, JdbcConnectionSource}
import com.j256.ormlite.logger.LocalLog
import org.bukkit.Bukkit
import net.minezrc.framework.test.CommandFramework

class ShoppingCartReloaded(val plugin: JavaPlugin) extends CommandExecutor {
  ShoppingCartReloaded.instance = this

  val requestManager: RequestManager = new RequestManager(this)
  var lang: Lang = _
  var dao: CartItemInfoDao = _
  var connectionSource: JdbcConnectionSource = _
  var nbtHelper: NBTHelper = _
  var commandFramework: CommandFramework = _

  def onEnable() {
    commandFramework = new CommandFramework(plugin)
    commandFramework.registerCommands(new Commands(this))

    reload()
  }

  override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
    commandFramework.handleCommand(sender, label, command, args)
  }

  def onDisable() {
    requestManager.shutdown()
  }

  def reload() {
    if (connectionSource != null)
      connectionSource.closeQuietly()

    lang = new Lang()
    dao = null

    initPex()
    loadMessages()
    loadItemNames()
    loadEnchantmentNames()
    initDatabase()
    initNbt()
  }

  def getLogger = plugin.getLogger
  def getServer = plugin.getServer

  def loadMessages() {
    try {
      val config = loadYamlAndMerge("messages.yml")
      lang.read(config)
      commandFramework.setNoPermMessage(lang.get("cart.no-perms"))
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
    try {
      plugin.saveDefaultConfig()
      plugin.reloadConfig()

      val section = plugin.getConfig.getConfigurationSection("db")

      val ormliteLogFile = new File(plugin.getDataFolder, "ormlite.log")
      LocalLog.openLogFile(ormliteLogFile.getAbsolutePath)

      val connConfig = ConnectionConfig.fromYaml(section)
      val dbConfig = DatabaseConfig.fromYaml(section)

      val source = new JdbcPooledConnectionSource(connConfig.url, connConfig.username, connConfig.password)
      source.setTestBeforeGet(true)
      source.setMaxConnectionsFree(2)

      connectionSource = source

      dao = new CartItemInfoDao(connectionSource, dbConfig)

      dao.setupTableAndStatements()
    } catch {
      case e: Exception => getLogger.log(Level.WARNING, "Error setting up DB", e)
    }
  }

  def initPex() {
    if (ShoppingCartReloaded.usePex)
      getLogger.info("Using PermissionEx")
    else
      getLogger.info("Using Vault for permissions")
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
      config.save(new File(plugin.getDataFolder, resource))
    config
  }

  private def copyDefaultIfNeeded(resource: String): File = {
    val file = new File(plugin.getDataFolder, resource)
    if (!file.exists()) {
      plugin.saveResource(resource, false)
    }
    file
  }
}

object ShoppingCartReloaded {
  var instance: ShoppingCartReloaded = _
  val usePex = Bukkit.getPluginManager.getPlugin("PermissionsEx") != null
}