package com.sharinganmod.logic;

import com.sharinganmod.data.Skills;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Olho de Mangekyou: clique direito para implantar. kind: 1 Itachi, 2 Sasuke, 3 Obito (metade), 4 Shisui. */
public class EyeItem extends Item {
    public final int kind;

    public EyeItem(Properties props, int kind) {
        super(props);
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (player instanceof ServerPlayer sp && SharinganLogic.transplant(sp, kind)) {
            stack.shrink(1);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("Clique direito para implantar (requer Sharingan de 3 Tomoe).")
                .withStyle(ChatFormatting.GRAY));
        if (kind == 3) {
            tip.add(Component.literal("Metade do olho de Obito: Kamui com desgaste reduzido, recargas maiores.")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        } else if (kind == 1 || kind == 2) {
            tip.add(Component.literal("Junto do Mangekyou de " + Skills.TYPE_NAMES[kind == 1 ? 2 : 1]
                    + " forma o Mangekyou Eterno.").withStyle(ChatFormatting.DARK_RED));
        }
    }
}
