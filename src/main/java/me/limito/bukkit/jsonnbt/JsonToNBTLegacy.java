package me.limito.bukkit.jsonnbt;

import com.google.gson.*;
import me.dpohvar.powernbt.nbt.*;

import java.util.Map;

public class JsonToNBTLegacy {
    public static NBTBase parse(String jsonAsString) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(jsonAsString);
            return parse(null, jsonElement);
        } catch (JsonSyntaxException ex) {
            throw new NBTException("Error parsing json", ex);
        }
    }

    public static NBTBase parse(String type, JsonElement element) {
        if (element instanceof JsonObject) {
            NBTTagCompound compound = new NBTTagCompound();

            JsonObject object = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String key = entry.getKey();
                JsonElement el = entry.getValue();

                String eType = null;
                int typeI = key.indexOf("#");
                if (typeI > 0)
                {
                    eType = key.substring(0, typeI);
                    key = key.substring(typeI + 1);
                }
                NBTBase value = parse(eType, el);
                compound.put(key, value);
            }
            return compound;
        } else if (element instanceof JsonPrimitive) {
            JsonPrimitive num = (JsonPrimitive) element;
            if (num.isNumber()) {
                if (type == null || type.equals("i")) {
                    return new NBTTagInt(num.getAsInt());
                } else if (type.equals("b")) {
                    return new NBTTagByte(num.getAsByte());
                } else if (type.equals("d")) {
                    return new NBTTagDouble(num.getAsDouble());
                } else if (type.equals("f")) {
                    return new NBTTagFloat(num.getAsFloat());
                } else if (type.equals("l")) {
                    return new NBTTagLong(num.getAsLong());
                } else if (type.equals("s")) {
                    return new NBTTagShort(num.getAsShort());
                } else
                    throw new RuntimeException("Unknown number type #" + type);
            } else {
                String s = num.getAsString();
                return new NBTTagString(s);
            }
        } else if (element instanceof JsonArray) {
            JsonArray array = (JsonArray) element;
            if (type == null || type.equals("list")) {
                NBTTagList list = new NBTTagList();
                for (JsonElement elt : array) {
                    list.add(parse(null, elt));
                }
                return list;
            } else if (type.equals("i")) {
                NBTTagIntArray list = new NBTTagIntArray();
                int i = 0;
                for (JsonElement elt : array) {
                    list.set(i, elt.getAsInt());
                    i++;
                }
                return list;
            } else if (type.equals("b")) {
                NBTTagByteArray list = new NBTTagByteArray();
                int i = 0;
                for (JsonElement elt : array) {
                    list.set(i, elt.getAsByte());
                    i++;
                }
                return list;
            } else {
                throw new NBTException("Unknown number type #" + type);
            }
        }
        throw new NBTException("Unknown json element " + element + " [" + element.getClass() + " Type: " + type);
    }
}
