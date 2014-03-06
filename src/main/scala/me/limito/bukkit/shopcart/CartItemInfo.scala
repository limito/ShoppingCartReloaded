package me.limito.bukkit.shopcart

class CartItemInfo(val id: Long, val itemType: String, val item: String, val owner: String, val amount: Int, val extra: String) {
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