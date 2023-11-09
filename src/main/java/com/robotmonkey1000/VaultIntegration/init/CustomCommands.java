package com.robotmonkey1000.VaultIntegration.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.robotmonkey1000.VaultIntegration.Commands.*;
import com.robotmonkey1000.VaultIntegration.VaultIntegration;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import java.util.function.Supplier;

public class CustomCommands {

    public static GiveTraderCore GIVE_TRADER;
    public static GiveRaffleCrystal GIVE_RAFFLE;
    public static GiveSubGift GIVE_SUB_GIFT;
    public static Raffle RAFFLE;
    public static AddSupport ADD_SUPPORT;
    public static SummonVaultFighter VAULT_FIGHTER;

    public CustomCommands() {
    }

    public static void registerCommands(CommandDispatcher<CommandSource> dispatcher, Commands.EnvironmentType env, VaultIntegration mod) {
        GIVE_TRADER = registerCommand(GiveTraderCore::new, dispatcher, env, mod);
        GIVE_RAFFLE = registerCommand(GiveRaffleCrystal::new, dispatcher, env, mod);
        GIVE_SUB_GIFT = registerCommand(GiveSubGift::new, dispatcher, env, mod);
        RAFFLE = registerCommand(Raffle::new, dispatcher, env, mod);
        ADD_SUPPORT = registerCommand(AddSupport::new, dispatcher, env, mod);
        VAULT_FIGHTER = registerCommand(SummonVaultFighter::new, dispatcher, env, mod);
    }

    public static <T extends Command> T registerCommand(Supplier<T> supplier, CommandDispatcher<CommandSource> dispatcher, Commands.EnvironmentType env, VaultIntegration mod) {
        T command = (T) supplier.get();
        if (!command.isDedicatedServerOnly() || env == Commands.EnvironmentType.DEDICATED || env == Commands.EnvironmentType.ALL) {
            LiteralArgumentBuilder<CommandSource> builder = Commands.literal(command.getName());
            builder.requires((sender) -> sender.hasPermissionLevel(command.getRequiredPermissionLevel()));
            command.build(builder, mod);
            dispatcher.register(Commands.literal("vaultintegration").then(builder));
        }

        return command;
    }
}
