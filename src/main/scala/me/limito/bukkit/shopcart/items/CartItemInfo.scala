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

  private def toMinecraftItem: CartItemItem = new CartItemItem(this, item.toInt, 0, amount, null)
}