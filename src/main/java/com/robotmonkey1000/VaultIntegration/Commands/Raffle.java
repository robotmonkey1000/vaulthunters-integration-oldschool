package com.robotmonkey1000.VaultIntegration.Commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.robotmonkey1000.VaultIntegration.VaultIntegration;
import iskallia.vault.item.ItemVaultCrystal;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Random;

public class Raffle extends Command {

    @Override
    public String getName() {
        return "raffle";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder, VaultIntegration mod) {
        builder.executes((context) -> {
            raffle(mod.getSupporters(), context.getSource().asPlayer());
            return 0;
        });
    }

    public static void raffle(HashMap<String, Integer> supporters, ServerPlayerEntity player) {
        if(supporters.size() > 0) {
            int sum = 0;
            for(String supporter: supporters.keySet()) {
                sum += supporters.get(supporter);
            }
            Random rand = new Random();
            int rnd = rand.nextInt(sum);

            for(String supporter: supporters.keySet()) {
                if(rnd < supporters.get(supporter)) {
                    giveRaffle(player, supporter);
                    return;
                }
                rnd -= supporters.get(supporter);
            }
        }

        //Weighted Random.
//        ItemStack trader = ItemVaultCrystal.getCrystalWithBoss(user);
//        boolean added = player.addItemStackToInventory(trader);
//        if(!added) {
//            player.dropItem(trader, false, false);
//        }
    }

    public static void giveRaffle(ServerPlayerEntity player, String user) {
        ItemStack trader = ItemVaultCrystal.getCrystalWithBoss(user);
//        boolean added = player.addItemStackToInventory(trader);
        player.dropItem(trader, false, false);

    }

    @Override
    public boolean isDedicatedServerOnly() {
        return false;
    }
}
