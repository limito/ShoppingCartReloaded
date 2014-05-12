package me.limito.bukkit.jsonnbt;

public class NBTException extends RuntimeException  {
    public NBTException() {
    }

    public NBTException(String message) {
        super(message);
    }

    public NBTException(String message, Throwable cause) {
        super(message, cause);
    }

    public NBTException(Throwable cause) {
        super(cause);
    }
}
