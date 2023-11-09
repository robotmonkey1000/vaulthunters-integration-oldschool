package com.robotmonkey1000.VaultIntegration.Commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
//import iskallia.vault.init.ModItems;
import com.robotmonkey1000.VaultIntegration.VaultIntegration;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.ItemTraderCore;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public class GiveTraderCore extends Command {
    @Override
    public String getName() {
        return "give_trader";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder, VaultIntegration mod) {
        builder.then(Commands.argument("user", StringArgumentType.string()).executes((context) -> {
            giveTrader(context.getSource().asPlayer(), StringArgumentType.getString(context, "user"));
            return 0;
        }));
    }

    public static void giveTrader(ServerPlayerEntity player, String user) {
        ItemStack trader = ItemTraderCore.generate(user, 1, false, ItemTraderCore.CoreType.COMMON);

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
