package xyz.ivan.chathider.commands;

import xyz.ivan.chathider.ChatHider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ChatHiderCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "chathider";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/chathider <toggle|whitelist|blacklist> ...";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            ChatHider.sendMessage(getCommandUsage(sender));
            return;
        }

        String mainAction = args[0].toLowerCase();

        if (mainAction.equals("toggle")) {
            ChatHider.toggleMod();
            return;
        }

        if (args.length < 2) {
            ChatHider.sendMessage("Usage: /chathide <whitelist|blacklist> <add|remove|list> [player]");
            return;
        }

        String listType = args[0].toLowerCase();
        String action = args[1].toLowerCase();

        if (!listType.equals("whitelist") && !listType.equals("blacklist")) {
            ChatHider.sendMessage("Invalid list type. Use 'whitelist' or 'blacklist'.");
            return;
        }

        if (action.equals("add")) {
            if (args.length < 3) {
                ChatHider.sendMessage("Usage: /chathide " + listType + " add <player>");
                return;
            }
            String playerToAdd = args[2].toLowerCase();
            if (listType.equals("whitelist")) {
                ChatHider.whitelist.add(playerToAdd);
                ChatHider.sendMessage("Added '" + playerToAdd + "' to the whitelist.");
            } else {
                ChatHider.blacklist.add(playerToAdd);
                ChatHider.sendMessage("Added '" + playerToAdd + "' to the blacklist.");
            }
            updateConfig();

        } else if (action.equals("remove")) {
            if (args.length < 3) {
                ChatHider.sendMessage("Usage: /chathide " + listType + " remove <player>");
                return;
            }
            String playerToRemove = args[2].toLowerCase();
            if (listType.equals("whitelist")) {
                if (ChatHider.whitelist.remove(playerToRemove)) {
                    ChatHider.sendMessage("Removed '" + playerToRemove + "' from the whitelist.");
                } else {
                    ChatHider.sendMessage("'" + playerToRemove + "' was not on the whitelist.");
                }
            } else {
                if (ChatHider.blacklist.remove(playerToRemove)) {
                    ChatHider.sendMessage("Removed '" + playerToRemove + "' from the blacklist.");
                } else {
                    ChatHider.sendMessage("'" + playerToRemove + "' was not on the blacklist.");
                }
            }
            updateConfig();

        } else if (action.equals("list")) {
            if (listType.equals("whitelist")) {
                ChatHider.sendMessage("Whitelist: " + joinSet(ChatHider.whitelist, ", "));
            } else {
                ChatHider.sendMessage("Blacklist: " + joinSet(ChatHider.blacklist, ", "));
            }

        } else {
            ChatHider.sendMessage("Invalid list action. Use 'add', 'remove', or 'list'.");
        }
    }

    private String joinSet(Set<String> set, String delimiter) {
        if (set == null || set.isEmpty()) {
            return "is empty.";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }


    private void updateConfig() {
        ChatHider.config.get("lists", "whitelist", new String[]{}).set(ChatHider.whitelist.toArray(new String[0]));
        ChatHider.config.get("lists", "blacklist", new String[]{}).set(ChatHider.blacklist.toArray(new String[0]));

        if (ChatHider.config.hasChanged()) {
            ChatHider.config.save();
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "toggle", "whitelist", "blacklist");
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("whitelist") || args[0].equalsIgnoreCase("blacklist"))) {
            return getListOfStringsMatchingLastWord(args, "add", "remove", "list");
        }

        if (args.length == 3 && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
            return getListOfStringsMatchingLastWord(args, getOnlinePlayerNames());
        }

        return null;
    }

    private String[] getOnlinePlayerNames() {
        List<String> playerNames = new ArrayList<String>();
        for (NetworkPlayerInfo info : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
            playerNames.add(info.getGameProfile().getName());
        }
        return playerNames.toArray(new String[0]);
    }
}