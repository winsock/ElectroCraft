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
    
    public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
    	EntityDrone drone = new EntityDrone(par3World);
    	drone.setPosition(par4, par5, par6);
    	par3World.spawnEntityInWorld(drone);
    	return false;
    }
}
