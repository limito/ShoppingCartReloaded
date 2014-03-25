package me.limito.bukkit.shopcart.optional.nbt

import org.bukkit.inventory.ItemStack

class NBTHelperStub extends NBTHelper {
  override def parseJson(json: String) = throw new NBTParseException("NBT is not supported")
  override def encodeJson(nbt: NBTTag) = null
  override def placeTag(tag: NBTTag, stack: ItemStack) = throw new NotImplementedError("NBT is not supported")
  def getTag(stack: ItemStack): NBTTag = null
}
