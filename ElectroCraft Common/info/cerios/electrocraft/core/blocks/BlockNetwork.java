package info.cerios.electrocraft.core.blocks;

import java.io.IOException;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntitySerialCable;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Material;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;

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
    	super.breakBlock(world, x, y, z, par5, par6);
    	if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
            ((NetworkBlock) world.getBlockTileEntity(x, y, z)).update((NetworkBlock) world.getBlockTileEntity(x, y, z));
        }
    }
    
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int nBlockId) {
		super.onNeighborBlockChange(world, x, y, z, nBlockId);
		if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
            ((NetworkBlock) world.getBlockTileEntity(x, y, z)).update((NetworkBlock) world.getBlockTileEntity(x, y, z));
        }
	}

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
    	if (player instanceof EntityPlayerMP) {
    		if (ElectroCraft.instance.isShiftHeld()) {
    			if (world.getBlockId(x, y, z) == this.blockID) {
    				if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
    					GuiPacket guiPacket = new GuiPacket();
    					guiPacket.setCloseWindow(false);
    					guiPacket.setGui(Gui.ADDRESS_SCREEN);
    					try {
    						PacketDispatcher.sendPacketToPlayer(guiPacket.getMCPacket(), (Player) player);
    					} catch (IOException e) {
    						ElectroCraft.instance.getLogger().severe("Unable to send \"Open Address GUI Packet\"!");
    					}
    					return true;
    				}
    			}
    		}
    	}
    	return false;
    }
}
