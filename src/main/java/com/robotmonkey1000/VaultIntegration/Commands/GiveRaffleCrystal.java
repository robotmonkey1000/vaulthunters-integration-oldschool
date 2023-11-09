package com.robotmonkey1000.VaultIntegration.Commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.robotmonkey1000.VaultIntegration.VaultIntegration;
import iskallia.vault.item.ItemTraderCore;
import iskallia.vault.item.ItemVaultCrystal;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public class GiveRaffleCrystal extends Command {

    @Override
    public String getName() {
        return "raffle_crystal";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder, VaultIntegration mod) {
        builder.then(Commands.argument("user", StringArgumentType.string()).executes((context) -> {
            giveRaffle(context.getSource().asPlayer(), StringArgumentType.getString(context, "user"));
            return 0;
        }));
    }

    public static void giveRaffle(ServerPlayerEntity player, String user) {
        ItemStack trader = ItemVaultCrystal.getCrystalWithBoss(user);
        boolean added = player.addItemStackToInventory(trader);
        if(!added) {
            player.dropItem(trader, false, false);
        }
    }

    @Override
    public boolean isDedicatedServerOnly() {
        return false;
    }
}
