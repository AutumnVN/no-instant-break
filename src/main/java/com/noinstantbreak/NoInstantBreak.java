package com.noinstantbreak;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod(NoInstantBreak.MODID)
public class NoInstantBreak {
    public static final String MODID = "noinstantbreak";

    private final KeyMapping toggleKey = new KeyMapping("key.noinstantbreak.toggle", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, "key.categories.noinstantbreak");

    private boolean enabled = true;
    private long ticks;
    private long cooldownUntil = -1;

    public NoInstantBreak(FMLJavaModLoadingContext context) {
        MinecraftForge.EVENT_BUS.register(this);
        context.getModEventBus().addListener((RegisterKeyMappingsEvent event) -> event.register(toggleKey));
    }

    @SubscribeEvent
    public void onAttackInput(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!enabled || !event.isAttack() || minecraft.level == null || minecraft.player == null || !(minecraft.hitResult instanceof BlockHitResult hit)) {
            return;
        }
        if (ticks < cooldownUntil) {
            event.setCanceled(true);
            event.setSwingHand(false);
            return;
        }
        BlockState state = minecraft.level.getBlockState(hit.getBlockPos());
        if (!state.isAir() && state.getDestroyProgress(minecraft.player, minecraft.level, hit.getBlockPos()) >= 1.0F) {
            cooldownUntil = ticks + 6;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            cooldownUntil = -1;
            ticks = 0;
            return;
        }
        if (toggleKey.consumeClick()) {
            enabled = !enabled;
            if (!enabled) {
                cooldownUntil = -1;
            }
            minecraft.player.displayClientMessage(Component.translatable(enabled ? "noinstantbreak.on" : "noinstantbreak.off").withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED), true);
        }
        ticks++;
    }
}
