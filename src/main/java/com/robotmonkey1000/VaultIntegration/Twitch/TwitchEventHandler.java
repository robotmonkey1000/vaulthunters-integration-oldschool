package com.robotmonkey1000.VaultIntegration.Twitch;

import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.chat.ITwitchChat;
import com.github.twitch4j.chat.events.channel.*;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.pubsub.domain.PredictionEvent;
import com.github.twitch4j.pubsub.domain.PredictionResult;
import com.github.twitch4j.pubsub.events.FollowingEvent;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import com.mojang.brigadier.Message;
import com.robotmonkey1000.VaultIntegration.Utility.ArenaManager;
import com.robotmonkey1000.VaultIntegration.VaultIntegration;
import iskallia.vault.entity.EntityScaler;
import iskallia.vault.entity.EternalEntity;
import iskallia.vault.entity.PlayerBossEntity;
import iskallia.vault.entity.VaultFighterEntity;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModEntities;
import iskallia.vault.init.ModItems;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.item.ItemGiftBomb;
import iskallia.vault.item.ItemTraderCore;
import iskallia.vault.network.message.ArenaLevelMessage;
import iskallia.vault.util.MathUtilities;
import iskallia.vault.util.TextUtil;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StandingSignBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.server.command.ForgeCommand;
import org.apache.logging.log4j.LogManager;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class TwitchEventHandler {

    private final VaultIntegration mod;
    private MinecraftServer server;
    private ArenaManager arenaManager;

    public TwitchEventHandler(VaultIntegration mod) {
        this.mod = mod;
        arenaManager = new ArenaManager();
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }


    @EventSubscriber
    public void raidedEvent(ChannelRaidEvent event) {
        System.out.println("Test message");
    }

    @EventSubscriber
    public void test(PredictionEvent event) {
        System.out.println(event.getWinningOutcomeId());
    }
    public void raidTest(RaidEvent raid) {
        System.out.println("Raided by: " + raid.getRaider());

        if(server != null) {
            if (raid.getViewers() > 3) {
                String message = "Thank you for the raid "  + raid.getRaider().getName() + "! Let's check out your trader!";
                raid.getTwitchChat().sendMessage(raid.getChannel().getName(), message);
                ServerPlayerEntity robot = server.getPlayerList().getPlayerByUsername("robotmonkey1000");
                if(robot == null) {
                } else {
                    giveTrader(robot, raid.getRaider().getName(), 5, ItemTraderCore.CoreType.COMMON, true);
                }
            }
        }
    }

    @EventSubscriber
    public void redeemRewards(RewardRedeemedEvent event) {

        ServerPlayerEntity robot = server.getPlayerList().getPlayerByUsername("robotmonkey1000");
        ServerWorld world = robot.getServerWorld();
        addSupport("robotmonkey1000", 1);
        if(event.getRedemption().getReward().getTitle().equalsIgnoreCase("Relic Booster Pack")) {
            ItemStack relicPack = new ItemStack(ModItems.RELIC_BOOSTER_PACK);
            if(robot == null) {
                //Enque
            } else {
                ITextComponent name = TextUtil.applyRainbowTo((event.getRedemption().getUser().getDisplayName()));
//                name.getStyle().setColor(Color.fromHex("#9ce62a"));
                relicPack.setDisplayName(name);
                boolean added = robot.addItemStackToInventory(relicPack);
                if(!added) {
                    robot.dropItem(relicPack, false, false);
                }
            }
        } else if(event.getRedemption().getReward().getTitle().equalsIgnoreCase("Summon Fighter")) {

            VaultFighterEntity vaultFighterEntity = ModEntities.VAULT_FIGHTER.create(world);
            if(vaultFighterEntity != null) {
                vaultFighterEntity.setPositionAndRotation(robot.getPosition().getX() + 0.5F, robot.getPosition().getY() + 0.2F, robot.getPosition().getZ() + 0.5F, 0.0F, 0.0F);
                world.summonEntity(vaultFighterEntity);
                vaultFighterEntity.func_213386_a(world, new DifficultyInstance(Difficulty.PEACEFUL, 13000L, 0L, 0.0F), SpawnReason.STRUCTURE, (ILivingEntityData)null, (CompoundNBT)null);
                vaultFighterEntity.setCustomName(ITextComponent.getTextComponentOrEmpty("[" + TextFormatting.RED + "EVIL" + TextFormatting.WHITE + "] " + (VaultIntegration.getSupporter(event.getRedemption().getUser().getDisplayName()))));
                //                vaultFighterEntity.setCustomName(ITextComponent.getTextComponentOrEmpty(VaultIntegration.getSupporter(event.getRedemption().getUser().getDisplayName())));
                vaultFighterEntity.func_70071_h_();
            }
        } else if(event.getRedemption().getReward().getTitle().equalsIgnoreCase("Summon Eternal")) {
            EternalEntity vaultFighterEntity = ModEntities.ETERNAL.create(world);
            vaultFighterEntity.owner = robot.getUniqueID();
            if(vaultFighterEntity != null) {
                vaultFighterEntity.setPositionAndRotation(robot.getPosition().getX() + 0.5F, robot.getPosition().getY() + 0.2F, robot.getPosition().getZ() + 0.5F, 0.0F, 0.0F);
                world.summonEntity(vaultFighterEntity);
                vaultFighterEntity.func_213386_a(world, new DifficultyInstance(Difficulty.PEACEFUL, 13000L, 0L, 0.0F), SpawnReason.STRUCTURE, (ILivingEntityData)null, (CompoundNBT)null);
                vaultFighterEntity.setCustomName(ITextComponent.getTextComponentOrEmpty("[" + TextFormatting.GREEN + "GOOD" + TextFormatting.WHITE + "] " + (VaultIntegration.getSupporter(event.getRedemption().getUser().getDisplayName()))));
                vaultFighterEntity.func_70071_h_();
            }
        } else if(event.getRedemption().getReward().getTitle().equalsIgnoreCase("Trader")) {
            giveTrader(robot, event.getRedemption().getUser().getDisplayName(), 5, ItemTraderCore.CoreType.COMMON, false);
        } else if(event.getRedemption().getReward().getTitle().equalsIgnoreCase("Sign")) {
            BlockPos pos = robot.getPosition();
            BlockState sign = Blocks.OAK_SIGN.getDefaultState();
            robot.world.destroyBlock(pos, true);
            robot.world.setBlockState(pos, sign);
            SignTileEntity te = (SignTileEntity) sign.createTileEntity(robot.world.getBlockReader(robot.chunkCoordX, robot.chunkCoordZ));

            if(te != null) {
                LogManager.getLogger().info("Testing Information");
                ((SignTileEntity)te).setText(0, ITextComponent.getTextComponentOrEmpty(event.getRedemption().getUser().getDisplayName()));
//                ((SignTileEntity)te).setText(1, ITextComponent.getTextComponentOrEmpty(event.getRedemption().getUserInput()));
                if(event.getRedemption().getUserInput().length() > 15) {
                    ((SignTileEntity)te).setText(1, ITextComponent.getTextComponentOrEmpty(event.getRedemption().getUserInput().substring(0, 15)));
                    ((SignTileEntity)te).setText(2, ITextComponent.getTextComponentOrEmpty(event.getRedemption().getUserInput().substring(15, Math.min(event.getRedemption().getUserInput().length(), 30))));
                    if(event.getRedemption().getUserInput().length() > 30) {
                        ((SignTileEntity)te).setText(3, ITextComponent.getTextComponentOrEmpty(event.getRedemption().getUserInput().substring(30, Math.min(event.getRedemption().getUserInput().length(), 45))));
                    }
                } else {
                    ((SignTileEntity)te).setText(1, ITextComponent.getTextComponentOrEmpty(event.getRedemption().getUserInput()));
                }

                robot.getServerWorld().setTileEntity(pos, te);
            }

        }
    }


    @EventSubscriber
    public void onFollow(FollowingEvent event) {
        addSupport(event.getData().getDisplayName(), 1);
        if(server != null) {
//            server.sendMessage(ITextComponent.getTextComponentOrEmpty("Hello"), UUID.randomUUID());
            String message = "Thank you for the follow and relic booster pack " + event.getData().getDisplayName() + "!";
            server.sendMessage(ITextComponent.getTextComponentOrEmpty(message), UUID.fromString("robotmonkey1000"));
            ItemStack relicPack = new ItemStack(ModItems.RELIC_BOOSTER_PACK);
            ServerPlayerEntity robot = server.getPlayerList().getPlayerByUsername("robotmonkey1000");
            if(robot == null) {

            } else {
                ITextComponent name = TextUtil.applyRainbowTo((event.getData().getDisplayName()));
                relicPack.setDisplayName(name);
                boolean added = robot.addItemStackToInventory(relicPack);
                if(!added) {
                    robot.dropItem(relicPack, false, false);
                }
            }
        }
//        (String.format("[Twitch] %s just followed %s!", event.getUser().getName(), event.getChannel().getName()));
    }
    private void giveBits(ServerPlayerEntity player, int amount) {

        int big = amount / 10000;
        amount %= 10000;
        int med = amount / 5000;
        amount %= 5000;
        int small = amount / 1000;
        amount %= 1000;
        int tiny = amount / 100;

        ItemStack kkBits = new ItemStack(ModItems.BIT_10000);
        kkBits.setCount(big);

        ItemStack fkBits = new ItemStack(ModItems.BIT_5000);
        fkBits.setCount(med);

        ItemStack kBits = new ItemStack(ModItems.BIT_1000);
        kBits.setCount(small);

        ItemStack bits = new ItemStack(ModItems.BIT_100);
        bits.setCount(tiny);

        boolean added = player.addItemStackToInventory(kkBits);
        if(!added) {
            player.dropItem(kkBits, false, false);
        }

        added = player.addItemStackToInventory(fkBits);
        if(!added) {
            player.dropItem(fkBits, false, false);
        }

        added = player.addItemStackToInventory(kBits);
        if(!added) {
            player.dropItem(kBits, false, false);
        }

        added = player.addItemStackToInventory(bits);
        if(!added) {
            player.dropItem(bits, false, false);
        }

    }
    @EventSubscriber
    public void onCheer(CheerEvent event) {

        addSupport(event.getUser().getName(), event.getBits() / 100);
        if(server != null) {
            ServerPlayerEntity robot = server.getPlayerList().getPlayerByUsername("robotmonkey1000");
            if(robot == null) {

            } else {
                giveBits(robot, event.getBits());
                if(event.getBits() >= 1500) {
                    giveTrader(robot, event.getUser().getName(), event.getBits() / 100, ItemTraderCore.CoreType.OMEGA, true);
                } else if(event.getBits() >= 500) {
                    giveTrader(robot, event.getUser().getName(), event.getBits() / 100, ItemTraderCore.CoreType.COMMON, false);
                }
            }
        }

//            broadcast(String.format("[Twitch] %s just cheered %d bits for %s!", event.getUser().getName(), event.getBits(), event.getChannel().getName()));
    }

    @EventSubscriber
    public void onSub(SubscriptionEvent event) {

        if(event.getSubPlan() == SubscriptionPlan.TIER2) {
            addSupport(event.getUser().getName(), 10);
        } else if(event.getSubPlan() == SubscriptionPlan.TIER3) {
            addSupport(event.getUser().getName(), 25);
        } else {
            addSupport(event.getUser().getName(), 5);
        }

        if(server != null) {
            if (!event.getGifted()) {
                ServerPlayerEntity robot = server.getPlayerList().getPlayerByUsername("robotmonkey1000");
                if(robot == null) {
                } else {
                    if(event.getSubPlan() == SubscriptionPlan.TIER2) {
                        giveTrader(robot, event.getUser().getName(), 10, ItemTraderCore.CoreType.OMEGA, true);
                    } else if(event.getSubPlan() == SubscriptionPlan.TIER3) {
                        giveTrader(robot, event.getUser().getName(), 25, ItemTraderCore.CoreType.OMEGA, true);
                    } else {
                        giveTrader(robot, event.getUser().getName(), 5, ItemTraderCore.CoreType.COMMON, false);
                    }
                }

            }
        }

//            broadcast(String.format("[Twitch] %s just subscribed to %s for %d months", event.getUser().getName(), event.getChannel().getName(), event.getMonths()));
    }

    public static void giveTrader(ServerPlayerEntity player, String user, int value, ItemTraderCore.CoreType type, boolean mega) {
        ItemStack trader = ItemTraderCore.generate(user, value, mega, type);

        boolean added = player.addItemStackToInventory(trader);
        if(!added) {
            player.dropItem(trader, false, false);
        }
    }

    @EventSubscriber
    public void onSubMysteryGift(GiftSubscriptionsEvent event) {
        int amount = event.getCount();
        addSupport(event.getUser().getName(), amount * 5);
        if(server != null) {
            ServerPlayerEntity robot = server.getPlayerList().getPlayerByUsername("robotmonkey1000");
            if(robot != null) {
                ItemGiftBomb.Variant var;
                if(amount > 9) {
                    var = ItemGiftBomb.Variant.OMEGA;
                }
                else if(amount >= 5) {
                    var = ItemGiftBomb.Variant.MEGA;
                }
                else if(amount > 1) {
                    var = ItemGiftBomb.Variant.SUPER;
                } else {
                    var = ItemGiftBomb.Variant.NORMAL;
                }

                ItemStack trader = ItemGiftBomb.forGift(var, event.getUser().getName(), amount);

                boolean added = robot.addItemStackToInventory(trader);
                if(!added) {
                    robot.dropItem(trader, false, false);
                }
            }

        }

//        broadcast(String.format("[Twitch] Thank you %s for gifting %d subs to %s", event.getUser().getName(), event.getCount(), event.getChannel().getName()));
    }
    private static final String raffleMessage = "Drop A follow and I get a relic booster pack! Send me bits and I get them in game! 500 bits or more gets you a trader, 1500 bits gets you a mega head trader! \n" +
            "Send gift subs and become a gift statue! 1 Gift sub is a normal gift bomb, 5 is a mega gift bomb and 10 is OMEGA! \n" +
            "Want to become a trader and get emotes? All subs become traders as well! \n" +
            "Any support gets added to the raffle for a vault crystal at the end of the stream! !free for more information.\n";
    private static final String packMessage = "The pack being played is Vault Hunters by Iskall85 and his team! Twitch Integration is enabled: Use !raffle for more information.";
    private static final String replaceError = "Usage: !replacename twitchName minecraftName";
    private static final String freeMessage = "Want to take part but can't afford stuff? Me too! Simply chat, follow and watch to earn some support! Channel points can be used for relic boosters and chatting will make there be a chance of you spawning during vault runs!";
    private static final String relicMessage = "Relic Pack are items that increase my Vault Level with each use and has a chance of giving me relics that extend my time in the vault.";
    private static final String traderMessage = "What is a trader? Your MC character becomes a vendor of a random item in my world! Want to be a trader? Any sub or 500 bits will get you a trader core.";

    @EventSubscriber
    public void onChat(ChannelMessageEvent event) {
        addSupport(event.getUser().getName(), 0);
        if(event.getMessage().equalsIgnoreCase("!raffle")) {
            event.getTwitchChat().sendMessage(event.getChannel().getName(), raffleMessage);
        }

        if(event.getMessage().equalsIgnoreCase("!pack")) {
            event.getTwitchChat().sendMessage(event.getChannel().getName(), packMessage);
        }

        if(event.getMessage().equalsIgnoreCase("!free")) {
            event.getTwitchChat().sendMessage(event.getChannel().getName(), freeMessage);
        }

        if(event.getMessage().equalsIgnoreCase("!relic")) {
            event.getTwitchChat().sendMessage(event.getChannel().getName(), relicMessage);
        }

        if(event.getMessage().equalsIgnoreCase("!trader")) {
            event.getTwitchChat().sendMessage(event.getChannel().getName(), traderMessage);
        }


        if(event.getPermissions().contains(CommandPermission.MODERATOR) && event.getMessage().startsWith("!replacename")) {
            String[] args = event.getMessage().split(" ");
            if(args.length == 3) {
                mod.replaceSupporter(args[1], args[2]);
                event.getTwitchChat().sendMessage(event.getChannel().getName(), args[1] + " has been replaced with " + args[2] + ".");
            } else {
                event.getTwitchChat().sendMessage(event.getChannel().getName(), replaceError);
            }
        }
//        event.getTwitchChat().sendMessage(event.getChannel().getName(), event.getUser().getName());
//        ItemStack relicPack = new ItemStack(ModItems.RELIC_BOOSTER_PACK);
//        ITextComponent name = ITextComponent.getTextComponentOrEmpty(event.getUser().getName());
//        Style style = name.getStyle().setColor(Color.fromHex("#9ce62aff"));
//        relicPack.setDisplayName(name);
//        ServerPlayerEntity robot = server.getPlayerList().getPlayerByUsername("robotmonkey1000");
//        if(robot == null) {
//            //Enque
//        } else {
//            relicPack.setDisplayName(ITextComponent.getTextComponentOrEmpty(event.getUser().getName()));
//            boolean added = robot.addItemStackToInventory(relicPack);
//            if(!added) {
//                robot.dropItem(relicPack, false, false);
//            }
//        }
    }



    private void addSupport(String name, int amount) {

        ServerPlayerEntity robot = server.getPlayerList().getPlayerByUsername("robotmonkey1000");
        if(robot != null) {
            ArenaManager.addAmount(name, amount, robot);
            if(amount == 0) return;

            ServerWorld serverWorld = robot.getServerWorld();
            int exp = ModConfigs.PLAYER_EXP.getRelicBoosterPackExp() + amount;
            float coef = MathUtilities.randomFloat(0.5f, 1.5f);
            PlayerVaultStatsData.get(serverWorld).addVaultExp(robot, (int) (exp * coef));

        }


    }


}
