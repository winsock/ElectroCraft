package info.cerios.electrocraft.items;

import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.electricity.ElectricBlock;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.forge.ITextureProvider;

public class Rubber extends Item implements ITextureProvider {

	protected Rubber(int itemId) {
		super(itemId);
	}
	
	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7)
    {
		if (par3World.getBlockTileEntity(par4, par5, par6) != null && par3World.getBlockTileEntity(par4, par5, par6) instanceof ElectricBlock) {
			ElectricBlock block = (ElectricBlock) par3World.getBlockTileEntity(par4, par5, par6);
			par2EntityPlayer.addChatMessage("ElectroCraft: Voltage:" + String.valueOf(block.getVoltage()) + ", Current:" + String.valueOf(block.getCurrent()) + ", Current Type:" + block.getElectricityType().name());
			return true;
		}		

        return false;
    }

	@Override
	public String getTextureFile(){
		return "/info/cerios/electrocraft/gfx/items.png";
	}
}
