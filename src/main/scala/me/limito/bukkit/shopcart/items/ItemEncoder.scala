package me.limito.bukkit.shopcart.items

import org.bukkit.inventory.ItemStack
import me.limito.bukkit.shopcart.ShoppingCartReloaded
import org.bukkit.enchantments.Enchantment
import scala.collection.JavaConverters._
import org.bukkit.inventory.meta.ItemMeta

object ItemEncoder {
  def createInfo(itemStack: ItemStack, owner: String, amount: Int):CartItemInfo = {
    val enchInfo = if(itemStack.getEnchantments.isEmpty) "" else "#" + createEnchantmentsInfo(itemStack.getEnchantments)
    val itemName = if(itemStack.getDurability == 0) itemStack.getTypeId.toString else itemStack.getTypeId.toString + ":" + itemStack.getDurability.toString
    val nameAndLoreInfo = createNameAndLoreInfo(itemStack.getItemMeta)

    val nbtHelper = ShoppingCartReloaded.instance.nbtHelper
    val tag = nbtHelper.getTag(itemStack)
    val encodedTag =nbtHelper.encodeJson(tag)

    new CartItemInfo(0, "item", itemName + nameAndLoreInfo + enchInfo, owner, amount, encodedTag)
  }

  private def createEnchantmentsInfo(enchs: java.util.Map[Enchantment, Integer]):String = (for ((id, level) <- enchs.asScala) yield id.getId + ":" + level).mkString("#")

  private def createNameAndLoreInfo(itemMeta: ItemMeta) = {
    if ((itemMeta.getDisplayName eq null) && (itemMeta.getLore eq null))
      ""
    else if (itemMeta.getLore eq null)
      "@" + encodeMetaOrLoreLine(itemMeta.getDisplayName)
    else
      "@" + encodeMetaOrLoreLine(itemMeta.getDisplayName) + "@" + encodeLore(itemMeta.getLore.asScala)
  }

  private def encodeMetaOrLoreLine(line: String) = line.replaceAll("@", "\\\\@")
  private def encodeLore(lore: Seq[String]) = lore.map(encodeMetaOrLoreLine).mkString("\n")
}
