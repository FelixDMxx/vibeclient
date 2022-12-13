package de.felix.test;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.text.Text;

public class CustomCommandManager implements ModInitializer {
    @Override
    public void onInitialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("xray")
                    .then(ClientCommandManager.literal("add")
                            .then(ClientCommandManager.argument("block", BlockStateArgumentType.blockState(registryAccess))
                                    .executes(context -> {
                                        BlockState blockState = context.getArgument("block", BlockStateArgument.class).getBlockState();
                                        context.getSource().sendFeedback(Text.literal("Added Block: " + blockState.getBlock().getName().getString()));
                                        Test.xRayBlocks.add(blockState.getBlock());
                                        MinecraftClient.getInstance().worldRenderer.reload();
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .then(ClientCommandManager.literal("remove")
                            .then(ClientCommandManager.argument("block", BlockStateArgumentType.blockState(registryAccess))
                                    .executes(context -> {
                                        BlockState blockState = context.getArgument("block", BlockStateArgument.class).getBlockState();
                                        if (Test.xRayBlocks.contains(blockState.getBlock())) {
                                            context.getSource().sendFeedback(Text.literal("Removed Block: " + blockState.getBlock().getName().getString()));
                                            Test.xRayBlocks.remove(blockState.getBlock());
                                            MinecraftClient.getInstance().worldRenderer.reload();
                                        } else {
                                            context.getSource().sendFeedback(Text.literal("Block: " + blockState.getBlock().getName().getString() + " not found in the X-Ray list."));
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .then(ClientCommandManager.literal("list")
                            .executes(context -> {
                                context.getSource().sendFeedback(Text.literal(Test.xRayBlocks.toString()));
                                return Command.SINGLE_SUCCESS;
                            })
                    )
            );
        });
    }
}