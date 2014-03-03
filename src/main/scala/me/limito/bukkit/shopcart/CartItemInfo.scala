package me.limito.bukkit.shopcart

class CartItemInfo(val id: Long, val itemType: String, val item: String, val owner: String, val amount: Int, val extra: String) {
  override def toString = id + ": " + item
}