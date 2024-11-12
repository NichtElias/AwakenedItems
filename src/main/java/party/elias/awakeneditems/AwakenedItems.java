package party.elias.awakeneditems;

import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AwakenedItems.MODID)
public class AwakenedItems {
    public static final String MODID = "awakeneditems";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Item> TAG_CURIOS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "curios"));

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final DeferredItem<Item> SOULSTUFF_ITEM = ITEMS.registerSimpleItem("soulstuff", new Item.Properties());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AI_TAB = CREATIVE_MODE_TABS.register("awakeneditems_tab", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.awakeneditems")).withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> SOULSTUFF_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(SOULSTUFF_ITEM.get());
    }).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AwakenedItemData>> AWAKENED_ITEM_COMPONENT = DATA_COMPONENTS.registerComponentType("awakened_item",
            (builder) -> builder.persistent(AwakenedItemData.CODEC).networkSynchronized(AwakenedItemData.STREAM_CODEC));

    public static final Supplier<AttachmentType<AwakenedItemPlayerData>> AWAKENED_ITEM_PLAYER_DATA_ATTACHMENT = ATTACHMENT_TYPES.register("awakened_item_player_data",
            () -> AttachmentType.builder(() -> new AwakenedItemPlayerData(0)).serialize(AwakenedItemPlayerData.CODEC).copyOnDeath().build());

    public AwakenedItems(IEventBus modEventBus, ModContainer modContainer) {

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        ATTACHMENT_TYPES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class CommonModEvents {
        @SubscribeEvent
        public static void commonSetup(final FMLCommonSetupEvent event) {

        }

        @SubscribeEvent
        public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");

            registrar.playToClient(
                    ItemChatMessage.TYPE,
                    ItemChatMessage.STREAM_CODEC,
                    ItemChatMessage::handle
            );
        }
    }
}
