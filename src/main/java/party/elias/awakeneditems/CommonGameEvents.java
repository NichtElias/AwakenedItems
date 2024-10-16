package party.elias.awakeneditems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

@EventBusSubscriber(modid = AwakenedItems.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CommonGameEvents {

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack item = event.getItemStack();
        AwakenedItemData aiData = item.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);
        if (aiData != null) {
            if (aiData.heldByOwner()) {
                if (item.is(Tags.Items.MELEE_WEAPON_TOOLS)) {
                    event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                                    ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "ai.attack_damage"),
                                    (double) aiData.level() / 10.0,
                                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                            ),
                            EquipmentSlotGroup.MAINHAND
                    );
                }
                if (item.is(Tags.Items.TOOLS)
                        && (!item.is(Tags.Items.MELEE_WEAPON_TOOLS) || item.is(ItemTags.AXES))
                        && !item.is(Tags.Items.RANGED_WEAPON_TOOLS)) {
                    event.addModifier(Attributes.BLOCK_BREAK_SPEED, new AttributeModifier(
                                    ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "ai.mining_speed"),
                                    (double) aiData.level() / 10.0,
                                    AttributeModifier.Operation.ADD_VALUE
                            ),
                            EquipmentSlotGroup.MAINHAND
                    );
                }
                if (item.is(Tags.Items.ARMORS)) {
                    double baseArmor = Utils.getSummedAttributeModifiers(item.getItem().getDefaultAttributeModifiers(item), Attributes.ARMOR, AttributeModifier.Operation.ADD_VALUE);
                    double baseToughness = Utils.getSummedAttributeModifiers(item.getItem().getDefaultAttributeModifiers(item), Attributes.ARMOR_TOUGHNESS, AttributeModifier.Operation.ADD_VALUE);

                    event.addModifier(Attributes.ARMOR, new AttributeModifier(
                                    ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "ai.armor"),
                                    (double) aiData.level() / 10.0 * baseArmor,
                                    AttributeModifier.Operation.ADD_VALUE
                            ),
                            EquipmentSlotGroup.ARMOR
                    );
                    event.addModifier(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                                    ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "ai.armor_toughness"),
                                    (double) aiData.level() / 10.0 * baseToughness,
                                    AttributeModifier.Operation.ADD_VALUE
                            ),
                            EquipmentSlotGroup.ARMOR
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack item = event.getItemStack();

        // awakening item
        if (item.is(AwakenedItems.SOULSTUFF_ITEM) && event.getLevel().getBlockState(event.getPos()).is(Blocks.ANVIL)) {
            List<ItemEntity> itemEntities = event.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(event.getPos().above()));

            for (ItemEntity itemEntity: itemEntities) {
                if (!itemEntity.getItem().has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
                    itemEntity.getItem().set(AwakenedItems.AWAKENED_ITEM_COMPONENT, new AwakenedItemData(event.getEntity().getUUID()));
                    item.shrink(1);

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

                    event.getEntity().hurt(new DamageSource(event.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                                    .getHolderOrThrow(DamageTypes.GENERIC)), 1); // represents binding it to yourself

                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (!event.isSimulated()) {
            ItemStack item = event.getHeldItemStack();

            if (item.get(AwakenedItems.AWAKENED_ITEM_COMPONENT) != null) {
                ItemAbility ability = event.getItemAbility();
                if (ability == ItemAbilities.SHOVEL_FLATTEN || ability == ItemAbilities.SHOVEL_DOUSE
                        || ability == ItemAbilities.AXE_STRIP || ability == ItemAbilities.AXE_SCRAPE || ability == ItemAbilities.AXE_WAX_OFF
                        || ability == ItemAbilities.HOE_TILL
                        || ability == ItemAbilities.FIRESTARTER_LIGHT
                        || ability == ItemAbilities.SHEARS_TRIM) {
                    AwakenedItemBehavior.addXp(item, 1, event.getContext().getLevel());
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
            if (event.getContext().level() != null && event.getContext().level().getPlayerByUUID(aiData.owner()) != null) {
                event.getToolTip().add(
                        Component.translatable("ai.awakeneditems.tooltip.owner", event.getContext().level().getPlayerByUUID(aiData.owner()).getDisplayName())
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
    public static void onLivingTookDamage(LivingDamageEvent.Post event) {
        for (EquipmentSlot slot: EquipmentSlot.values()) {
            ItemStack item = event.getEntity().getItemBySlot(slot);
            AwakenedItemData aiData = item.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

            if (slot.isArmor() && aiData != null) {
                AwakenedItemBehavior.addXp(item, 1, event.getEntity().level());
            }
        }
    }

    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        ItemStack item = event.getPlayer().getMainHandItem();

        if (item.get(AwakenedItems.AWAKENED_ITEM_COMPONENT) != null) {
            if (item.is(Tags.Items.TOOLS)
                && (!item.is(Tags.Items.MELEE_WEAPON_TOOLS) || item.is(ItemTags.AXES))
                && !item.is(Tags.Items.RANGED_WEAPON_TOOLS)) {
                AwakenedItemBehavior.addXp(item, 1, event.getPlayer().level());
            }
        }
    }

    @SubscribeEvent
    public static void onItemExpire(ItemExpireEvent event) {
        if (event.getEntity().getItem().has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
            event.getEntity().setUnlimitedLifetime();
            AwakenedItemBehavior.onItemWouldDespawn(event.getEntity().getItem(), event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity living) {
            for (EquipmentSlot slot: EquipmentSlot.values()) {
                ItemStack item = living.getItemBySlot(slot);

                if (item.has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
                    AwakenedItemBehavior.inventoryTick(item, living);
                }
            }

            if (living instanceof Player player) {
                for (ItemStack stack: player.getInventory().items) {
                    if (stack.has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
                        AwakenedItemBehavior.inventoryTick(stack, player);
                    }
                }

                player.setData(AwakenedItems.AWAKENED_ITEM_PLAYER_DATA_ATTACHMENT,
                        player.getData(AwakenedItems.AWAKENED_ITEM_PLAYER_DATA_ATTACHMENT).addTimeSinceLastItemMsg(1));
            }
        }
    }
}
