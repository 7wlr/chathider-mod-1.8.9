package xyz.ivan.chathider;

import xyz.ivan.chathider.commands.ChatHiderCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = ChatHider.MODID, name = ChatHider.NAME, version = ChatHider.VERSION, clientSideOnly = true)
public class ChatHider {

    public static final String MODID = "chathider";
    public static final String NAME = "Chat Hider";
    public static final String VERSION = "1.4";

    public static Configuration config;
    public static boolean isEnabled = true;
    public static KeyBinding toggleKey;

    public static final Set<String> whitelist = new HashSet<String>();
    public static final Set<String> blacklist = new HashSet<String>();

    private static final Pattern NAME_PATTERN = Pattern.compile("([a-zA-Z0-9_]{3,16})(?::| »)");

    private static final List<String> ALLOWED_PREFIXES = Arrays.asList(
            "Party >",
            "Guild >",
            "From ",
            "To "
    );

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        ClientCommandHandler.instance.registerCommand(new ChatHiderCommand());
        toggleKey = new KeyBinding("ChatHider Toggle", Keyboard.KEY_H, "Chat Hider");
        ClientRegistry.registerKeyBinding(toggleKey);
        File configFile = event.getSuggestedConfigurationFile();
        config = new Configuration(configFile);
        syncConfig();
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (toggleKey.isPressed()) {
            toggleMod();
        }
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!isEnabled || event.type != 0) {
            return;
        }

        String unformattedMessage = event.message.getUnformattedText();
        String senderName = getPlayerNameFromMessage(unformattedMessage);
        String myName = Minecraft.getMinecraft().thePlayer.getName();
        String lowerCaseMessage = unformattedMessage.toLowerCase();

        if (senderName != null && senderName.equalsIgnoreCase(myName)) {
            return;
        }

        for (String prefix : ALLOWED_PREFIXES) {
            if (lowerCaseMessage.startsWith(prefix)) {
                return;
            }
        }

        if (senderName != null && whitelist.contains(senderName.toLowerCase())) {
            return;
        }
        for (String whitelistedName : whitelist) {
            if (lowerCaseMessage.contains(whitelistedName)) {
                return;
            }
        }

        if (lowerCaseMessage.contains(myName.toLowerCase())) {
            if (senderName == null || !blacklist.contains(senderName.toLowerCase())) {
                return;
            }
        }

        event.setCanceled(true);
    }

    private String getPlayerNameFromMessage(String unformattedMessage) {
        Matcher matcher = NAME_PATTERN.matcher(unformattedMessage);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static void syncConfig() {
        isEnabled = config.getBoolean("enabled", "general", true, "Master toggle for the mod. Set to false to disable all features.");
        String[] whitelistArray = config.getStringList("whitelist", "lists", new String[]{}, "Players whose messages are always shown.");
        String[] blacklistArray = config.getStringList("blacklist", "lists", new String[]{}, "Players whose pings will be hidden.");
        whitelist.clear();
        for (String name : whitelistArray) {
            whitelist.add(name.toLowerCase());
        }
        blacklist.clear();
        for (String name : blacklistArray) {
            blacklist.add(name.toLowerCase());
        }
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void toggleMod() {
        isEnabled = !isEnabled;
        config.get("general", "enabled", true).set(isEnabled);
        config.save();
        String status = isEnabled ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled";
        sendMessage("Mod is now " + status + EnumChatFormatting.GRAY + ".");
    }

    public static void sendMessage(String message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(
                new ChatComponentText(
                        EnumChatFormatting.RED.toString() +
                                EnumChatFormatting.BOLD.toString() +
                                "[ChatHider] " +
                                EnumChatFormatting.RESET.toString() +
                                EnumChatFormatting.GRAY.toString() +
                                message
                )
        );
    }
}