package info.cerios.electrocraft.core.blocks;

import org.lwjgl.input.Keyboard;

import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.gui.GuiNetworkAddressScreen;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
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
    		((NetworkBlock)world.getBlockTileEntity(x, y, z)).updateComputerNetwork();
    		((NetworkBlock)world.getBlockTileEntity(x, y, z)).getComputerNetwork().registerIOPort((NetworkBlock)world.getBlockTileEntity(x, y, z));
        }
	}
	
	@Override
	public void onBlockRemoval(World world, int x, int y, int z) {
        super.onBlockRemoval(world, x, y, z);
        if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
    		((NetworkBlock)world.getBlockTileEntity(x, y, z)).updateComputerNetwork();
    		((NetworkBlock)world.getBlockTileEntity(x, y, z)).getComputerNetwork().removeIOPort((NetworkBlock)world.getBlockTileEntity(x, y, z));
        }
	}
	
	@Override
	public boolean blockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer) {
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
			if (par1World.getBlockId(par2, par3, par4) == this.blockID) {
				if (par1World.getBlockTileEntity(par2, par3, par4) instanceof NetworkBlock) {
					ModLoader.getMinecraftInstance().displayGuiScreen(new GuiNetworkAddressScreen((NetworkBlock) par1World.getBlockTileEntity(par2, par3, par4)));
					return true;
				}
			}
		}
		return false;
	}
}
