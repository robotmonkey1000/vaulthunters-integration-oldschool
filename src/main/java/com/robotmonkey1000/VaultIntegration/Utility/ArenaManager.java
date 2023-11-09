package com.robotmonkey1000.VaultIntegration.Utility;

import com.robotmonkey1000.VaultIntegration.VaultIntegration;
import iskallia.vault.Vault;
import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.block.item.LootStatueBlockItem;
import iskallia.vault.client.gui.helper.ConfettiParticles;
import iskallia.vault.entity.*;
import iskallia.vault.init.*;
import iskallia.vault.item.ItemTraderCore;
import iskallia.vault.network.message.ArenaLevelMessage;
import iskallia.vault.util.MathUtilities;
import iskallia.vault.util.StatueType;
import iskallia.vault.world.data.EternalsData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkDirection;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArenaManager {

    private static int arenaAmount = 0;
    private static boolean arenaActive = false;
    private static MinecraftServer server;

    public static ArrayList<LivingEntity> bosses = new ArrayList<>();
    public static ArrayList<LivingEntity> fighters = new ArrayList<>();

    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent event) {
        if(!arenaActive) return;
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.START) {
//            fighters.removeIf(entity -> !entity.isAlive());
//            bosses.removeIf(entity -> !entity.isAlive());

            if(fighters.stream().noneMatch(LivingEntity::isAlive)) {
                ArenaManager.arenaLost();
            }
        }



    }


    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (!ArenaManager.arenaActive || event.getEntity().world.isRemote
                || !event.getEntity().getTags().contains("ArenaBoss")) return;

        if(ArenaManager.bosses.contains((LivingEntity) event.getEntity()))
        {
            if(ArenaManager.bosses.size() == 1) {

                ServerPlayerEntity player = server.getPlayerList().getPlayerByUsername("robotmonkey1000");
                ServerWorld world = (ServerWorld) event.getEntityLiving().world;

                LootContext.Builder builder = (new LootContext.Builder(world)).withRandom(world.rand)
                        .withParameter(LootParameters.THIS_ENTITY, player)
                        .withParameter(LootParameters.field_237457_g_, event.getEntity().getPositionVec())
                        .withParameter(LootParameters.DAMAGE_SOURCE, event.getSource())
                        .withNullableParameter(LootParameters.KILLER_ENTITY, event.getSource().getTrueSource())
                        .withNullableParameter(LootParameters.DIRECT_KILLER_ENTITY, event.getSource().getImmediateSource())
                        .withParameter(LootParameters.LAST_DAMAGE_PLAYER, player).withLuck(player.getLuck());

                LootContext ctx = builder.build(LootParameterSets.ENTITY);

                NonNullList<ItemStack> stacks = NonNullList.create();
                stacks.addAll(world.getServer().getLootTableManager().getLootTableFromLocation(Vault.id("chest/arena")).generate(ctx)); //TODO change for arena
                if(event.getSource().getImmediateSource() != null) {
                    if(event.getSource().getImmediateSource().getCustomName() != null) {
                        stacks.add(LootStatueBlockItem.forArenaChampion(event.getSource().getImmediateSource().getCustomName().getString(), StatueType.ARENA_CHAMPION.ordinal(), true));

                        if (world.rand.nextInt(4) != 0) {
                            stacks.add(ItemTraderCore.generate(event.getSource().getImmediateSource().getCustomName().getString(), 100, true, ItemTraderCore.CoreType.RAFFLE));
                        }
                    }


                }

                ItemStack crate = VaultCrateBlock.getCrateWithLoot(ModBlocks.VAULT_CRATE_ARENA, stacks);

                boolean added = player.addItemStackToInventory(crate);
                if (!added) {
                    player.dropItem(crate, false, false);
                }

                FireworkRocketEntity fireworks = new FireworkRocketEntity(world, event.getEntity().getPosX(),
                        event.getEntity().getPosY(), event.getEntity().getPosZ(), new ItemStack(Items.FIREWORK_ROCKET));
                world.addEntity(fireworks);

                world.playSound(null, player.getPosition(), ModSounds.ARENA_HORNS_SFX, SoundCategory.PLAYERS,
                        1.0F, 2f);

                ArenaManager.arenaWon();
            }
            if(ArenaManager.bosses.contains((LivingEntity) event.getEntity())) ArenaManager.bosses.remove((LivingEntity) event.getEntity());

        }

    }

    public static void setServer(MinecraftServer server) {
        ArenaManager.server = server;
    }

    public static void arenaLost() {
        resetArena();
    }

    public static void arenaWon() {
        resetArena();
    }

    public static void resetArena() {
        bosses.forEach(Entity::remove);
        fighters.forEach(Entity::remove);
        bosses.clear();
        fighters.clear();
        arenaActive = false;
    }

    public static void addAmount(String name, int amount, PlayerEntity player) {
        VaultIntegration.addSupporter(name, amount);
        if(arenaAmount + amount >= 20) {
            arenaAmount = (arenaAmount + amount) % 20;
            summonArena(player);
        } else {
            arenaAmount += amount;
        }

        ModNetwork.CHANNEL.sendTo(new ArenaLevelMessage(arenaAmount), ((ServerPlayerEntity)player).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);

    }

    protected static void fancyLevelUpEffects(ServerPlayerEntity player) {
        World world = player.world;

        Vector3d pos = player.getPositionVec();

        for (int i = 0; i < 20; ++i) {
            double d0 = world.rand.nextGaussian() * 1D;
            double d1 = world.rand.nextGaussian() * 1D;
            double d2 = world.rand.nextGaussian() * 1D;

            ((ServerWorld) world).spawnParticle(ParticleTypes.FIREWORK,
                    pos.getX() + world.rand.nextDouble() - 0.5,
                    pos.getY() + world.rand.nextDouble() - 0.5 + 3,
                    pos.getZ() + world.rand.nextDouble() - 0.5, 10, d0, d1, d2, 0.25D);
        }

        world.playSound(null, player.getPosition(), ModSounds.CONFETTI_SFX, SoundCategory.PLAYERS,
                1.0F, 1f);
    }

    private static void summonArena(PlayerEntity player) {
        fancyLevelUpEffects((ServerPlayerEntity) player);
        summonBoss();
        arenaActive = true;
    }

    private static void summonFighter(String name, BlockPos pos) {
        Random rand = new Random();
        int fighterDistance = 10;
        ServerPlayerEntity player = server.getPlayerList().getPlayerByUsername("robotmonkey1000");
        ArenaFighterEntity vaultFighterEntity = ModEntities.ARENA_FIGHTER.create(player.getServerWorld());
        if(vaultFighterEntity != null) {
            vaultFighterEntity.setCustomName(ITextComponent.getTextComponentOrEmpty(name));
            vaultFighterEntity.func_70071_h_();
            int xDiff = rand.nextInt(2) == 0 ? fighterDistance: -fighterDistance;
            int zDiff = rand.nextInt(2) == 0 ? fighterDistance: -fighterDistance;
            int yDiff = rand.nextInt(2) == 0 ? fighterDistance: 0;
            vaultFighterEntity.setPositionAndRotation(pos.getX() + xDiff, pos.getY() + yDiff, pos.getZ() + zDiff, 0.0F, 0.0F);
            player.getServerWorld().summonEntity(vaultFighterEntity);
            vaultFighterEntity.func_213386_a(player.getServerWorld(), new DifficultyInstance(Difficulty.PEACEFUL, 13000L, 0L, 0.0F), SpawnReason.STRUCTURE, (ILivingEntityData)null, (CompoundNBT)null);
            vaultFighterEntity.addPotionEffect(new EffectInstance(Effects.GLOWING, 100000));
            EntityScaler.scaleVault(vaultFighterEntity, VaultIntegration.getSupporters().get(name) + 25, new Random(), EntityScaler.Type.MOB);
            ArenaManager.fighters.add(vaultFighterEntity);
        }
    }
    private static void summonBoss() {

        ServerPlayerEntity player = server.getPlayerList().getPlayerByUsername("robotmonkey1000");
        BlockPos pos = player.getPosition();
        ArenaPlayerBossEntity boss = ModEntities.ARENA_BOSS.create(player.world);
        if (boss != null) {
            boss.changeSize(2.0F);

            boss.setPositionAndRotation((double) pos.getX() + 0.5D, (double) pos.getY() + 0.2D, (double) pos.getZ() + 0.5D, 0.0F, 0.0F);
            player.getServerWorld().summonEntity(boss);

            boss.getTags().add("ArenaBoss");
            bosses.add(boss);

            boss.bossInfo.setVisible(true);

            EntityScaler.scaleVault(boss, 30, new Random(), EntityScaler.Type.BOSS);
            boss.setCustomName(new StringTextComponent("robotmonkey1000"));
            boss.bossInfo.setName(new StringTextComponent("robotmonkey1000"));


            for(String name: VaultIntegration.getSupporters().keySet()) {
                summonFighter(name, boss.getPosition());
            }
        }

    }
}
