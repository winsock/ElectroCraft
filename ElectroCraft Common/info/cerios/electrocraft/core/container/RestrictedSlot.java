package info.cerios.electrocraft.core.container;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class RestrictedSlot extends Slot {
	
	private ItemStack[] items;
	private boolean allowed = false;
	
	public RestrictedSlot(IInventory par1iInventory, int par2, int par3, int par4, boolean allowed, ItemStack... items) {
		super(par1iInventory, par2, par3, par4);
		this.allowed = allowed;
		this.items = items;
	}

    public boolean isItemValid(ItemStack par1ItemStack) {
    	if (allowed) {
    		for (ItemStack stack : items) {
    			if (par1ItemStack.itemID == stack.itemID && ((par1ItemStack.isItemDamaged() || stack.isItemDamaged()) ? (par1ItemStack.getItemDamage() == stack.getItemDamage()) : true)) {
    				return true;
    			}
    		}
    		return false;
    	} else {
    		for (ItemStack stack : items) {
    			if (par1ItemStack.itemID == stack.itemID && ((par1ItemStack.isItemDamaged() || stack.isItemDamaged()) ? (par1ItemStack.getItemDamage() == stack.getItemDamage()) : true)) {
    				return false;
    			}
    		}
    		return true;
    	}
    }
}
