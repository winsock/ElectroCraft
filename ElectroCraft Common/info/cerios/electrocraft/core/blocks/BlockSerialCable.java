package info.cerios.electrocraft.core.blocks;

import java.util.ArrayList;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntitySerialCable;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockSerialCable extends BlockNetwork {

	public static final int renderId = ElectroCraft.electroCraftSided.getFreeRenderId();
	
	public BlockSerialCable(int id) {
		super(id, 0, Material.cloth);
        this.setBlockBounds(0f, 0f, 0f, 1.0f, 0.55f, 1.0f);
	}
	
	@Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving) {
    	super.onBlockPlacedBy(world, x, y, z, entityliving);
    	
    	int connections = 0;
    	if (world.getBlockId(x + 1, y, z) == this.blockID) {
    		connections |= 1;
    	} else if (world.getBlockId(x - 1, y, z) == this.blockID) {
    		connections |= 2;
    	} else if (world.getBlockId(x, y + 1, z) == this.blockID) {
    		connections |= 4;
    	} else if (world.getBlockId(x, y - 1, z) == this.blockID) {
    		connections |= 8;
    	} else if (world.getBlockId(x, y, z + 1) == this.blockID) {
    		connections |= 16;
    	} else if (world.getBlockId(x, y, z - 1) == this.blockID) {
    		connections |= 32;
    	}
    	
    	connections = connections << 8;
    	ForgeDirection direction = ForgeDirection.values()[MathHelper.floor_double((double)(entityliving.rotationYaw * 4.0F / 360.0F + 0.5D)) & 0x3];
		int blockX = x + direction.offsetX;
		int blockY = y + direction.offsetY;
		int blockZ = z + direction.offsetZ;
		connections |= (byte)direction.ordinal();

    	world.setBlockMetadataWithNotify(x, y, z, connections);
    }
	
	@Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int nBlockId) {
		super.onNeighborBlockChange(world, x, y, z, nBlockId);
		int metadata = world.getBlockMetadata(x, y, z);
		int direction = metadata & 0xFF;
		
		int connections = 0;
    	if (world.getBlockId(x + 1, y, z) == this.blockID) {
    		connections |= 1;
    	} else if (world.getBlockId(x - 1, y, z) == this.blockID) {
    		connections |= 2;
    	} else if (world.getBlockId(x, y + 1, z) == this.blockID) {
    		connections |= 4;
    	} else if (world.getBlockId(x, y - 1, z) == this.blockID) {
    		connections |= 8;
    	} else if (world.getBlockId(x, y, z + 1) == this.blockID) {
    		connections |= 16;
    	} else if (world.getBlockId(x, y, z - 1) == this.blockID) {
    		connections |= 32;
    	}
    	
    	int newMetadata = connections << 8;
    	newMetadata |= direction;
    	
    	world.setBlockMetadata(x, y, z, newMetadata);
    }

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileEntitySerialCable();
	}
	
	@Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }
	
	@Override
	public int getRenderType() {
		return renderId;
	}
	
	@Override
    public void addCreativeItems(ArrayList itemList) { 
    	itemList.add(this);
    }
}
