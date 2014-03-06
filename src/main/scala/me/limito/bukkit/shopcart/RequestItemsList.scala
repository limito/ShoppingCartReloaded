package me.limito.bukkit.shopcart

import org.bukkit.command.CommandSender

class RequestItemsList(requestManager: RequestManager, commandSender: CommandSender) extends Request(requestManager, commandSender) {
  def handle() {
    withDatabase(() => {
      val playerName = commandSender.getName
      val itemsInfo = requestManager.plugin.dao.getItems(playerName, 0)
      val items = itemsInfo map (_.toItem)
      val outLines = items map (item => lang.format("cart.item", item.info.id, item.getLocalizedName(lang), item.info.amount))

      withBukkit(() => {
        commandSender.sendMessage(outLines.toArray)
      })
    })
  }
}
