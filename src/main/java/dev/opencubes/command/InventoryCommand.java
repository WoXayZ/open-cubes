package dev.opencubes.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.opencubes.OCConstants;
import dev.opencubes.content.inventory.PlayerInventoryStore;
import dev.opencubes.content.tomfoolery.FlimFlamHandler;
import dev.opencubes.content.tomfoolery.FlimFlamRegistry;
import java.nio.file.Path;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class InventoryCommand {

    private static final SuggestionProvider<CommandSourceStack> DUMP_SUGGESTIONS = (context, builder) -> {
        var server = context.getSource().getServer();
        String remaining = builder.getRemaining().toLowerCase();
        return SharedSuggestionProvider.suggest(
                PlayerInventoryStore.INSTANCE.listDumpIds(server, remaining), builder);
    };

    private static final SuggestionProvider<CommandSourceStack> EFFECT_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(FlimFlamRegistry.INSTANCE.names(), builder);

    private InventoryCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("opencubes")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.literal("inventory")
                        .then(Commands.literal("store")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> store(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player")))))
                        .then(Commands.literal("restore")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("id", StringArgumentType.greedyString())
                                                .suggests(DUMP_SUGGESTIONS)
                                                .executes(ctx -> restore(ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        StringArgumentType.getString(ctx, "id")))))))
                .then(Commands.literal("flimflam")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> flimflam(ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player"), null))
                                .then(Commands.argument("effect", StringArgumentType.word())
                                        .suggests(EFFECT_SUGGESTIONS)
                                        .executes(ctx -> flimflam(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "effect"))))))
                .then(Commands.literal("luck")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> luck(ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player"), null))
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(ctx -> luck(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))));
        dispatcher.register(root);
    }

    private static int store(CommandSourceStack source, ServerPlayer player) {
        Path file = PlayerInventoryStore.INSTANCE.storePlayerInventory(player, "command");
        source.sendSuccess(() -> Component.translatable("commands.opencubes.inventory.stored",
                player.getGameProfile().name(),
                PlayerInventoryStore.stripFilename(file.getFileName().toString())), true);
        return 1;
    }

    private static int restore(CommandSourceStack source, ServerPlayer player, String id) {
        boolean ok = PlayerInventoryStore.INSTANCE.restoreInventory(player, id);
        if (ok) {
            source.sendSuccess(() -> Component.translatable("commands.opencubes.inventory.restored",
                    player.getGameProfile().name(),
                    PlayerInventoryStore.stripFilename(id)), true);
            return 1;
        }
        source.sendFailure(Component.translatable("commands.opencubes.inventory.missing", id));
        return 0;
    }

    private static int flimflam(CommandSourceStack source, ServerPlayer player, String effect) {
        boolean ok = effect == null
                ? FlimFlamHandler.forceRandom(player)
                : FlimFlamHandler.forceEffect(player, effect);
        if (ok) {
            source.sendSuccess(() -> Component.translatable("commands.opencubes.flimflam.ok",
                    player.getGameProfile().name(), effect == null ? "random" : effect), true);
            return 1;
        }
        source.sendFailure(Component.translatable("commands.opencubes.flimflam.fail",
                player.getGameProfile().name(), effect == null ? "random" : effect));
        return 0;
    }

    private static int luck(CommandSourceStack source, ServerPlayer player, Integer amount) {
        if (amount == null) {
            int value = FlimFlamHandler.getLuck(player);
            source.sendSuccess(() -> Component.translatable("commands.opencubes.luck.read",
                    player.getGameProfile().name(), value), false);
            return value;
        }
        int value = FlimFlamHandler.modifyLuck(player, amount);
        source.sendSuccess(() -> Component.translatable("commands.opencubes.luck.set",
                player.getGameProfile().name(), value), true);
        return value;
    }
}
