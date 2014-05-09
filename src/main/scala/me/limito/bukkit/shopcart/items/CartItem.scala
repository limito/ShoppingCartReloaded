package me.limito.bukkit.shopcart.items

import org.bukkit.entity.Player
import me.limito.bukkit.shopcart.Lang
import org.bukkit.inventory.ItemStack
import org.bukkit.Material

abstract class CartItem() {
  def giveToPlayer(player: Player): Int
  def giveToPlayer(player: Player, amount: Int): Int
  def getLocalizedName(lang: Lang): String
  def getYouGetMessage(amount: Int, lang: Lang): String

  def getIcon: ItemStack = new ItemStack(Material.PAPER)
}