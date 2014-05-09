package me.limito.bukkit.shopcart.request

import org.bukkit.command.CommandSender
import me.limito.bukkit.shopcart.ShoppingCartReloaded
import org.bukkit.entity.Player
import me.limito.bukkit.shopcart.gui.{CartInventoryNew, CartInventory}

class RequestShowGui(commandSender: CommandSender) extends Request(commandSender) {
  override def prehandle() {
    requirePermission("cartr.user.gui")
  }

  def handle() {
    val playerName = commandSender.getName
    val itemsInfo = ShoppingCartReloaded.instance.dao.getItemInfos(playerName)

    withBukkit(() => {
      val player = commandSender.asInstanceOf[Player]
      if (classExists("org.bukkit.event.inventory.InventoryDragEvent")) {
        val gui = new CartInventoryNew(player, itemsInfo)
        gui.open()
      } else {
        val gui = new CartInventory(player, itemsInfo)
        gui.open()
      }
    })
  }

  private def classExists(className: String) = {
    try {
      Class.forName(className)
      true
    }
    catch {
      case e: ClassNotFoundException => false
    }
  }
}
