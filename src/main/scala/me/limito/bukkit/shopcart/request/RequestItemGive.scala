package me.limito.bukkit.shopcart.request

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RequestItemGive(requestManager: RequestManager, commandSender: CommandSender, itemId: Int, itemAmount: Int) extends Request(requestManager, commandSender) {
  def handle() {
    requirePermission("cart.get")

    if (!commandSender.isInstanceOf[Player]) {
      sendMessage(lang.get("cart.not-a-player"))
      return
    }

    withDatabase(() => {
      val playerName = commandSender.getName
      val itemInfo = dao.getItemInfoById(itemId)

      itemInfo match {
        case Some(info) if (info.owner == playerName) => {
          val item = info.toItem
          val amount = Math.min(itemAmount, info.amount)

          val amountGiven = item.giveToPlayer(commandSender.asInstanceOf[Player], amount)

          if (amountGiven > 0) {
            info.amount -= amountGiven
            dao.updateItemsAmount(info :: Nil)

            sendMessage(item.getYouGetMessage(amountGiven, lang))
          } else {
            sendMessage(lang.get("cart-get.get-nothing"))
          }
        }
        case _ => sendMessage(lang.get("cart-get.no-such-id"))
      }
    })
  }
}
