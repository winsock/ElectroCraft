package info.cerios.electrocraft.core.blocks;

import cpw.mods.fml.common.network.Player;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.World;

public abstract class BlockNetwork extends ElectroBlock {

    protected BlockNetwork(int id, int textureId, Material material) {
        super(id, textureId, material);
    }

    protected BlockNetwork(int id, Material material) {
        super(id, material);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
            ((NetworkBlock) world.getBlockTileEntity(x, y, z)).update((NetworkBlock) world.getBlockTileEntity(x, y, z));
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
    	if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
            ((NetworkBlock) world.getBlockTileEntity(x, y, z)).update((NetworkBlock) world.getBlockTileEntity(x, y, z));
        }
    	super.breakBlock(world, x, y, z, par5, par6);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
        if (ElectroCraft.instance.isShiftHeld()) {
            if (world.getBlockId(x, y, z) == this.blockID) {
                if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
                    ElectroCraft.electroCraftSided.openNetworkGui((NetworkBlock) world.getBlockTileEntity(x, y, z));
                    return true;
                }
            }
        }
        return false;
    }
}
