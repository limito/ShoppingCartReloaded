package me.limito.bukkit.shopcart.request

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import collection.mutable.ListBuffer
import me.limito.bukkit.shopcart.items.CartItem

class RequestGiveAll(requestManager: RequestManager, commandSender: CommandSender) extends Request(requestManager, commandSender) {
  case class ItemGiveInfo(item: CartItem, amount: Int)

  def handle() {
    if (!commandSender.isInstanceOf[Player]) {
      sendMessage(lang.get("cart.not-a-player"))
      return
    }

    withDatabase(() => {
      val playerName = commandSender.getName
      val itemInfos = dao.getItemInfos(playerName, 0)
      val items = itemInfos map (_.toItem)

      val updatedItemsBuffer = new ListBuffer[ItemGiveInfo]

      withBukkit(() => {
        items foreach (item => {
          val given = giveItem(item)
          item.info.amount -= given
          updatedItemsBuffer += new ItemGiveInfo(item, given)
        })

        withDatabase(() => {
          val updatedItems = updatedItemsBuffer.toList

          dao.updateItemsAmount(updatedItems.map(_.item.info).toList)
          sendMessages(updatedItems.map(update => update.item.getYouGetMessage(update.amount, lang)))
        })
      })
    })
  }

  private def giveItem(item: CartItem): Int = {
    try {
      item.giveToPlayer(commandSender.asInstanceOf[Player])
    } catch {
      case ex: Exception => ex.printStackTrace()
      0
    }
  }
}
