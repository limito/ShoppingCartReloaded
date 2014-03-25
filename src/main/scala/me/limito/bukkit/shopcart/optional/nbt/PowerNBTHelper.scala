package me.limito.bukkit.shopcart.optional.nbt

import me.dpohvar.powernbt.nbt.{NBTContainerItem, NBTBase}
import org.bukkit.inventory.{Inventory, ItemStack}
import me.limito.bukkit.shopcart.ShoppingCartReloaded
import me.limito.bukkit.jsonnbt.JsonToNBT
import me.limito.bukkit.jsonnbt.NBTToJson

class PowerNBTHelper extends NBTHelper {
  /* PowerNBT работает только на CraftItemStack, этот метод служит для конвертации ItemStack в CraftItemStack*/
  def wrapToCraftStack(stack: ItemStack): ItemStack = {
    val inv: Inventory = ShoppingCartReloaded.instance.getServer.createInventory(null, 9)
    inv.addItem(stack)
    inv.getItem(0)
  }

  @throws[NBTParseException]
  override def parseJson(json: String): NBTTag = {
    val nbt: NBTBase = JsonToNBT.parse(json)
    NBTTagImpl(nbt)
  }

  override def placeTag(tag: NBTTag, stack: ItemStack) = {
    val tagi = tag.asInstanceOf[NBTTagImpl]

    val wrappedStack = wrapToCraftStack(stack)
    val container = new NBTContainerItem(wrappedStack)
    container.setTag(if (tag != null) tagi.nbt else null)

    wrappedStack
  }

  override def encodeJson(tag: NBTTag): String = {
    if (tag != null) {
      val tagi = tag.asInstanceOf[NBTTagImpl]
      NBTToJson.encode(tagi.nbt)
    } else null
  }

  def getTag(stack: ItemStack): NBTTag = {
    val wrappedStack = wrapToCraftStack(stack)
    val container = new NBTContainerItem(wrappedStack)
    val tag = container.getTag
    if (tag != null) NBTTagImpl(tag) else null
  }

  case class NBTTagImpl(nbt: NBTBase) extends NBTTag
}
