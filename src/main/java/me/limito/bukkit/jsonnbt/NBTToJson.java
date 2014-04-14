package me.limito.bukkit.jsonnbt;

import me.dpohvar.powernbt.nbt.*;

import java.util.Iterator;
import java.util.Set;

public class NBTToJson {
    public static String encode(NBTBase base) {
        StringBuilder builder = new StringBuilder();
        encode(base, builder);
        return builder.toString();
    }

    private static void encodeNamed(NBTBase base, String name, StringBuilder b) {
        b.append(name);
        b.append(": ");
        encode(base, b);
    }

    private static void encode(NBTBase base, StringBuilder b) {
        if (base instanceof NBTTagNumeric)
            encodeNumeric((NBTTagNumeric) base, b);
        else {
            switch (base.getType()) {
                case BYTEARRAY:
                    encodeByteArray((NBTTagByteArray) base, b);
                    break;
                case INTARRAY:
                    encodeIntArray((NBTTagIntArray) base, b);
                    break;
                case LIST:
                    encodeList((NBTTagList) base, b);
                    break;
                case STRING:
                    encodeString((NBTTagString) base, b);
                    break;
                case COMPOUND:
                    encodeCompound((NBTTagCompound) base, b);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown nbt type: " + base.getType());
            }
        }
    }

    private static void encodeCompound(NBTTagCompound tag, StringBuilder b) {
        b.append('{');
        Set<String> keys = tag.getHandleMap().keySet();

        if (tag.size() > 0) {
            Iterator<String> iterator = keys.iterator();
            String firstKey = iterator.next();
            encodeNamed(tag.get(firstKey), firstKey, b);
            while (iterator.hasNext()) {
                b.append(", ");

                String key = iterator.next();
                encodeNamed(tag.get(key), key, b);
            }
        }
        b.append('}');
    }

    private static void encodeString(NBTTagString tag, StringBuilder b) {
        String escaped = escapeString(tag.get());
        b.append('"');
        b.append(escaped);
        b.append('"');
    }

    private static String escapeString(String s) {
        StringBuilder escaped = new StringBuilder(s.length());
        char[] chars = s.toCharArray();

        for (char c: chars) {
            switch (c) {
                case '"':
                    escaped.append("\\");
                    escaped.append(c);
                    break;
                default:
                    escaped.append(c);
            }
        }

        return escaped.toString();
    }

    private static void encodeList(NBTTagList list, StringBuilder b) {
        b.append('[');
        if (list.size() > 0) {
            encode(list.get(0), b);
            for (int i = 1; i < list.size(); i++) {
                b.append(", ");
                encode(list.get(i), b);
            }
        }
        b.append(']');
    }

    private static void encodeByteArray(NBTTagByteArray tag, StringBuilder b) {
        byte[] bytes = tag.get();

        b.append('[');
        if (bytes.length > 0) {
            b.append(bytes[0]);
            b.append('b');

            for (int i = 1; i < bytes.length; i++) {
                b.append(", ");
                b.append(bytes[i]);
                b.append('b');
            }
        }
        b.append(']');
    }

    private static void encodeIntArray(NBTTagIntArray tag, StringBuilder b) {
        int[] ints = tag.get();

        b.append('[');
        if (ints.length > 0) {
            b.append(ints[0]);

            for (int i = 1; i < ints.length; i++) {
                b.append(", ");
                b.append(ints[i]);
            }
        }
        b.append(']');
    }

    private static void encodeNumeric(NBTTagNumeric tag, StringBuilder b) {
        b.append(tag.get());

        char suffix = numericSuffix(tag);
        if (suffix != 0)
            b.append(suffix);
    }

    private static char numericSuffix(NBTTagNumeric tagNumeric) {
        switch (tagNumeric.getType()) {
            case BYTE:
                return 'b';
            case DOUBLE:
                return 'd';
            case FLOAT:
                return 'f';
            case INT:
                return 0;
            case LONG:
                return 'L';
            case SHORT:
                return 's';
            default:
                throw new IllegalArgumentException("Unknown numeric type " + tagNumeric.getType());
        }
    }
}
