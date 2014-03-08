package me.limito.bukkit.shopcart

import collection.mutable
import org.bukkit.configuration.{ConfigurationSection, Configuration}
import collection.JavaConversions._
import org.bukkit.Material

class Lang {
  val messageFormats = mutable.Map[String, String]()

  val itemsNames = new Array[String](Short.MaxValue + 1)
  val itemsMetaNames = new Array[Array[String]](Short.MaxValue +  1)

  def read(config: ConfigurationSection) {
    for (key <- config.getKeys(true))
      messageFormats.put(key, config.getString(key))
  }

  def readItems(config: ConfigurationSection) {
    for (key <- config.getKeys(true); value = config.getString(key)) {
      val item = key.split("\\.")
      item match {
        case Array(itemId, "0") => itemsNames(itemId.toInt) = value
        case Array(itemId) => itemsNames(itemId.toInt) = value
        case Array(itemId, itemMeta) => {
          if (itemsMetaNames(itemId.toInt) == null)
            itemsMetaNames(itemId.toInt) = new Array[String](Short.MaxValue + 1)
          itemsMetaNames(itemId.toInt)(itemMeta.toInt) = value
        }
      }
    }
  }

  def get(formatName: String) = messageFormats.getOrElse(formatName, formatName)
  def format(formatName: String, data: Any*):String = get(formatName).format(data: _*)

  def getItemName(id: Int, meta: Int): String = {
    val metaNames = itemsMetaNames(id)
    if (metaNames != null && metaNames(meta) != null)
      metaNames(meta)
    else if (itemsNames(id) != null)
      addMetaSuffix(itemsNames(id), meta)
    else addMetaSuffix(Material.getMaterial(id).toString, meta)
  }

  def addMetaSuffix(str: String, meta: Int) = if (meta > 0) str + ":" + meta else str
}
