package party.elias.awakeneditems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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
                if (AwakenedItemType.MELEE_WEAPON.checkItem(item)) {
                    double baseDamage = Utils.getSummedAttributeModifiers(event.getDefaultModifiers(), Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE);

                    event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                                    ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "ai"),
                                    (double) aiData.level() / 20.0 * Mth.floor( 4.0 + baseDamage / 2.0),
                                    AttributeModifier.Operation.ADD_VALUE
                            ),
                            EquipmentSlotGroup.MAINHAND
                    );

                    event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(
                                    ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "ai"),
                                    (double) aiData.level() / 20.0,
                                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                            ),
                            EquipmentSlotGroup.MAINHAND
                    );
                }
                if (AwakenedItemType.BREAKING_TOOL.checkItem(item)) {

                    event.addModifier(Attributes.MINING_EFFICIENCY, new AttributeModifier(
                                    ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "ai"),
                                    aiData.level(),
                                    AttributeModifier.Operation.ADD_VALUE
                            ),
                            EquipmentSlotGroup.MAINHAND
                    );
                }
                if (AwakenedItemType.ARMOR.checkItem(item)) {
                    double baseArmor = Utils.getSummedAttributeModifiers(event.getDefaultModifiers(), Attributes.ARMOR, AttributeModifier.Operation.ADD_VALUE);
                    double baseToughness = Utils.getSummedAttributeModifiers(event.getDefaultModifiers(), Attributes.ARMOR_TOUGHNESS, AttributeModifier.Operation.ADD_VALUE);

                    event.addModifier(Attributes.ARMOR, new AttributeModifier(
                                    ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "ai"),
                                    (double) aiData.level() / 10.0 * baseArmor,
                                    AttributeModifier.Operation.ADD_VALUE
                            ),
                            EquipmentSlotGroup.ARMOR
                    );

                    event.addModifier(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                                    ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "ai"),
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
        if (item.is(AwakenedItems.SOULSTUFF_ITEM) && event.getLevel().getBlockState(event.getPos()).is(BlockTags.ANVIL)) {
            List<ItemEntity> itemEntities = event.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(event.getPos().above()));

            for (ItemEntity itemEntity: itemEntities) {
                if (!itemEntity.getItem().has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
                    itemEntity.getItem().set(AwakenedItems.AWAKENED_ITEM_COMPONENT, new AwakenedItemData(event.getEntity().getUUID(), AwakenedItemBehavior.getRandomPersonality()));
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

                    if (itemEntity.getItem().has(DataComponents.CUSTOM_NAME)) {
                        AwakenedItemBehavior.speakToOwner(itemEntity.getItem(), event.getLevel(), "awaken", 0, itemEntity.getItem().getHoverName());
                    } else {
                        AwakenedItemBehavior.speakToOwner(itemEntity.getItem(), event.getLevel(), "awaken-noname", 0, itemEntity.getItem().getHoverName());
                    }
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
                    AwakenedItemBehavior.maybeSpeakToOwner(0.01, item, event.getContext().getLevel(), "tooluse", 2000, event.getFinalState().getBlock().getName());
                    AwakenedItemBehavior.addXp(item, 1, event.getContext().getLevel());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        AwakenedItemData aiData = event.getItemStack().get(AwakenedItems.AWAKENED_ITEM_COMPONENT);
        if (aiData != null) {
            event.getToolTip().add(1,
                    Component.translatable("ai.awakeneditems.tooltip",
                            aiData.level()
                    ).withStyle(ChatFormatting.AQUA)
            );
            if (event.getContext().level() != null && event.getContext().level().getPlayerByUUID(aiData.owner()) != null) {
                event.getToolTip().add(2,
                        Component.translatable("ai.awakeneditems.tooltip.owner", event.getContext().level().getPlayerByUUID(aiData.owner()).getDisplayName())
                                .withStyle(ChatFormatting.DARK_AQUA)
                );
            } else {
                event.getToolTip().add(2,
                        Component.translatable("ai.awakeneditems.tooltip.owner", "???").withStyle(ChatFormatting.DARK_AQUA)
                );
            }
        }
    }

    @SubscribeEvent
    public static void onAttack(LivingIncomingDamageEvent event) {
        if (event.getSource().getWeaponItem() != null) {
            ItemStack weapon = event.getSource().getWeaponItem();
            if (weapon.has(AwakenedItems.AWAKENED_ITEM_COMPONENT) && AwakenedItemType.MELEE_WEAPON.checkItem(weapon)) {
                AwakenedItemBehavior.maybeSpeakToOwner(0.01, weapon, event.getEntity().level(), "weaponattack", 2000, event.getEntity().getType().getDescription());
                AwakenedItemBehavior.addXp(weapon, 2, event.getEntity().level());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTookDamage(LivingDamageEvent.Post event) {
        for (EquipmentSlot slot: EquipmentSlot.values()) {
            ItemStack item = event.getEntity().getItemBySlot(slot);
            AwakenedItemData aiData = item.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

            if (slot.isArmor() && aiData != null) {
                AwakenedItemBehavior.maybeSpeakToOwner(0.01, item, event.getEntity().level(), "armorprotect", 2000);
                AwakenedItemBehavior.addXp(item, 4, event.getEntity().level());
            }
        }
    }

    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        ItemStack item = event.getPlayer().getMainHandItem();

        if (item.get(AwakenedItems.AWAKENED_ITEM_COMPONENT) != null) {
            if (AwakenedItemType.BREAKING_TOOL.checkItem(item)) {
                AwakenedItemBehavior.maybeSpeakToOwner(0.005, item, (Level) event.getLevel(), "toolmine", 2000, event.getState().getBlock().getName());
                AwakenedItemBehavior.addXp(item, 1, event.getPlayer().level());
            }
        }
    }

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        ItemStack item = event.getBow();

        AwakenedItemData awakenedItemData = item.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        if (awakenedItemData != null) {
            AwakenedItemBehavior.addXp(item, 4, event.getLevel());
        }
    }

    @SubscribeEvent
    public static void onItemExpire(ItemExpireEvent event) {
        if (event.getEntity().getItem().has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
            event.getEntity().setUnlimitedLifetime();
            AwakenedItemBehavior.speakToOwner(event.getEntity().getItem(), event.getEntity().level(), "despawn", 20);
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            ItemStack item = event.getTo();

            AwakenedItemData aiData = item.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);
            AwakenedItemData prevAIData = event.getFrom().get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

            // TODO: if I ever give awakened items a uuid, use that to compare to the previous ItemStack instead of personality
            if (aiData != null && (prevAIData == null || !prevAIData.personality().equals(aiData.personality()))) {
                AwakenedItemBehavior.speakToOwner(item, event.getEntity().level(), "mobpickup", 20, Component.translatable(event.getEntity().getType().getDescriptionId()));
            }
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Utils.forAllAwakenedItemsOnEntity(event.getEntity(), (stack, entity) -> {
            AwakenedItemBehavior.speakToOwner(stack, entity.level(), "dimchange", 200, Component.translatable(
                    "dimension." + event.getTo().location().toLanguageKey()
            ));
        });
    }

    @SubscribeEvent
    public static void onLivingChangeTargetEvent(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Monster monster && event.getNewAboutToBeSetTarget() instanceof Player player
                && (monster.getTarget() == null || !monster.getTarget().equals(player))) {
            Utils.forAllAwakenedItemsOnEntity(player, (item, entity) -> {
                if (!player.level().isClientSide()) {
                    if (entity instanceof Creeper) {
                        AwakenedItemBehavior.maybeSpeakToOwner(0.5, item, entity.level(), "mobtarget", 50, Component.translatable(monster.getType().getDescriptionId()));
                    } else {
                        AwakenedItemBehavior.maybeSpeakToOwner(0.25, item, entity.level(), "mobtarget", 500, Component.translatable(monster.getType().getDescriptionId()));
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {

        if (event.getEntity() instanceof Projectile projectile && !event.loadedFromDisk()) {
            ItemStack weapon = projectile.getWeaponItem();

            if (weapon != null) {
                AwakenedItemData awakenedItemData = weapon.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

                if (awakenedItemData != null) {
                    Entity owner = projectile.getOwner();

                    if (owner != null && owner.getUUID().equals(awakenedItemData.owner())) {
                        projectile.setDeltaMovement(projectile.getDeltaMovement().scale(1 + awakenedItemData.level() / 10.0));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity living) {
            Utils.forAllAwakenedItemsOnEntity(living, AwakenedItemBehavior::inventoryTick);

            if (living instanceof Player player) {
                player.setData(AwakenedItems.AWAKENED_ITEM_PLAYER_DATA_ATTACHMENT,
                        player.getData(AwakenedItems.AWAKENED_ITEM_PLAYER_DATA_ATTACHMENT).addTimeSinceLastItemMsg(1));
            }
        }
    }
}
