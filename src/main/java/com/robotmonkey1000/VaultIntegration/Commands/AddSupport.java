package com.robotmonkey1000.VaultIntegration.Commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.robotmonkey1000.VaultIntegration.Utility.ArenaManager;
import com.robotmonkey1000.VaultIntegration.VaultIntegration;
import iskallia.vault.Vault;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;

public class AddSupport extends Command {

    @Override
    public String getName() {
        return "add_support";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder, VaultIntegration mod) {
        builder.then(Commands.argument("user", StringArgumentType.string()).then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes((context) -> {
            ArenaManager.addAmount(StringArgumentType.getString(context, "user"), IntegerArgumentType.getInteger(context, "amount"), (PlayerEntity) context.getSource().getEntity());
            return 0;
        })));
    }

    @Override
    public boolean isDedicatedServerOnly() {
        return false;
    }
}
