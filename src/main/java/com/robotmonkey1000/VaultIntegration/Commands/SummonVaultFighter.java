package com.robotmonkey1000.VaultIntegration.Commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.robotmonkey1000.VaultIntegration.VaultIntegration;
import iskallia.vault.entity.VaultFighterEntity;
import iskallia.vault.init.ModEntities;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import org.lwjgl.system.CallbackI;

import java.util.Random;

public class SummonVaultFighter extends Command {
    @Override
    public String getName() {
        return "vault_fighter";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder, VaultIntegration mod) {
        builder.then(Commands.argument("user", StringArgumentType.string()).executes((context) -> {
            summonFighter(context.getSource().asPlayer(), StringArgumentType.getString(context, "user"));
            return 0;
        }));
    }

    private void summonFighter(ServerPlayerEntity player, String user) {
//        VaultFighterEntity vaultEntity = (VaultFighterEntity) ModEntities.VAULT_FIGHTER.spawn(player.getServerWorld(), null, player, player.getPosition(), SpawnReason.STRUCTURE, false, false);
        VaultFighterEntity vaultFighterEntity = ModEntities.VAULT_FIGHTER.create(player.getServerWorld());
        if(vaultFighterEntity != null) {
            vaultFighterEntity.setCustomName(ITextComponent.getTextComponentOrEmpty(VaultIntegration.getRandomSupporter(true)));
            vaultFighterEntity.func_70071_h_();
            vaultFighterEntity.setPositionAndRotation(player.getPosition().getX() + 0.5F, player.getPosition().getY() + 0.2F, player.getPosition().getZ() + 0.5F, 0.0F, 0.0F);
            player.getServerWorld().summonEntity(vaultFighterEntity);
            vaultFighterEntity.func_213386_a(player.getServerWorld(), new DifficultyInstance(Difficulty.PEACEFUL, 13000L, 0L, 0.0F), SpawnReason.STRUCTURE, (ILivingEntityData)null, (CompoundNBT)null);
        }

    }

    @Override
    public boolean isDedicatedServerOnly() {
        return false;
    }
}
