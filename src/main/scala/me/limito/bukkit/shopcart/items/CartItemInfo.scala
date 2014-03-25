package me.limito.bukkit.shopcart.items

import me.limito.bukkit.shopcart.ShoppingCartReloaded
import java.util.logging.Level
import me.limito.bukkit.shopcart.optional.nbt.NBTTag


class CartItemInfo(var id: java.lang.Long, var itemType: String, var item: String, var owner: String, var amount: Int, var extra: String) {
  override def toString = id + ": " + item

  def toItem: CartItem = {
    try {
      itemType match {
        case "item" => toMinecraftItem
        case "money" => toMoneyItem
        case _ => new CartItemUnknown()
      }
    } catch {
      case e: Exception =>
        ShoppingCartReloaded.instance.getLogger.log(Level.WARNING, "Error parsing cart item#" + id, e)
        new CartItemUnknown()
    }
  }

  private def toMoneyItem: CartItemMoney = new CartItemMoney(amount)

  private def toMinecraftItem: CartItemItem = {
    val Array(main, enchants @ _*) = item.split("#", 2)
    val Array(id, meta @ _*) = main.split(":", 2)

    val ench = if(enchants.size > 0) parsePoundEnchantments(enchants(0)) else null
    new CartItemItem(id.toInt, if (meta.isEmpty) 0 else meta.head.toShort, amount, ench, parseNBT)
  }

  private def parseNBT: NBTTag = {
    if (extra == null)
      null
    else
      ShoppingCartReloaded.instance.nbtHelper.parseJson(extra)
  }

  private def parsePoundEnchantments(str: String): Array[LeveledEnchantment] = str.split("#").map (d => {
    val Array(id, level) = d.split(":")
    new LeveledEnchantment(id.toInt, level.toInt)
  })
}