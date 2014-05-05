package me.limito.bukkit.shopcart.request

import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack
import me.limito.bukkit.shopcart.items.{ItemEncoder, CartItemInfo}
import me.limito.bukkit.shopcart.ShoppingCartReloaded

class RequestPutItem(commandSender: CommandSender, owner: String, itemStack: ItemStack, amount: Int) extends Request(commandSender) {
  private var info: CartItemInfo = _

  /** Здесь идет проверка условий для выполнения запроса (например, проверка наличия пермов) **/
  override def prehandle() {
    requirePermission("cartr.admin.put")

    info = ItemEncoder.createInfo(itemStack, owner, amount)
  }

  def handle() {
    val id = dao.addItem(info)
    sendMessage(ShoppingCartReloaded.instance.lang.format("cart-put.put", id))
  }


}
