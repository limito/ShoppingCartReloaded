package me.limito.bukkit.shopcart.items

import org.bukkit.entity.Player
import me.limito.bukkit.shopcart.Lang

abstract class CartItem() {
  def giveToPlayer(player: Player): Int
  def giveToPlayer(player: Player, amount: Int): Int
  def getLocalizedName(lang: Lang): String
  def getYouGetMessage(amount: Int, lang: Lang): String
}