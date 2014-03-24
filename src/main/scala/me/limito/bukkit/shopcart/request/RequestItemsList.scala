package me.limito.bukkit.shopcart.request

import org.bukkit.command.CommandSender
import me.limito.bukkit.shopcart.ShoppingCartReloaded

class RequestItemsList(commandSender: CommandSender) extends Request(commandSender) {
  override def prehandle() {
    requirePermission("cart.list")
  }

  def handle() {
    val playerName = commandSender.getName
    val itemsInfo = ShoppingCartReloaded.instance.dao.getItemInfos(playerName, 0)
    val outLines = itemsInfo map (info => {
      val item = info.toItem; lang.format("cart.item", info.id, item.getLocalizedName(lang), info.amount)
    })
    val countMessage = lang.getParametrizedMessage("cart.n-items", itemsInfo.size)

    withBukkit(() => {
      commandSender.sendMessage(countMessage)
      commandSender.sendMessage(outLines.toArray)
    })
  }
}
