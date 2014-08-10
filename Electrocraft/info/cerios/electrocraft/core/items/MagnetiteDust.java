package info.cerios.electrocraft.core.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

public class MagnetiteDust extends Item {

    public MagnetiteDust(int itemId) {
        Item.itemRegistry.addObject(itemId, "magnetiteDust", this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister) {
        this.itemIcon = par1IconRegister.registerIcon("electrocraft:magnetiteDust");
    }
}
