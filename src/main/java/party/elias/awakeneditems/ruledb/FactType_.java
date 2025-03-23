package party.elias.awakeneditems.ruledb;

import net.minecraft.util.StringRepresentable;

public enum FactType implements StringRepresentable {
    INT,
    DOUBLE,
    STRING,
    TAGS,
    BOOL;

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
