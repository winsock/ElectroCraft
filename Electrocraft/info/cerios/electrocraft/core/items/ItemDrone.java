package info.cerios.electrocraft.core.items;

import info.cerios.electrocraft.core.entites.EntityDrone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemDrone extends Item {
    public ItemDrone(int id) {
        super(id);
    }

    @Override
    public String getTextureFile() {
        return "/info/cerios/electrocraft/gfx/items.png";
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
}
