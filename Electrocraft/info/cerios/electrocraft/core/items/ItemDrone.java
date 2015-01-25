package info.cerios.electrocraft.core.items;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import info.cerios.electrocraft.core.entites.EntityDrone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemDrone extends Item {
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            EntityDrone drone = new EntityDrone(worldIn);
            drone.setPositionAndRotation(pos.getX(), pos.getY() + 1, pos.getZ(), playerIn.rotationYaw, 0);
            worldIn.spawnEntityInWorld(drone);
            stack.stackSize--;
        }
        return true;
    }
}
