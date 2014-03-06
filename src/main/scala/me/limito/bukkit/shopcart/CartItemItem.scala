package me.limito.bukkit.shopcart

import org.bukkit.entity.Player
import org.bukkit.inventory.{Inventory, ItemStack}

class CartItemItem(info: CartItemInfo, val itemId: Int, val itemMeta: Short, val amount: Int, val nbtData: String) extends CartItem(info) {
  def giveToPlayer(player: Player):Int = {
    val stack = new ItemStack(itemId, 1, itemMeta)
    give(player.getInventory, stack, amount)
  }

  def giveToPlayer(player: Player, amount: Int):Int = {
    val stack = new ItemStack(itemId, 1, itemMeta)
    give(player.getInventory, stack, amount)
  }

  def getLocalizedName(lang: Lang): String = itemId + ":" + itemMeta

  def getYouGetMessage(amount: Int, lang: Lang): String = lang.format("cart-get.get", getLocalizedName(lang), amount)

  private def give(inv: Inventory, baseStack: ItemStack, amount: Int): Int = {
    require(amount >= 0)
    if (amount == 0)
      return 0

    val maxStackSize = Math.max(baseStack.getMaxStackSize, 1)
    val amountToGive = Math.min(maxStackSize, amount)
    val stack = baseStack.clone()
    stack.setAmount(amountToGive)
    val notGiven = inv.addItem(stack)

    if (notGiven.isEmpty)
      amountToGive + give(inv, baseStack, amount - amountToGive)
    else
      amountToGive - notGiven.values().iterator().next().getAmount
  }
}
