package me.limito.bukkit.shopcart.request

import org.bukkit.command.CommandSender

class RequestItemsList(requestManager: RequestManager, commandSender: CommandSender) extends Request(requestManager, commandSender) {
  def handle() {
    requirePermission("cart.list")

    withDatabase(() => {
      val playerName = commandSender.getName
      val itemsInfo = requestManager.plugin.dao.getItemInfos(playerName, 0)
      val items = itemsInfo map (_.toItem)
      val outLines = items map (item => lang.format("cart.item", item.info.id, item.getLocalizedName(lang), item.info.amount))

      withBukkit(() => {
        commandSender.sendMessage(outLines.toArray)
      })
    })
  }
}
