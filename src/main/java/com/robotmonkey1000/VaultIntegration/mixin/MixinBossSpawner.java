package com.robotmonkey1000.VaultIntegration.mixin;

import iskallia.vault.block.ObeliskBlock;
import iskallia.vault.config.VaultMobsConfig;
import iskallia.vault.entity.EntityScaler;
import iskallia.vault.entity.FighterEntity;
import iskallia.vault.entity.PlayerBossEntity;
import iskallia.vault.entity.VaultBoss;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModEntities;
import iskallia.vault.world.raid.VaultRaid;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ObeliskBlock.class)
public class MixinBossSpawner {

    @Inject(at = @At("HEAD"), method = "Liskallia/vault/block/ObeliskBlock;spawnBoss(Liskallia/vault/world/raid/VaultRaid;Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/util/math/BlockPos;Liskallia/vault/entity/EntityScaler$Type;)V", cancellable = true, remap=false)
    private void spawnBoss(VaultRaid raid, ServerWorld world, BlockPos pos, EntityScaler.Type type, CallbackInfo info) {
        if (type == EntityScaler.Type.BOSS) {
            if(raid.playerBossName != null && !raid.playerBossName.equalsIgnoreCase("")) {
                PlayerBossEntity boss = ModEntities.PLAYER_BOSS.create(world);
                if (boss != null) {
                    boss.changeSize(2.0F);

                    boss.setPositionAndRotation((double)pos.getX() + 0.5D, (double)pos.getY() + 0.2D, (double)pos.getZ() + 0.5D, 0.0F, 0.0F);
                    world.summonEntity(boss);

                    boss.getTags().add("VaultBoss");
                    raid.addBoss(boss);

                    boss.bossInfo.setVisible(true);

                    EntityScaler.scaleVault(boss, raid.level, new Random(), EntityScaler.Type.BOSS);
                    if (raid.playerBossName != null) {
                        boss.setCustomName(new StringTextComponent(raid.playerBossName));
                        boss.bossInfo.setName(new StringTextComponent(raid.playerBossName));
                    }

                    info.cancel();
                }
            }
        }
    }
}
