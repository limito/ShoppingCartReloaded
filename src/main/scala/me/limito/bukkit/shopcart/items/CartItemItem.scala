package me.limito.bukkit.shopcart.items

import org.bukkit.entity.Player
import org.bukkit.inventory.{Inventory, ItemStack}
import me.limito.bukkit.shopcart.{ShoppingCartReloaded, Lang}
import org.bukkit.enchantments.Enchantment
import me.limito.bukkit.shopcart.optional.nbt.NBTTag
import scala.annotation.tailrec
import scala.collection.JavaConverters._
import java.util
import org.bukkit.Material

case class NameAndLore(name: String, lore: String)

class CartItemItem(val itemId: Int, val itemMeta: Short, val amount: Int, val enchantments: Array[LeveledEnchantment], val nbtTag: NBTTag, val nameAndLoreOption: Option[NameAndLore]) extends CartItem {
  def giveToPlayer(player: Player):Int = giveToPlayer(player, amount)

  def giveToPlayer(player: Player, amount: Int):Int = {
    val stack = stackToGive
    if (stack.isDefined)
      give(player.getInventory, stack.get, amount, 0)
    else 0
  }

  private def stackToGive: Option[ItemStack] = {
    val material = Material.getMaterial(itemId)
    if (material != null) {
      val bstack = wrapInCraftStack(new ItemStack(material, 1, itemMeta))
      val stack = if (nbtTag != null) ShoppingCartReloaded.instance.nbtHelper.placeTag(nbtTag, bstack) else bstack

      if (enchantments != null)
        enchantments foreach(e => stack.addUnsafeEnchantment(Enchantment.getById(e.id), e.level))
      if (nameAndLoreOption.isDefined) {
        val nameAndLore = nameAndLoreOption.get
        val meta = stack.getItemMeta

        if (nameAndLore.name != null)
          meta.setDisplayName(nameAndLore.name)
        if (nameAndLore.lore != null)
          meta.setLore(nameAndLore.lore.split('\n').toList.asJava)

        stack.setItemMeta(meta)
      }
      Some(stack)
    } else None
  }

  def getLocalizedName(lang: Lang): String = {
    val name = lang.getItemName(itemId, itemMeta)
    if (enchantments != null)
      name + " " + lang.formatEnchantments(enchantments)
    else
      name
  }

  def getYouGetMessage(amount: Int, lang: Lang): String = lang.format("cart-get.get", getLocalizedName(lang), amount)

  @tailrec
  private def give(inv: Inventory, baseStack: ItemStack, amount: Int, alreadyGiven: Int): Int = {
    require(amount >= 0)
    if (amount == 0)
      return alreadyGiven

    val maxStackSize = Math.max(baseStack.getMaxStackSize, 1)
    val amountToGive = Math.min(maxStackSize, amount)
    val stack = baseStack.clone()
    stack.setAmount(amountToGive)
    val notGiven = inv.addItem(stack)

    if (notGiven.isEmpty)
      give(inv, baseStack, amount - amountToGive, amountToGive + alreadyGiven)
    else
      alreadyGiven + amountToGive - notGiven.values().iterator().next().getAmount
  }

  override def getIcon: ItemStack = stackToGive match {
    case Some(stack) => new ItemStack(stack)
    case None => super.getIcon
  }

  private def wrapInCraftStack(stack: ItemStack): ItemStack = {
    val inv: Inventory = ShoppingCartReloaded.instance.getServer.createInventory(null, 9)
    inv.addItem(stack)
    inv.getItem(0)
  }
}