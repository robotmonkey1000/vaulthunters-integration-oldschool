package com.robotmonkey1000.VaultIntegration;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.helix.domain.UserList;
import com.robotmonkey1000.VaultIntegration.Twitch.TwitchEventHandler;
import com.robotmonkey1000.VaultIntegration.Utility.ArenaManager;
import com.robotmonkey1000.VaultIntegration.init.CustomCommands;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.util.MathUtilities;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.configuration.AbstractConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

@Mod("vaultintegration")
public class VaultIntegration
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    private static final HashMap<String, Integer> supporters = new HashMap<>();
    private static final HashMap<String, String> replacedSupporters = new HashMap<>();
    private TwitchClient twitchClient;
    private TwitchEventHandler twitchEventHandler;

    public VaultIntegration() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, this::onCommandRegister);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        OAuth2Credential credential = new OAuth2Credential("twitch", "AUTH CODE HERE");
        twitchClient = TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withClientId("CLIENT ID")
                .withClientSecret("CLIENT SECRET")
                .withEnableChat(true)
                .withChatAccount(credential)
                .withDefaultAuthToken(credential)
                .withEnablePubSub(true)
                .build();
        twitchEventHandler = new TwitchEventHandler(this);
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(twitchEventHandler);
        twitchClient.getPubSub().listenForChannelPointsRedemptionEvents(credential, "CHANNEL ID");
        twitchClient.getPubSub().listenForFollowingEvents(credential, "CHANNEL ID");
        twitchClient.getPubSub().listenForCheerEvents(credential, "CHANNEL ID");
        twitchClient.getPubSub().listenForSubscriptionEvents(credential, "CHANNEL ID");
        twitchClient.getPubSub().listenForChannelSubGiftsEvents(credential, "CHANNEL ID");
        twitchClient.getPubSub().listenForRaidEvents(credential, "CHANNEL ID");
    }



    private void onCommandRegister(RegisterCommandsEvent event) {
        CustomCommands.registerCommands(event.getDispatcher(), event.getEnvironment(), this);
    }
    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("vaultintegration", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
        twitchEventHandler.setServer(event.getServer());
        ArenaManager.setServer(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        twitchClient.close();
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }

    public static int getTotalSupport() {
        int total = 0;
        for(int cur: supporters.values()) {
            total += cur;
        }

        return total;
    }

    public static void addSupporter(String name, int amount) {
        if(name.equalsIgnoreCase("robotmonkey1000")) return;
        if(replacedSupporters.containsKey(name)) {
            name = replacedSupporters.get(name);
        }
        if(amount == 0) {
            if(!supporters.containsKey(name)) {
                supporters.put(name, 1);
            }
        } else {
            if(supporters.containsKey(name)) {
                supporters.put(name, supporters.get(name) + amount);
            } else {
                supporters.put(name, amount);
            }
        }

    }

    public void replaceSupporter(String curName, String newName) {
        if(!replacedSupporters.containsKey(curName)) {
            replacedSupporters.put(curName, newName);
            if(supporters.containsKey(curName)) {
                int support = supporters.get(curName);
                supporters.remove(curName);
                supporters.put(newName, support);
            }
        }
    }

    public static HashMap<String, Integer> getSupporters() {
        return supporters;
    }

    public static String getRandomSupporter(boolean addLevel) {
        if(supporters.size() > 0) {
            Random rand = new Random();
            int randInt = rand.nextInt(supporters.size());
            String userName = (String) supporters.keySet().toArray()[randInt];

            return addLevel ? "[" + supporters.get(userName) + "] " + userName: userName;
        }

        return "Fighter";
    }

    public static String getSupporter(String name) {
        if(replacedSupporters.containsKey(name)) {
            name = replacedSupporters.get(name);
        }

        return name;
    }


}
