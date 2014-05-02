package me.limito.bukkit.shopcart

import collection.mutable
import items.LeveledEnchantment
import org.bukkit.configuration.{ConfigurationSection, Configuration}
import collection.JavaConversions._
import org.bukkit.{ChatColor, Material}
import me.limito.bukkit.shopcart.Lang.{ExtraLangData, WorldName}
import java.util.concurrent.TimeUnit

object Lang {
  abstract sealed class ExtraLangData {
    def getExtraString(lang: Lang): String
  }
  case class WorldName(name: String) extends ExtraLangData {
    override def getExtraString(lang: Lang): String = lang.format("misc.world", name)
  }
  case class TimeDuration(durationSec: Long) extends ExtraLangData {
    override def getExtraString(lang: Lang): String = lang.format("misc.duration", getIntervalString(lang))

    def getIntervalString(lang: Lang): String = {
      val secondsInMinute = 60L
      val secondsInHours = secondsInMinute * 60
      val secondsInDay = secondsInHours * 24

      var secondsLeft = durationSec
      val days = secondsLeft / secondsInDay
      secondsLeft -= days * secondsInDay
      val hours = secondsLeft / secondsInHours
      secondsLeft -= hours * secondsInHours
      val minutes = secondsLeft / secondsInMinute
      secondsLeft -= minutes * secondsInMinute
      val seconds = secondsLeft

      lang.format("misc.timeinterval", days, hours, minutes, seconds)
    }
  }
}

class Lang {
  val romanNums = Array("0", "I", "II", "III", "IV", "V")

  val messageFormats = mutable.Map[String, String]()

  val itemsNames = new Array[String](Short.MaxValue + 1)
  val itemsMetaNames = new Array[Array[String]](Short.MaxValue +  1)
  val enchantmentTypes = new Array[String](Short.MaxValue)

  def read(config: ConfigurationSection) {
    for (key <- config.getKeys(true))
      messageFormats.put(key, config.getString(key))
  }

  def readItems(config: ConfigurationSection) {
    for (key <- config.getKeys(true); value = config.getString(key)) {
      val item = key.split("\\.")
      item match {
        case Array(itemId) => itemsNames(itemId.toInt) = value
        case Array(itemId, itemMeta) =>
          if (itemsMetaNames(itemId.toInt) == null)
            itemsMetaNames(itemId.toInt) = new Array[String](Short.MaxValue + 1)
          itemsMetaNames(itemId.toInt)(itemMeta.toInt) = value
      }
    }
  }

  def readEnchantments(config: ConfigurationSection) {
    for (key <- config.getKeys(false)) {
      enchantmentTypes(key.toInt) = config.getString(key)
    }
  }

  def get(formatName: String) = messageFormats.getOrElse(formatName, formatName)
  def format(formatName: String, data: Any*):String = get(formatName).format(data: _*)
  def formatExtra(formatName: String, extra: Seq[ExtraLangData], data: Any*): String = {
    val formatted = get(formatName).format(data: _*)
    val extraString = createExtra(extra)
    if (extraString != null) formatted + extraString else formatted
  }

  def createExtra(extra: Seq[ExtraLangData]): String = {
    val extraString = if (extra.length > 0) extra.map(_.getExtraString(this)).mkString(ChatColor.WHITE + " (", ChatColor.WHITE + ", ", ")") else null
    if (extraString != null)
      extraString
    else
      null
  }

  def formatSubtype(formatName: String, param: Any) = {
    messageFormats.getOrElse(formatName + "." + param, get(formatName + ".default")).format(param)
  }

  def formatSubtypeExtra(formatName: String, param: Any, extra: Seq[ExtraLangData]) = {
    val formatted = formatSubtype(formatName, param)
    val extraString = createExtra(extra)
    if (extraString != null) formatted + extraString else formatted
  }

  def getItemName(id: Int, meta: Int): String = {
    def addMetaSuffix(str: String, meta: Int) = if (meta > 0) str + ":" + meta else str

    val metaNames = itemsMetaNames(id)
    if (metaNames != null && metaNames(meta) != null)
      metaNames(meta)
    else if (itemsNames(id) != null)
      addMetaSuffix(itemsNames(id), meta)
    else addMetaSuffix(Material.getMaterial(id).toString, meta)
  }

  def formatEnchantments(ench: Array[LeveledEnchantment]):String = ench map(e => enchantmentTypes(e.id) + " " + romanNum(e.level)) mkString(ChatColor.AQUA.toString, ", ", "")
  def romanNum(a: Int) = if (a >= 0 && a < romanNums.length) romanNums(a) else a.toString
}
