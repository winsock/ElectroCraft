package info.cerios.electrocraft.core.blocks;

import java.util.ArrayList;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.computer.IComputerCallback;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockComputer extends BlockNetwork implements IComputerCallback {

    public BlockComputer(int id) {
        super(id, 40, Material.iron);
    }

    @Override
    public int getBlockTextureFromSide(int side) {
        return ElectroBlocks.COMPUTER.getDefaultTextureIndices()[side];
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
        if (super.onBlockActivated(world, x, y, z, player, par6, par7, par8, par9)) {
            return true;
        }

        if (world.getBlockTileEntity(x, y, z) instanceof TileEntityComputer) {
            TileEntityComputer computerTileEntity = (TileEntityComputer) world.getBlockTileEntity(x, y, z);
            computerTileEntity.setActivePlayer(player);
            if (computerTileEntity != null) {
                if (computerTileEntity.getComputer() == null)
                    ElectroCraft.instance.getComputerHandler().createAndStartComputer(computerTileEntity, this);
                else
                    this.onTaskComplete(computerTileEntity);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onTaskComplete(Object... objects) {
        if (objects[0] instanceof TileEntityComputer) {
            TileEntityComputer tileComputer = (TileEntityComputer) objects[0];
            if (!ElectroCraft.instance.getComputerHandler().isComputerRunning(tileComputer.getComputer())) {
            	ElectroCraft.instance.getComputerHandler().startComputer(tileComputer, this);
            }
            ElectroCraft.electroCraftSided.openComputerGui(tileComputer);
        }
    }

	@Override
	public TileEntity createNewTileEntity(World var1) {
        return new TileEntityComputer();
	}
	
	@Override
    public void addCreativeItems(ArrayList itemList) { 
    	itemList.add(this);
    }
}
