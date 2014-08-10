package info.cerios.electrocraft.core.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

public class ElectroDust extends Item {

    protected ElectroDust(int id) {
        Item.itemRegistry.addObject(id, "electroDust", this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister) {
        this.itemIcon = par1IconRegister.registerIcon("electrocraft:elctroDust");
    }
}
