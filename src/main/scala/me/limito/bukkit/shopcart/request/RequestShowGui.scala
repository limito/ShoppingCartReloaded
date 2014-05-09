package me.limito.bukkit.shopcart.request

import org.bukkit.command.CommandSender
import me.limito.bukkit.shopcart.ShoppingCartReloaded
import org.bukkit.entity.Player
import me.limito.bukkit.shopcart.gui.CartInventory

class RequestShowGui(commandSender: CommandSender) extends Request(commandSender) {
  override def prehandle() {
    requirePermission("cartr.user.gui")
  }

  def handle() {
    val playerName = commandSender.getName
    val itemsInfo = ShoppingCartReloaded.instance.dao.getItemInfos(playerName)

    withBukkit(() => {
      val player = commandSender.asInstanceOf[Player]
      val gui = new CartInventory(player, itemsInfo)
      gui.open()
    })
  }
}
