package me.limito.bukkit.shopcart.items

import me.limito.bukkit.shopcart.Lang
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import net.milkbowl.vault.permission.Permission

class CartItemPermGroup(groupName: String) extends CartItem {
  override def getYouGetMessage(amount: Int, lang: Lang): String = lang.format("cart-get.get-permgroup", groupName)

  override def getLocalizedName(lang: Lang): String = lang.format("cart.perm", groupName)


  override def giveToPlayer(player: Player, amount: Int): Int = {
    val rsp = Bukkit.getServer.getServicesManager.getRegistration(classOf[Permission])
    if (rsp != null) {
      val permProvider = rsp.getProvider
      if (!permProvider.playerInGroup(player, groupName)) {
        permProvider.playerAddGroup(null.asInstanceOf[String], player.getName, groupName)
        1
      } else 0
    } else 0
  }

  override def giveToPlayer(player: Player): Int = giveToPlayer(player, 1)
}
