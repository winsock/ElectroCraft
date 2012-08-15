package info.cerios.electrocraft.core.items;

import net.minecraft.src.Item;

public class CopperIngot extends Item {

    protected CopperIngot(int itemId) {
        super(itemId);
    }

    @Override
    public String getTextureFile() {
        return "/info/cerios/electrocraft/gfx/items.png";
    }
}
