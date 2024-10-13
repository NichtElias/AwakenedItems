package party.elias.awakeneditems;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AwakenedItems.MODID)
public class AwakenedItems {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "awakeneditems";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
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

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
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

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class CommonModEvents {
        @SubscribeEvent
        public static void commonSetup(final FMLCommonSetupEvent event) {
            // Some common setup code
            LOGGER.info("HELLO FROM COMMON SETUP");
        }
    }
}
