package me.limito.bukkit.shopcart.optional.nbt

import org.bukkit.inventory.ItemStack

trait NBTHelper {
  @throws[NBTParseException]
  def parseJson(json: String): NBTTag

  def encodeJson(nbt: NBTTag): String

  def placeTag(tag: NBTTag, stack: ItemStack): ItemStack

  def getTag(stack: ItemStack): NBTTag
}
