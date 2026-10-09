package com.sharinganmod.client;

import com.sharinganmod.SharinganMod;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = SharinganMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(ModKeys.SCREEN);
        e.register(ModKeys.SHARINGAN);
        e.register(ModKeys.MANGEKYOU);
        e.register(ModKeys.SKILL1);
        e.register(ModKeys.SKILL2);
        e.register(ModKeys.SKILL3);
        e.register(ModKeys.SKILL4);
    }

    @SubscribeEvent
    public static void layers(RegisterGuiLayersEvent e) {
        e.registerBelow(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(SharinganMod.MODID, "vision"), ClientOverlay::renderVision);
        e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(SharinganMod.MODID, "hud"), ClientOverlay::renderHud);
    }
}
