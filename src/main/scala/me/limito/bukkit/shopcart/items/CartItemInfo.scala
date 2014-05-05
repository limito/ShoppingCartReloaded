package me.limito.bukkit.shopcart.items

import me.limito.bukkit.shopcart.ShoppingCartReloaded
import java.util.logging.Level
import me.limito.bukkit.shopcart.optional.nbt.NBTTag
import scala.collection.mutable
import org.bukkit.enchantments.Enchantment

class CartItemInfo(var id: Long,
                   var itemType: String,
                   var item: String,
                   var owner: String,
                   var amount: Int,
                   var extra: String,
                   var server: String = null) {
  def this() = this(0, "item", "0", "-", 1, null, null)

  override def toString = String.valueOf(id) + ": " + item

  def toItem: CartItem = {
    try {
      itemType match {
        case "item" => toMinecraftItem
        case "money" => toMoneyItem
        case "rgown" => toWGRegion(WGOwner)
        case "rgmem" => toWGRegion(WGMember)
        case "permgroup" => toPermGroup
        case "perm" => toPerm
        case _ => new CartItemUnknown()
      }
    } catch {
      case e: Exception =>
        ShoppingCartReloaded.instance.getLogger.log(Level.WARNING, "Error parsing cart item#" + id, e)
        new CartItemUnknown()
    }
  }

  private def toWGRegion(membershipType: WGMembershipType) = new CartItemWG(item, membershipType, amount)
  private def toMoneyItem: CartItemMoney = new CartItemMoney(amount)

  private def toPerm: CartItem = {
    val (groupName, paramsMap) = getMainPartAndParamsMap(item)

    val world = paramsMap.get("world")
    val lifetime = paramsMap.get("lifetime").map(_.toLong)

    if (ShoppingCartReloaded.usePex)
      new CartItemPermPex(groupName, world, lifetime)
    else
      new CartItemPerm(groupName, world)
  }

  private def toPermGroup: CartItem = {
    val (groupName, paramsMap) = getMainPartAndParamsMap(item)

    val world = paramsMap.get("world")
    val lifetime = paramsMap.get("lifetime").map(_.toLong)

    if (ShoppingCartReloaded.usePex)
      new CartItemPermGroupPex(groupName, world, lifetime)
    else
      new CartItemPermGroup(groupName, world)
  }

  private def getMainPartAndParamsMap(str: String): (String, Map[String, String]) = {
    val paramDelimIndex = str.indexOf('?')
    if (paramDelimIndex >= 0) {
      val paramString = str.drop(paramDelimIndex + 1)
      val paramStringMap = paramStringToMap(paramString)
      val mainPart = str.take(paramDelimIndex)

      (mainPart, paramStringMap)
    }
    else
      (item, Map())
  }

  private def paramStringToMap(paramString: String): Map[String, String] = {
    val paramsAsString = paramString.split('&')
    val paramsAsTuples = paramsAsString.map(param => {
      val keyValueDelimiter = param.indexOf('=')
      (param.take(keyValueDelimiter), param.drop(keyValueDelimiter + 1))
    })
    Map(paramsAsTuples: _*)
  }

  private def toMinecraftItem: CartItemItem = {
    val poundEnchantDelimIndex = item.indexOf('#')
    val chestshopEnchantDelimIndex = item.indexOf('-')

    val enchantmentsChestshop =
      if(chestshopEnchantDelimIndex >= 0)
        parseChestshopEnchantments(item.drop(chestshopEnchantDelimIndex + 1))
      else null
    val enchantmentsPound =
      if (enchantmentsChestshop == null && poundEnchantDelimIndex >= 0)
        parsePoundEnchantments(item.drop(poundEnchantDelimIndex + 1))
      else null
    val enchantments = if(enchantmentsChestshop != null) enchantmentsChestshop else enchantmentsPound
    val enchantmentsIndex = if(enchantmentsChestshop != null) chestshopEnchantDelimIndex else poundEnchantDelimIndex

    val mainAndNameWithLore = if (enchantmentsIndex < 0) item else item.take(enchantmentsIndex)
    val Array(main, nameAndLoreString @ _*) = mainAndNameWithLore.split("@", 2)
    val nameAndLore = nameAndLoreString.headOption.map(parseNameAndLore)

    val Array(id, meta @ _*) = main.split(":", 2)

    new CartItemItem(id.toInt, if (meta.isEmpty) 0 else meta.head.toShort, amount, enchantments, parseNBT, nameAndLore)
  }

  private def parseNameAndLore(s: String): NameAndLore = s.split("(?<!\\\\)@", 2) match {
    case Array(name, lore) => NameAndLore(name.replaceAll("\\\\@", "@"), lore)
    case Array(name) => NameAndLore(name.replaceAll("\\\\@", "@"), null)
  }

  private def parseNBT: NBTTag = {
    if (extra == null || extra.isEmpty)
      null
    else
      ShoppingCartReloaded.instance.nbtHelper.parseJson(extra)
  }

  private def parsePoundEnchantments(str: String): Array[LeveledEnchantment] = str.split("#").map (d => {
    val Array(id, level) = d.split(":")
    new LeveledEnchantment(id.toInt, level.toInt)
  })

  private def parseChestshopEnchantments(str: String): Array[LeveledEnchantment] = {
    val base10NumberAsString = new StringBuilder(java.lang.Long.parseLong(str, 32).toString)
    while (base10NumberAsString.length % 3 != 0) {
      base10NumberAsString.insert(0, '0')
    }

    val enchants = new Array[LeveledEnchantment](base10NumberAsString.length / 3)
    for (i <- enchants.indices) {
      val oneEnchantmentSubstring = base10NumberAsString.substring(i * 3, i * 3 + 3)

      val enchId = Integer.parseInt(oneEnchantmentSubstring.substring(0, 2))
      val enchLevel = Integer.parseInt(oneEnchantmentSubstring.substring(2))

      enchants(i) = new LeveledEnchantment(enchId, enchLevel)
    }

    enchants
  }
}