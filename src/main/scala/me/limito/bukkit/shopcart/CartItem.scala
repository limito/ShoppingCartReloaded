package me.limito.bukkit.shopcart

import org.bukkit.entity.Player

abstract class CartItem(val info: CartItemInfo) {
  def giveToPlayer(player: Player): Int
  def giveToPlayer(player: Player, amount: Int): Int
  def getLocalizedName(lang: Lang): String
  def getYouGetMessage(amount: Int, lang: Lang): String
}