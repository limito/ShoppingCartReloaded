package me.limito.bukkit.shopcart.database

import org.bukkit.configuration.ConfigurationSection
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

class DatabaseConfig(val table: String, val columns: Map[String, String], val serverName: Option[String])

object DatabaseConfig {
  def fromYaml(config: ConfigurationSection):DatabaseConfig = {
    val table = config.getString("table")
    val columns = sectionToMap(config.getConfigurationSection("column"))
    val serverName = if(config.getBoolean("multiserver.enabled")) Some(config.getString("multiserver.server")) else None

    new DatabaseConfig(table, columns, serverName)
  }

  private def sectionToMap(section: ConfigurationSection): Map[String, String] = {
    val mapBuffer = ArrayBuffer[(String, String)]()
    val keys = section.getKeys(false)
    for (key <- keys)
      mapBuffer += (key -> section.getString(key))
    Map(mapBuffer.toArray: _*)
  }
}