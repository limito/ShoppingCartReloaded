package me.limito.bukkit.shopcart.database

import org.bukkit.configuration.ConfigurationSection

class ConnectionConfig(val url: String, val username: String, val password: String, val connections: Int)
object ConnectionConfig {
  def fromYaml(section: ConfigurationSection):ConnectionConfig = {
    val url = section.getString("url")
    val username = section.getString("username")
    val password = section.getString("password")
    val connections = section.getInt("connections")

    new ConnectionConfig(url, username, password, connections)
  }
}