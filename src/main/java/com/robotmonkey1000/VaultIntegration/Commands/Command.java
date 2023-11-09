package com.robotmonkey1000.VaultIntegration.Commands;


import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.robotmonkey1000.VaultIntegration.VaultIntegration;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

public abstract class Command {
    public Command() {
    }

    public abstract String getName();

    public abstract int getRequiredPermissionLevel();

    public abstract void build(LiteralArgumentBuilder<CommandSource> var1, VaultIntegration mod);

    public abstract boolean isDedicatedServerOnly();

    protected final void sendFeedback(CommandContext<CommandSource> context, String message, boolean showOps) {
        (context.getSource()).sendFeedback(new StringTextComponent(message), showOps);
    }
}