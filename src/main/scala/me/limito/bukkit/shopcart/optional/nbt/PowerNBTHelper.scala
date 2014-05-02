package me.limito.bukkit.shopcart.optional.nbt

import me.dpohvar.powernbt.nbt.{NBTContainerItem, NBTBase}
import org.bukkit.inventory.{Inventory, ItemStack}
import me.limito.bukkit.shopcart.ShoppingCartReloaded
import me.limito.bukkit.jsonnbt._
import java.util.logging.Level

class PowerNBTHelper extends NBTHelper {
  runTests()

  /* PowerNBT работает только на CraftItemStack, этот метод служит для конвертации ItemStack в CraftItemStack*/
  def wrapToCraftStack(stack: ItemStack): ItemStack = {
    val inv: Inventory = ShoppingCartReloaded.instance.getServer.createInventory(null, 9)
    inv.addItem(stack)
    inv.getItem(0)
  }

  def runTests() {
    val logger = ShoppingCartReloaded.instance.getLogger
    logger.info("Testing PowerNBT...")
    try {
      val test = new Test()
      test.run()
      logger.info("Tests passed")
    } catch {
      case e: Throwable => logger.log(Level.WARNING, "*** PowerNBT tests failed! ***", e)
    }
  }

  @throws[NBTParseException]
  override def parseJson(json: String): NBTTag = {
    try {
      val nbt: NBTBase = JsonToNBTLegacy.parse(json)
      return NBTTagImpl(nbt)
    } catch {
      case ex: NBTException => // Try another parser
    }

    try {
      val nbt: NBTBase = JsonToNBT.parse(json)
      NBTTagImpl(nbt)
    } catch {
      case ex: NBTException => throw new NBTParseException("Error parsing json", ex)
    }
  }
  override def placeTag(tag: NBTTag, stack: ItemStack) = {
    val tagi = tag.asInstanceOf[NBTTagImpl]

    val wrappedStack = wrapToCraftStack(stack)
    val container = new NBTContainerItem(wrappedStack)
    val nbt = if (tag != null) tagi.nbt else null
    container.setTag(nbt)

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
