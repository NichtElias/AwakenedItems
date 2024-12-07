package party.elias.awakeneditems.compat;

import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import party.elias.awakeneditems.AwakenedItemData;
import party.elias.awakeneditems.AwakenedItemType;
import party.elias.awakeneditems.AwakenedItems;
import party.elias.awakeneditems.Utils;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

import java.util.Map;

public class CuriosEvents {

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(CuriosEvents::onCurioAttributeModifier);
    }

    public static void onCurioAttributeModifier(CurioAttributeModifierEvent event) {
        ItemStack item = event.getItemStack();
        AwakenedItemData aiData = item.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);
        if (aiData != null) {
            if (aiData.heldByOwner()) {
                if (AwakenedItemType.CURIO.checkItem(item)) {

                    Multimap<Holder<Attribute>, AttributeModifier> modifiers = event.getOriginalModifiers();

                    for (Map.Entry<Holder<Attribute>, AttributeModifier> modifierEntry : modifiers.entries()) {

                        if (modifierEntry.getKey().is(AwakenedItems.AI_POWER_ATTRIBUTE.getId()))
                            continue;

                        event.addModifier(modifierEntry.getKey(), new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "curio." + modifierEntry.getValue().id().toLanguageKey()),
                                (double) aiData.level() / 10.0 * modifierEntry.getValue().amount() * Utils.getOwnerPower(item),
                                modifierEntry.getValue().operation()
                        ));

                    }

                    event.addModifier(AwakenedItems.AI_POWER_ATTRIBUTE, new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "ai." + BuiltInRegistries.ITEM.getKey(item.getItem()).getPath()),
                            (double) aiData.level() / 50.0,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ));

                }

                if (AwakenedItemType.GLIDER.checkItem(item)) {

                    event.addModifier(AwakenedItems.GLIDER_EFFICIENCY_ATTRIBUTE, new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "ai"),
                            (double) aiData.level() / 20.0 * Utils.getOwnerPower(item),
                            AttributeModifier.Operation.ADD_VALUE
                    ));
                }
            }
        }
    }
}
