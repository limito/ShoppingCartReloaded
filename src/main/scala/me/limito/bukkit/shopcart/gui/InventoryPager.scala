package me.limito.bukkit.shopcart.gui

import org.bukkit.inventory.{ItemStack, Inventory}
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable
import me.limito.bukkit.shopcart.ShoppingCartReloaded
import org.bukkit.Material

class InventoryPager(val inv: Inventory) {
  val maxPages = 100
  val freeSlots = 2
  val pages: mutable.Buffer[Array[ItemStack]] = new ArrayBuffer[Array[ItemStack]]
  var selectedPage = -1

  def numPages = pages.size

  def populateInventory(items: Seq[ItemStack]): Unit = populateInventory(items.toList)
  def populateInventory(items: List[ItemStack]) {
    // Create page
    val page = new Array[ItemStack](inv.getSize)
    pages += page
    inv.clear()

    var enoughSpace = true
    var list = items
    do {
      // Try to add new item
      val item = list.head
      val newList = list.tail
      val notFit = inv.addItem(item)
      if (!notFit.isEmpty) {
        // Item not fit
        val notFitItem = notFit.get(0)
        list = notFitItem :: newList
        enoughSpace = false
      } else list = newList
    } while (enoughSpace && !list.isEmpty)

    // Remove items from last freeSlots
    for (i <- (inv.getSize - 1) to (inv.getSize - freeSlots) by -1) {
      val item = inv.getItem(i)
      if (item != null && item.getAmount > 0) {
        list = item :: list
        inv.setItem(i, null)
      }
    }

    // Save page
    savePage(pages.size - 1)

    // if list is not empty - populate more
    if (!list.isEmpty && pages.size < maxPages)
      populateInventory(list)
  }

  def createPageLinks() {
    def itemStack(material: Material, amount: Int, name: String) = {
      val stack = new ItemStack(material)
      stack.setAmount(amount)
      val meta = stack.getItemMeta
      meta.setDisplayName(name)
      stack.setItemMeta(meta)
      stack
    }

    val lang = ShoppingCartReloaded.instance.lang
    for (i <- 0 until numPages) {
      val prev = if (i > 0) i else numPages
      val next = if (i < numPages - 1) i + 2 else 1
      val prevString = lang.format("cart-gui.prev", prev)
      val nextString = lang.format("cart-gui.next", next)

      val prevSlot = inv.getSize - freeSlots
      val nextSlot = inv.getSize - freeSlots + 1
      val prevStack = itemStack(Material.FLINT, prev, prevString)
      val nextStack = itemStack(Material.FLINT, next, nextString)

      selectPage(i)
      inv.setItem(prevSlot, prevStack)
      inv.setItem(nextSlot, nextStack)
    }
  }

  def selectPage(page: Int) {
    savePage()
    loadPage(page)
  }

  def nextPage() {
    val newPage = (selectedPage + 1) % numPages
    selectPage(newPage)
  }

  def previousPage() {
    val newPage = if (selectedPage > 0) selectedPage - 1 else numPages - 1
    selectPage(newPage)
  }

  private def savePage() {
    if (selectedPage >= 0)
      savePage(selectedPage)
  }

  private def savePage(page: Int) {
    pages(page) = inv.getContents.clone()
  }

  private def loadPage(page: Int) {
    inv.setContents(pages(page).clone())
    selectedPage = page
  }
}