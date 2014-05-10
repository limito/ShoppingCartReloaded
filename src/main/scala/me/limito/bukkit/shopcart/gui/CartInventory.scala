package me.limito.bukkit.shopcart.gui

import org.bukkit.entity.Player
import me.limito.bukkit.shopcart.items.{CartItem, CartItemInfo}
import org.bukkit.Bukkit
import org.bukkit.inventory.{InventoryView, ItemStack}
import me.limito.bukkit.shopcart.ShoppingCartReloaded
import scala.collection.JavaConverters._
import org.bukkit.event.{EventHandler, HandlerList, Listener}
import org.bukkit.event.inventory.{InventoryCloseEvent, InventoryClickEvent}
import org.bukkit.event.inventory.InventoryType.SlotType
import me.limito.bukkit.shopcart.request.RequestItemGive

class CartInventory(player: Player, itemInfos: Seq[CartItemInfo]) extends Listener {
  abstract class StackSlot {
    def set(stack: ItemStack)
    def get: ItemStack
  }

  protected val MaxGive = 64
  protected val invSize = 36
  protected val buttonsSlots = 2

  protected val inventory = Bukkit.getServer.createInventory(null, invSize, ShoppingCartReloaded.instance.lang.get("cart-gui.title"))
  protected var inventoryView: InventoryView = _
  protected val inventoryPager = new InventoryPager(inventory)

  protected def isPlayerSlot(rawSlot: Int) = rawSlot >= invSize
  protected def isCartSlot(rawSlot: Int) = rawSlot < invSize - buttonsSlots
  protected def isButtonSlot(rawSlot: Int) = rawSlot >= invSize - buttonsSlots && rawSlot < invSize

  protected def invSlot(rawSlot: Int) = new StackSlot {
    override def get: ItemStack = inventory.getItem(rawSlot)
    override def set(stack: ItemStack): Unit = inventory.setItem(rawSlot, stack)
  }

  protected def populateInventory() {
    val items = itemInfos.map(_.toItem)
    val stacks = itemInfos zip items map(i => itemStackForItem(i._1, i._2))
    inventoryPager.populateInventory(stacks)
    inventoryPager.createPageLinks()
    inventoryPager.selectPage(0)
  }

  protected def itemStackForItem(itemInfo: CartItemInfo, item: CartItem): ItemStack = {
    val name = item.getLocalizedName(ShoppingCartReloaded.instance.lang)
    val lore = List(
      "#" + itemInfo.id,
      "x" + itemInfo.amount
    ).asJava
    val icon = item.getIcon
    val meta = icon.getItemMeta
    meta.setDisplayName(name)
    meta.setLore(lore)
    icon.setItemMeta(meta)
    icon.setAmount(itemInfo.amount)

    icon
  }

  protected def giveItem(infoId: Int, amount: Int) {
    ShoppingCartReloaded.instance.requestManager.handleRequest(new RequestItemGive(player, infoId, amount))
  }

  protected def itemInfoIdFromItemStack(stack: ItemStack): Option[Long] = {
    val meta = stack.getItemMeta
    val lore = meta.getLore
    if (lore != null && lore.size() >= 1) {
      val idString = lore.get(0)
      val id = idString.substring(1).toInt
      Some(id)
    } else None
  }

  def open() {
    Bukkit.getPluginManager.registerEvents(this, ShoppingCartReloaded.instance.plugin)

    populateInventory()
    inventoryView = player.openInventory(inventory)
  }

  def close() {
    player.setItemOnCursor(null)

    HandlerList.unregisterAll(this)
    player.closeInventory()
  }

  @EventHandler
  def onInvClicked(event: InventoryClickEvent) {
    if (!(event.getWhoClicked == player))
      return

    if (event.getSlotType == SlotType.OUTSIDE) {
      event.setCancelled(true)
      return
    }
    if (isCartSlot(event.getRawSlot) && event.getCursor.getAmount > 0 && !itemInfoIdFromItemStack(event.getCursor).isDefined) {
      // Load, not implemented
      event.setCancelled(true)
      return
    }
    if (isButtonSlot(event.getSlot)) {
      onButtonClick(event.getSlot)
      event.setCancelled(true)
    }

    if (event.isShiftClick) {
      if (isPlayerSlot(event.getRawSlot)) {
        // Load, not implemented yet
        event.setCancelled(true)
      } else {
        // Get
        val slot = invSlot(event.getRawSlot)
        if(giveItemAndUpdateStackInSlot(slot, MaxGive))
          event.setCancelled(true)
      }
      return
    }
    if (isPlayerSlot(event.getRawSlot)) {
      val maxAmount = if (event.isRightClick) 1 else MaxGive
      val slot = new StackSlot {
        override def get: ItemStack = event.getCursor
        override def set(stack: ItemStack): Unit = event.setCursor(stack) // Updated automatically
      }
      if (giveItemAndUpdateStackInSlot(slot, maxAmount))
        event.setCancelled(true)
    }
  }

  protected def onButtonClick(rawSlot: Int) {
    val index = rawSlot - (invSize - buttonsSlots)
    index match {
      case 0 => inventoryPager.previousPage()
      case 1 => inventoryPager.nextPage()
    }
  }

  protected def giveItemAndUpdateStackInSlot(stackSlot: StackSlot, maxAmount: Int): Boolean = {
    if (stackSlot.get == null)
      return false

    val stack = stackSlot.get
    val amount = Math.min(stack.getAmount, maxAmount)
    if (amount >= 1 && amount <= stack.getAmount) {
      val itemInfoId = itemInfoIdFromItemStack(stack)
      itemInfoId match {
        case Some(id) =>
          stack.setAmount(stack.getAmount - amount)
          stackSlot.set(stack)
          giveItem(id.toInt, amount)
          true
        case None => false
      }
    } else false
  }

  @EventHandler
  def onInvClosed(event: InventoryCloseEvent) {
    if (event.getPlayer == player)
      close()
  }
}
