package me.limito.bukkit.shopcart.request

import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack
import me.limito.bukkit.shopcart.items.CartItemInfo
import org.bukkit.enchantments.Enchantment
import collection.JavaConversions._
import me.limito.bukkit.shopcart.ShoppingCartReloaded

class RequestPutItem(commandSender: CommandSender, owner: String, itemStack: ItemStack, amount: Int) extends Request(commandSender) {
  private var info: CartItemInfo = _

  /** Здесь идет проверка условий для выполнения запроса (например, проверка наличия пермов) **/
  override def prehandle() {
    requirePermission("cart.admin.put")

    info = createInfo(itemStack)
  }

  def handle() {
    val id = dao.addItem(info)
    sendMessage(ShoppingCartReloaded.instance.lang.format("cart-put.put", id))
  }

  private def createInfo(itemStack: ItemStack):CartItemInfo = {
    val enchInfo = if(itemStack.getEnchantments.isEmpty) "" else "#" + createEnchantmentsInfo(itemStack.getEnchantments)
    val itemName = if(itemStack.getDurability == 0) itemStack.getTypeId.toString else itemStack.getTypeId.toString + ":" + itemStack.getDurability.toString

    val nbtHelper = ShoppingCartReloaded.instance.nbtHelper
    val tag = nbtHelper.getTag(itemStack)
    val encodedTag =nbtHelper.encodeJson(tag)

    new CartItemInfo(0, "item", itemName + enchInfo, owner, amount, encodedTag)
  }

  private def createEnchantmentsInfo(enchs: java.util.Map[Enchantment, Integer]):String = (for ((id, level) <- enchs) yield id.getId + ":" + level).mkString("#")
}
