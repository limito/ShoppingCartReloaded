package me.limito.bukkit.shopcart.request

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import me.limito.bukkit.shopcart.items.CartItem
import me.limito.bukkit.shopcart.ShoppingCartReloaded
import java.util.logging.Level

class RequestItemGive(commandSender: CommandSender, itemId: Int, itemAmount: Int) extends Request(commandSender) {


  /** Здесь идет проверка условий для выполнения запроса (например, проверка наличия пермов) **/
  override def prehandle() = {
    requirePermission("cart.get")

    if (!commandSender.isInstanceOf[Player]) {
      sendMessage(lang.get("cart.not-a-player"))
    }
  }

  def handle() {
    val playerName = commandSender.getName
    val itemInfo = dao.getItemInfoById(itemId)

    itemInfo match {
      case Some(info) if info.owner == playerName =>
        val item = info.toItem
        val amount = Math.min(itemAmount, info.amount)
        var amountGiven: Int = 0

        withBukkit(() => {
          amountGiven = giveItem(item, amount)
        })

        if (amountGiven > 0) {
          info.amount -= amountGiven
          dao.updateItems(info :: Nil)

          sendMessage(item.getYouGetMessage(amountGiven, lang))
        } else {
          sendMessage(lang.get("cart-get.get-nothing"))
        }
      case _ => sendMessage(lang.get("cart-get.no-such-id"))
    }
  }

  private def giveItem(item: CartItem, amount: Int): Int = {
    try {
      item.giveToPlayer(commandSender.asInstanceOf[Player], amount)
    } catch {
      case ex: Exception => ShoppingCartReloaded.instance.getLogger.log(Level.WARNING, "Error giving item", ex)
      0
    }
  }
}
