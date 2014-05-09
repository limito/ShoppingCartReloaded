package me.limito.bukkit.shopcart.gui

import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.{DragType, InventoryDragEvent}
import org.bukkit.inventory.ItemStack
import org.bukkit.entity.Player
import me.limito.bukkit.shopcart.items.CartItemInfo

/**
 * For Minecraft 1.5.2+
 */
class CartInventoryNew(player: Player, itemInfos: Seq[CartItemInfo]) extends CartInventory(player, itemInfos) {
  @EventHandler
  def onInvDragged(event: InventoryDragEvent) {
    if (!(event.getWhoClicked == player))
      return
    if (event.getRawSlots.size() > 1) {
      event.setCancelled(true)
      return
    }

    val rawSlot = event.getRawSlots.iterator().next().toInt
    if (isCartSlot(rawSlot) && event.getOldCursor.getAmount > 0 && !itemInfoIdFromItemStack(event.getOldCursor).isDefined) {
      // Load, not implemented
      event.setCancelled(true)
      return
    }
    if (isButtonSlot(rawSlot)) {
      onButtonClick(rawSlot)
      event.setCancelled(true)
    }

    if (isPlayerSlot(rawSlot)) {
      val amount = if (event.getType == DragType.SINGLE) 1 else MaxGive
      val slot = new StackSlot {
        override def get: ItemStack = event.getOldCursor
        override def set(stack: ItemStack): Unit = {}// Update not needed
      }
      if (giveItemAndUpdateStackInSlot(slot, amount))
        event.setCancelled(true)
    }
  }
}
