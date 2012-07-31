package info.cerios.electrocraft.core.items;

import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraft.src.forge.ITextureProvider;

public class ElectroDust extends Item implements ITextureProvider {

	protected ElectroDust(int id) {
		super(id);
	}
	
	@Override
	public String getTextureFile(){
		return "/info/cerios/electrocraft/gfx/items.png";
	}
	
	// Borrowed from ItemRedstone
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7) {
        if (par3World.getBlockId(par4, par5, par6) != Block.snow.blockID) {
            
            switch (par7) {
            	case 0: --par5; break;
            	case 1: ++par5; break;
            	case 2: --par6; break;
            	case 3: ++par6; break;
            	case 4: --par4; break;
            	case 5: ++par4; break;
            }

            if (!par3World.isAirBlock(par4, par5, par6)) {
                return false;
            }
        }

        if (!par2EntityPlayer.canPlayerEdit(par4, par5, par6)) {
            return false;
        } else {
            if (ElectroBlocks.ELECTRO_WIRE.getBlock().canPlaceBlockAt(par3World, par4, par5, par6)) {
                --par1ItemStack.stackSize;
                par3World.setBlockWithNotify(par4, par5, par6, ElectroBlocks.ELECTRO_WIRE.getBlock().blockID);
            }
            return true;
        }
    }
}
