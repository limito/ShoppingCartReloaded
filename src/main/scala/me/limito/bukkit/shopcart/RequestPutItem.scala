package me.limito.bukkit.shopcart

import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack

class RequestPutItem(requestManager: RequestManager, commandSender: CommandSender, owner: String, itemStack: ItemStack, amount: Int) extends Request(requestManager, commandSender) {
  /** Здесь идет первичная обработка запроса (в игровом потоке) **/
  def handle() {
    val info = createInfo(itemStack)
    withDatabase(() => {
      val id = dao.addItem(info)
      sendMessage(s"Item added (id: $id)")
    })
  }

  private def createInfo(itemStack: ItemStack):CartItemInfo = {
    val itemName = if(itemStack.getDurability == 0) itemStack.getTypeId.toString else itemStack.getTypeId.toString + ":" + itemStack.getDurability.toString

    new CartItemInfo(null, "item", itemName, owner, amount, null)
  }
}
