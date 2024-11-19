package party.elias.awakeneditems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public enum AwakenedItemType implements StringRepresentable {
    BREAKING_TOOL(5, Set.of(), item -> {
            Tool tool = item.get(DataComponents.TOOL);
            return ((tool != null) && !tool.rules().isEmpty())
                    || item.is(ItemTags.PICKAXES) || item.is(ItemTags.SHOVELS) || item.is(ItemTags.AXES);
        }
    ),
    MELEE_WEAPON(5, Set.of(), item ->
            item.is(Tags.Items.MELEE_WEAPON_TOOLS) || item.is(ItemTags.SWORDS)
    ),
    ARMOR(5, Set.of(), item ->
            item.is(Tags.Items.ARMORS)
    ),
    SHIELD(5, Set.of(), item ->
            item.is(Tags.Items.TOOLS_SHIELD)
    ),
    FISHING_ROD(5, Set.of(), item ->
            item.is(Tags.Items.TOOLS_FISHING_ROD)
    ),
    CURIO(5, Set.of(), item ->
            item.is(AwakenedItems.TAG_CURIOS)
    );


    private final int specificity;
    private final Set<AwakenedItemType> subTypeOf;
    private final Predicate<ItemStack> predicate; // item is considered this type if this returns true

    AwakenedItemType(int specificity, Set<AwakenedItemType> subTypeOf, Predicate<ItemStack> predicate) {
        this.specificity = specificity;
        this.subTypeOf = subTypeOf;
        this.predicate = predicate;
    }

    public int getSpecificity() {
        return specificity;
    }

    public boolean isSubTypeOf(AwakenedItemType type) {
        return subTypeOf.contains(type);
    }

    public boolean checkItem(ItemStack item) {
        return predicate.test(item);
    }

    public boolean checkItemOnly(ItemStack item) {
        if (!checkItem(item))
            return false;

        for (AwakenedItemType type: values()) {
            if (type.checkItem(item) && type != this) {
                return false;
            }
        }

        return true;
    }

    public static List<AwakenedItemType> getItemTypes(ItemStack item) {
        List<AwakenedItemType> types = new ArrayList<>();

        for (AwakenedItemType type: values()) {
            if (type.checkItem(item)) {
                types.add(type);
            }
        }

        return types;
    }

    private static boolean emptyPredicate(ItemStack item) {
        return false;
    }

    @Override
    public String getSerializedName() {
        return String.valueOf(this).toLowerCase();
    }
}
