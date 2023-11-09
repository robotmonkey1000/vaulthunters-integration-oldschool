package com.robotmonkey1000.VaultIntegration.Commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.robotmonkey1000.VaultIntegration.VaultIntegration;
import iskallia.vault.item.ItemGiftBomb;
import iskallia.vault.item.ItemVaultCrystal;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public class GiveSubGift extends Command{

    @Override
    public String getName() {
        return "sub_gift";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder, VaultIntegration mod) {
        builder.then(Commands.argument("user", StringArgumentType.string()).then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(this::subGift)));
    }

    private int subGift(CommandContext<CommandSource> context) throws CommandSyntaxException {
        giveSubGift(context.getSource().asPlayer(), StringArgumentType.getString(context, "user"), IntegerArgumentType.getInteger(context, "amount"));
        return 0;
    }

    public static void giveSubGift(ServerPlayerEntity player, String user, int amount) {
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

        ItemStack trader = ItemGiftBomb.forGift(var, user, amount);

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
