package net.minezrc.framework.test;

import org.apache.commons.lang.Validate;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Command Framework - CommandFramework <br>
 * The main command framework class used for controlling the framework.
 *
 * @author minnymin3, some modifications by limito
 */
public class CommandFramework {

    private final Map<String, Entry<Method, Object>> commandMap = new HashMap<String, Entry<Method, Object>>();
    private CommandMap map;
    private final Plugin plugin;
    private String noPermMessage = "You don't have permission";

    /**
     * Initializes the command framework and sets up the command maps
     */
    public CommandFramework(Plugin plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
            SimplePluginManager manager = (SimplePluginManager) plugin.getServer().getPluginManager();
            try {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                map = (CommandMap) field.get(manager);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setNoPermMessage(String noPermMessage) {
        this.noPermMessage = noPermMessage;
    }

    /**
     * Handles commands. Used in the onCommand method in your JavaPlugin class
     *
     * @param sender The {@link org.bukkit.command.CommandSender} parsed from
     *            onCommand
     * @param label The label parsed from onCommand
     * @param cmd The {@link org.bukkit.command.Command} parsed from onCommand
     * @param args The arguments parsed from onCommand
     * @return Always returns true for simplicity's sake in onCommand
     */
    public boolean handleCommand(CommandSender sender, String label, org.bukkit.command.Command cmd, String[] args) {
        for (int i = args.length; i >= 0; i--) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());
            for (int x = 0; x < i; x++) {
                buffer.append(".").append(args[x].toLowerCase());
            }
            String cmdLabel = buffer.toString();
            if (commandMap.containsKey(cmdLabel)) {
                Entry<Method, Object> entry = commandMap.get(cmdLabel);
                Command command = entry.getKey().getAnnotation(Command.class);
                if (!sender.isOp() && !sender.hasPermission(command.permission())) {
                    sender.sendMessage(noPermMessage);
                    return true;
                }
                try {
                    entry.getKey().invoke(entry.getValue(),
                            new CommandArgs(sender, cmd, label, args, cmdLabel.split("\\.").length - 1));
                } catch (InvocationTargetException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof CommandMessagedException) {
                        sender.sendMessage(cause.getMessage());
                    } else {
                        ex.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        defaultCommand(new CommandArgs(sender, cmd, label, args, 0));
        return true;
    }

    /**
     * Registers all command and completer methods inside of the object. Similar
     * to Bukkit's registerEvents method.
     *
     * @param obj The object to register the commands of
     */
    public void registerCommands(Object obj) {
        for (Method m : obj.getClass().getMethods()) {
            if (m.getAnnotation(Command.class) != null) {
                Command command = m.getAnnotation(Command.class);
                if (m.getParameterTypes().length > 1 || m.getParameterTypes()[0] != CommandArgs.class) {
                    System.out.println("Unable to register command " + m.getName() + ". Unexpected method arguments");
                    continue;
                }
                registerCommand(command, command.name(), m, obj);
                for (String alias : command.aliases()) {
                    registerCommand(command, alias, m, obj);
                }
            } else if (m.getAnnotation(Completer.class) != null) {
                Completer comp = m.getAnnotation(Completer.class);
                if (m.getParameterTypes().length > 1 || m.getParameterTypes().length == 0
                        || m.getParameterTypes()[0] != CommandArgs.class) {
                    System.out.println("Unable to register tab completer " + m.getName()
                            + ". Unexpected method arguments");
                    continue;
                }
                if (m.getReturnType() != List.class) {
                    System.out.println("Unable to register tab completer " + m.getName() + ". Unexpected return type");
                    continue;
                }
                registerCompleter(comp.name(), m, obj);
                for (String alias : comp.aliases()) {
                    registerCompleter(alias, m, obj);
                }
            }
        }
    }

    private void registerCommand(Command command, String label, Method m, Object obj) {
        Entry<Method, Object> entry = new AbstractMap.SimpleEntry<Method, Object>(m, obj);
        commandMap.put(label.toLowerCase(), entry);
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
        if (map.getCommand(cmdLabel) == null) {
            org.bukkit.command.Command cmd = new BukkitCommand(cmdLabel, plugin);
            map.register(plugin.getName(), cmd);
        }
        if (!command.description().equalsIgnoreCase("") && cmdLabel.equals(label)) {
            map.getCommand(cmdLabel).setDescription(command.description());
        }
        if (!command.usage().equalsIgnoreCase("") && cmdLabel.equals(label)) {
            map.getCommand(cmdLabel).setUsage(command.usage());
        }
    }

    private void registerCompleter(String label, Method m, Object obj) {
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
        if (map.getCommand(cmdLabel) == null) {
            org.bukkit.command.Command command = new BukkitCommand(cmdLabel, plugin);
            map.register(plugin.getName(), command);
        }
        if (map.getCommand(cmdLabel) instanceof BukkitCommand) {
            BukkitCommand command = (BukkitCommand) map.getCommand(cmdLabel);
            if (command.completer == null) {
                command.completer = new BukkitCompleter();
            }
            command.completer.addCompleter(label, m, obj);
        } else if (map.getCommand(cmdLabel) instanceof PluginCommand) {
            try {
                Object command = map.getCommand(cmdLabel);
                Field field = command.getClass().getDeclaredField("completer");
                field.setAccessible(true);
                if (field.get(command) == null) {
                    BukkitCompleter completer = new BukkitCompleter();
                    completer.addCompleter(label, m, obj);
                    field.set(command, completer);
                } else if (field.get(command) instanceof BukkitCompleter) {
                    BukkitCompleter completer = (BukkitCompleter) field.get(command);
                    completer.addCompleter(label, m, obj);
                } else {
                    System.out.println("Unable to register tab completer " + m.getName()
                            + ". A tab completer is already registered for that command!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void defaultCommand(CommandArgs args) {
        args.getSender().sendMessage(args.getLabel() + " is not handled! Oh noes!");
    }

    /**
     * Command Framework - Command <br>
     * The command annotation used to designate methods as commands. All methods
     * should have a single CommandArgs argument
     *
     * @author minnymin3
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Command {

        /**
         * The name of the command. If it is a sub command then its values would
         * be separated by periods. ie. a command that would be a subcommand of
         * test would be 'test.subcommandname'
         */
        public String name();

        /**
         * Gets the required permission of the command
         */
        public String permission() default "";

        /**
         * A list of alternate names that the command is executed under. See
         * name() for details on how names work
         */
        public String[] aliases() default {};

        /**
         * The description that will appear in /help of the command
         */
        public String description() default "";

        /**
         * The usage that will appear in /help (commandname)
         */
        public String usage() default "";
    }

    /**
     * Command Framework - Completer <br>
     * The completer annotation used to designate methods as command completers.
     * All methods should have a single CommandArgs argument and return a String
     * List object
     *
     * @author minnymin3
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Completer {

        /**
         * The command that this completer completes. If it is a sub command
         * then its values would be separated by periods. ie. a command that
         * would be a subcommand of test would be 'test.subcommandname'
         */
        String name();

        /**
         * A list of alternate names that the completer is executed under. See
         * name() for details on how names work
         */
        String[] aliases() default {};

    }

    /**
     * Command Framework - BukkitCommand <br>
     * An implementation of Bukkit's Command class allowing for registering of
     * commands without plugin.yml
     *
     * @author minnymin3
     */
    class BukkitCommand extends org.bukkit.command.Command {

        private final Plugin owningPlugin;
        protected BukkitCompleter completer;
        private final CommandExecutor executor;

        /**
         * A slimmed down PluginCommand
         */
        protected BukkitCommand(String label, Plugin owner) {
            super(label);
            this.executor = owner;
            this.owningPlugin = owner;
            this.usageMessage = "";
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            boolean success;

            if (!owningPlugin.isEnabled()) {
                return false;
            }

            if (!testPermission(sender)) {
                return true;
            }

            try {
                success = executor.onCommand(sender, this, commandLabel, args);
            } catch (Throwable ex) {
                throw new CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin "
                        + owningPlugin.getDescription().getFullName(), ex);
            }

            if (!success && usageMessage.length() > 0) {
                for (String line : usageMessage.replace("<command>", commandLabel).split("\n")) {
                    sender.sendMessage(line);
                }
            }

            return success;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws CommandException,
                IllegalArgumentException {
            Validate.notNull(sender, "Sender cannot be null");
            Validate.notNull(args, "Arguments cannot be null");
            Validate.notNull(alias, "Alias cannot be null");

            List<String> completions = null;
            try {
                if (completer != null) {
                    completions = completer.onTabComplete(sender, this, alias, args);
                }
                if (completions == null && executor instanceof TabCompleter) {
                    completions = ((TabCompleter) executor).onTabComplete(sender, this, alias, args);
                }
            } catch (Throwable ex) {
                StringBuilder message = new StringBuilder();
                message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');
                for (String arg : args) {
                    message.append(arg).append(' ');
                }
                message.deleteCharAt(message.length() - 1).append("' in plugin ")
                        .append(owningPlugin.getDescription().getFullName());
                throw new CommandException(message.toString(), ex);
            }

            if (completions == null) {
                return super.tabComplete(sender, alias, args);
            }
            return completions;
        }

    }

    public static class CommandMessagedException extends RuntimeException {
        public CommandMessagedException(String message) {
            super(message);
        }
    }

    /**
     * Command Framework - BukkitCompleter <br>
     * An implementation of the TabCompleter class allowing for multiple tab
     * completers per command
     *
     * @author minnymin3
     */
    class BukkitCompleter implements TabCompleter {

        private final Map<String, Entry<Method, Object>> completers = new HashMap<String, Entry<Method, Object>>();

        public void addCompleter(String label, Method m, Object obj) {
            completers.put(label, new AbstractMap.SimpleEntry<Method, Object>(m, obj));
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label,
                                          String[] args) {
            for (int i = args.length; i >= 0; i--) {
                StringBuilder buffer = new StringBuilder();
                buffer.append(label.toLowerCase());
                for (int x = 0; x < i; x++) {
                    if (!args[x].equals("") && !args[x].equals(" ")) {
                        buffer.append(".").append(args[x].toLowerCase());
                    }
                }
                String cmdLabel = buffer.toString();
                if (completers.containsKey(cmdLabel)) {
                    Entry<Method, Object> entry = completers.get(cmdLabel);
                    try {
                        return (List<String>) entry.getKey().invoke(entry.getValue(),
                                new CommandArgs(sender, command, label, args, cmdLabel.split("\\.").length - 1));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }

    /**
     * Command Framework - CommandArgs <br>
     * This class is passed to the command methods and contains various
     * utilities as well as the command info.
     *
     * @author minnymin3
     */
    public static class CommandArgs {

        private final CommandSender sender;
        private final org.bukkit.command.Command command;
        private final String label;
        private final String[] args;

        protected CommandArgs(CommandSender sender, org.bukkit.command.Command command, String label, String[] args,
                              int subCommand) {
            String[] modArgs = new String[args.length - subCommand];
            System.arraycopy(args, subCommand, modArgs, 0, args.length - subCommand);

            StringBuilder buffer = new StringBuilder();
            buffer.append(label);
            for (int x = 0; x < subCommand; x++) {
                buffer.append(".").append(args[x]);
            }
            String cmdLabel = buffer.toString();
            this.sender = sender;
            this.command = command;
            this.label = cmdLabel;
            this.args = modArgs;
        }

        /**
         * Gets the command sender
         *
         * @return sender
         */
        public CommandSender getSender() {
            return sender;
        }

        /**
         * Gets the original command object
         */
        public org.bukkit.command.Command getCommand() {
            return command;
        }

        /**
         * Gets the label including sub command labels of this command
         *
         * @return Something like 'test.subcommand'
         */
        public String getLabel() {
            return label;
        }

        /**
         * Gets all the arguments after the command's label. ie. if the command
         * label was test.subcommand and the arguments were subcommand foo foo,
         * it would only return 'foo foo' because 'subcommand' is part of the
         * command
         */
        public String[] getArgs() {
            return args;
        }

        public boolean isPlayer() {
            return sender instanceof Player;
        }

        public Player getPlayer() {
            if (sender instanceof Player) {
                return (Player) sender;
            } else {
                return null;
            }
        }
    }
}