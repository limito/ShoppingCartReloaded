package me.limito.bukkit.shopcart.request

import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack
import me.limito.bukkit.shopcart.items.CartItemInfo
import org.bukkit.enchantments.Enchantment
import collection.JavaConversions._
import me.limito.bukkit.shopcart.ShoppingCartReloaded
import org.bukkit.entity.Player

class RequestLoadItem(commandSender: CommandSender, owner: String) extends Request(commandSender) {
  private var info: CartItemInfo = _
  private var itemStack: ItemStack = _

  /** Здесь идет проверка условий для выполнения запроса (например, проверка наличия пермов) **/
  override def prehandle() {
    requirePermission("cart.useradv.load")

    commandSender match {
      case player: Player =>
        val stack = player.getItemInHand
        if (stack != null && stack.getAmount > 0) {
          itemStack = stack
          val heldItemSlot = player.getInventory.getHeldItemSlot
          player.getInventory.setItem(heldItemSlot, null)

          info = createInfo(stack)
        } else {
          sendMessage(ShoppingCartReloaded.instance.lang.format("cart-load.no-item"))
        }
      case _ => sendMessage(ShoppingCartReloaded.instance.lang.get("cart.not-a-player"))
    }
  }

  def handle() {
    if (itemStack != null) {
      val id = dao.addItem(info)
      sendMessage(ShoppingCartReloaded.instance.lang.format("cart-load.load", id))
    }
  }


  override def onException(t: Throwable) {
    if (itemStack != null) {
      commandSender.asInstanceOf[Player].getInventory.addItem(itemStack)
    }
  }

  private def createInfo(itemStack: ItemStack):CartItemInfo = {
    val enchInfo = if(itemStack.getEnchantments.isEmpty) "" else "#" + createEnchantmentsInfo(itemStack.getEnchantments)
    val itemName = if(itemStack.getDurability == 0) itemStack.getTypeId.toString else itemStack.getTypeId.toString + ":" + itemStack.getDurability.toString

    val nbtHelper = ShoppingCartReloaded.instance.nbtHelper
    val tag = nbtHelper.getTag(itemStack)
    val encodedTag =nbtHelper.encodeJson(tag)

    new CartItemInfo(0, "item", itemName + enchInfo, owner, itemStack.getAmount, encodedTag)
  }

  private def createEnchantmentsInfo(enchs: java.util.Map[Enchantment, Integer]):String = (for ((id, level) <- enchs) yield id.getId + ":" + level).mkString("#")
}
