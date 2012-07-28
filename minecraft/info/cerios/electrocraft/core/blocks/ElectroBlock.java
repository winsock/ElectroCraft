package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.electricity.ElectricBlock;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.forge.ITextureProvider;

public abstract class ElectroBlock extends BlockContainer implements ITextureProvider {
	
	protected ElectroBlock(int id, int textureId, Material material) {
		super(id, textureId, material);
	}
	
	protected ElectroBlock(int id, Material material) {
		super(id, material);
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        if (world.getBlockTileEntity(x, y, z) instanceof ElectricBlock) {
    		((ElectricBlock)world.getBlockTileEntity(x, y, z)).updateNetwork();
        }
        if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
    		((NetworkBlock)world.getBlockTileEntity(x, y, z)).updateComputerNetwork();
        }
	}
	
	@Override
	public void onBlockRemoval(World world, int x, int y, int z) {
        super.onBlockRemoval(world, x, y, z);
        if (world.getBlockTileEntity(x, y, z) instanceof ElectricBlock) {
    		((ElectricBlock)world.getBlockTileEntity(x, y, z)).updateNetwork();
        }
        if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
    		((NetworkBlock)world.getBlockTileEntity(x, y, z)).updateComputerNetwork();
        }
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
    	super.onNeighborBlockChange(world, x, y, z, id);
    }
	
	
	@Override
	public String getTextureFile(){
		return "/info/cerios/electrocraft/gfx/blocks.png";
	}
}
