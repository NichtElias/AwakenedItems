package party.elias.awakeneditems;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AwakenedItems.MODID)
public class AwakenedItems {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "awakeneditems";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "awakeneditems" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "awakeneditems" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "awakeneditems" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final DeferredItem<Item> SOULSTUFF_ITEM = ITEMS.registerSimpleItem("soulstuff", new Item.Properties());

    // Creates a creative tab with the id "awakeneditems:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AI_TAB = CREATIVE_MODE_TABS.register("awakeneditems_tab", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.awakeneditems")).withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> SOULSTUFF_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(SOULSTUFF_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
    }).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AwakenedItemData>> AWAKENED_ITEM_COMPONENT = DATA_COMPONENTS.registerComponentType("awakened_item",
            (builder) -> builder.persistent(AwakenedItemData.CODEC).networkSynchronized(AwakenedItemData.STREAM_CODEC));

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AwakenedItems(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        DATA_COMPONENTS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Awakeneditems) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().is(SOULSTUFF_ITEM) && event.getLevel().getBlockState(event.getPos()).is(Blocks.ANVIL)) {
            List<ItemEntity> itemEntities = event.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(event.getPos().above()));

            for (ItemEntity itemEntity: itemEntities) {
                if (!itemEntity.getItem().has(AWAKENED_ITEM_COMPONENT)) {
                    itemEntity.getItem().set(AWAKENED_ITEM_COMPONENT, new AwakenedItemData(0, 0));
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
    public void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        if (event.getItemStack().has(AWAKENED_ITEM_COMPONENT)) {
            if (event.getItemStack().is(Tags.Items.MELEE_WEAPON_TOOLS)) {
                event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath(MODID, "ai.attack_damage"),
                            event.getItemStack().get(AWAKENED_ITEM_COMPONENT).level(),
                            AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                );
            }
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
