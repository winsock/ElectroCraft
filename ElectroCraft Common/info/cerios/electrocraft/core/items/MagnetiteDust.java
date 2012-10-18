package info.cerios.electrocraft.core.items;

import info.cerios.electrocraft.core.entites.EntityDrone;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class MagnetiteDust extends Item {

    public MagnetiteDust(int itemId) {
        super(itemId);
    }

    @Override
    public String getTextureFile() {
        return "/info/cerios/electrocraft/gfx/items.png";
    }
}
