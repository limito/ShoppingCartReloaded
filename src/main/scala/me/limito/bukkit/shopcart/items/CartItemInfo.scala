package me.limito.bukkit.shopcart.items


class CartItemInfo(var id: java.lang.Long, var itemType: String, var item: String, var owner: String, var amount: Int, var extra: String) {
  override def toString = id + ": " + item

  def toItem: CartItem = {
    try {
      itemType match {
        case "item" => toMinecraftItem
        case _ => new CartItemUnknown(this)
      }
    } catch {
      case e: Exception => new CartItemUnknown(this)
    }
  }

  private def toMinecraftItem: CartItemItem = {
    val Array(main, enchants @ _*) = item.split("#", 2)
    val Array(id, meta @ _*) = main.split(":", 2)

    val ench = if(enchants.size > 0) parsePoundEnchantments(enchants(0)) else null
    new CartItemItem(this, id.toInt, if (meta.isEmpty) 0 else meta.head.toShort, amount, ench, null)
  }

  private def parsePoundEnchantments(str: String): Array[LeveledEnchantment] = str.split("#").map (d => {
    val Array(id, level) = d.split(":")
    new LeveledEnchantment(id.toInt, level.toInt)
  })
}