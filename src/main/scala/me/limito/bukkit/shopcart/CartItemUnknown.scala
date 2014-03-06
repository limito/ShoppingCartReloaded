package me.limito.bukkit.shopcart

import org.bukkit.entity.Player

class CartItemUnknown(info: CartItemInfo) extends CartItem(info) {
  def giveToPlayer(player: Player): Int = 0

  def giveToPlayer(player: Player, amount: Int): Int = 0

  def getLocalizedName(lang: Lang): String = lang.get("cart.unknown")

  def getYouGetMessage(amount: Int, lang: Lang): String = "???"
}
