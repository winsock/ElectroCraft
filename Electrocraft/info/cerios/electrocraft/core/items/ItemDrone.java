package info.cerios.electrocraft.core.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import info.cerios.electrocraft.core.entites.EntityDrone;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemDrone extends Item {
    public ItemDrone(int id) {
        Item.itemRegistry.addObject(id, "itemDrone",this);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
        if (!world.isRemote) {
            EntityDrone drone = new EntityDrone(world);
            drone.setPositionAndRotation(x, y + 1, z, 0, 0);
            world.spawnEntityInWorld(drone);
            stack.stackSize--;
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister) {
        this.itemIcon = par1IconRegister.registerIcon("electrocraft:droneItem");
    }
}
