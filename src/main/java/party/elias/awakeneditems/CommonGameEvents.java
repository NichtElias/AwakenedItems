package party.elias.awakeneditems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

@EventBusSubscriber(modid = AwakenedItems.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CommonGameEvents {

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        if (event.getItemStack().has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
            if (event.getItemStack().is(Tags.Items.MELEE_WEAPON_TOOLS)) {
                event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "ai.attack_damage"),
                                event.getItemStack().get(AwakenedItems.AWAKENED_ITEM_COMPONENT).level(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                );
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().is(AwakenedItems.SOULSTUFF_ITEM) && event.getLevel().getBlockState(event.getPos()).is(Blocks.ANVIL)) {
            List<ItemEntity> itemEntities = event.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(event.getPos().above()));

            for (ItemEntity itemEntity: itemEntities) {
                if (!itemEntity.getItem().has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
                    itemEntity.getItem().set(AwakenedItems.AWAKENED_ITEM_COMPONENT, new AwakenedItemData(event.getEntity().getUUID()));
                    event.getItemStack().shrink(1);

                    //  flair
                    if (event.getLevel().isClientSide()) {
                        double x = itemEntity.getX();
                        double y = itemEntity.getY() + 0.25;
                        double z = itemEntity.getZ();

                        for (int i = 0; i < 20; i++) {
                            event.getLevel().addParticle(ParticleTypes.SOUL, x, y, z, Math.random() / 10 - 0.05, Math.random() / 10 - 0.05, Math.random() / 10 - 0.05);
                        }

                        event.getLevel().playLocalSound(event.getPos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.75f, 1, false);
                        event.getLevel().playLocalSound(event.getPos(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1, false);
                    }

                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        AwakenedItemData aiData = event.getItemStack().get(AwakenedItems.AWAKENED_ITEM_COMPONENT);
        if (aiData != null) {
            event.getToolTip().add(
                    Component.translatable("ai.awakeneditems.tooltip",
                            aiData.level()
                    ).withStyle(ChatFormatting.AQUA)
            );
            if (event.getContext().level() != null && event.getContext().level().getPlayerByUUID(aiData.getOwner()) != null) {
                event.getToolTip().add(
                        Component.translatable("ai.awakeneditems.tooltip.owner", event.getContext().level().getPlayerByUUID(aiData.getOwner()).getDisplayName())
                                .withStyle(ChatFormatting.DARK_AQUA)
                );
            } else {
                event.getToolTip().add(
                        Component.translatable("ai.awakeneditems.tooltip.owner", "???").withStyle(ChatFormatting.DARK_AQUA)
                );
            }
        }
    }

    @SubscribeEvent
    public static void onAttack(LivingIncomingDamageEvent event) {
        if (event.getSource().getWeaponItem() != null) {
            ItemStack weapon = event.getSource().getWeaponItem();
            if (weapon.has(AwakenedItems.AWAKENED_ITEM_COMPONENT) && weapon.is(Tags.Items.MELEE_WEAPON_TOOLS)) {
                AwakenedItemBehavior.addXp(weapon, 1, event.getEntity().level());
            }
        }
    }

    @SubscribeEvent
    public static void onItemExpire(ItemExpireEvent event) {
        if (event.getEntity().getItem().has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
            event.getEntity().setUnlimitedLifetime();
        }
    }
}
