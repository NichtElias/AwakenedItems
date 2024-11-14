package party.elias.awakeneditems;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

public class ClientUtils {
    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }
}
