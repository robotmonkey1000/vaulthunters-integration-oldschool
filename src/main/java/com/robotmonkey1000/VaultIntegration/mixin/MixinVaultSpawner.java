package com.robotmonkey1000.VaultIntegration.mixin;

import com.robotmonkey1000.VaultIntegration.VaultIntegration;
import iskallia.vault.entity.VaultFighterEntity;
import iskallia.vault.init.ModEntities;
import iskallia.vault.world.raid.VaultSpawner;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(VaultSpawner.class)
public class MixinVaultSpawner {
    @Shadow private List<LivingEntity> extraMobs;

    @Inject(at = @At("HEAD"), method = "spawn(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/util/math/BlockPos;)V", cancellable = true, remap=false)
    private void spawn(ServerWorld world, BlockPos pos, CallbackInfo info) {
        Random rand = new Random();
        int randInt = rand.nextInt(4);
        if(randInt == 1) {
            VaultFighterEntity vaultFighterEntity = ModEntities.VAULT_FIGHTER.create(world);
            if(vaultFighterEntity != null) {

                vaultFighterEntity.setPositionAndRotation(pos.getX() + 0.5F, pos.getY() + 0.2F, pos.getZ() + 0.5F, 0.0F, 0.0F);
                world.summonEntity(vaultFighterEntity);
                vaultFighterEntity.func_213386_a(world, new DifficultyInstance(Difficulty.PEACEFUL, 13000L, 0L, 0.0F), SpawnReason.STRUCTURE, (ILivingEntityData)null, (CompoundNBT)null);
                vaultFighterEntity.setCustomName(ITextComponent.getTextComponentOrEmpty(VaultIntegration.getRandomSupporter(true)));
                vaultFighterEntity.func_70071_h_();

                this.extraMobs.add(vaultFighterEntity);
            }
            info.cancel();
        }
    }
}
