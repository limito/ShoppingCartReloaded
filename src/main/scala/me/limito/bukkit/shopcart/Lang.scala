package me.limito.bukkit.shopcart

import collection.mutable
import org.bukkit.configuration.{ConfigurationSection, Configuration}
import collection.JavaConversions._

class Lang {
  val messageFormats = mutable.Map[String, String]()

  def read(config: ConfigurationSection) {
    for (key <- config.getKeys(true))
      messageFormats.put(key, config.getString(key))
  }
  def get(formatName: String) = messageFormats.getOrElse(formatName, formatName)
  def format(formatName: String, data: Any*):String = get(formatName).format(data: _*)
}
