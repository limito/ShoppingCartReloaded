package me.limito.bukkit.shopcart

import org.bukkit.entity.Player

abstract class CartItem {
  def giveToPlayer(player: Player)
  def getLocalizedName(lang: Lang): String
  def getYouGetMessage(amount: Int, lang: Lang): String
}
