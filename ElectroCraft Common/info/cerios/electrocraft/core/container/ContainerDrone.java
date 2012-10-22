package info.cerios.electrocraft.core.container;

import info.cerios.electrocraft.core.drone.InventoryDrone;
import info.cerios.electrocraft.core.entites.EntityDrone;
import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class ContainerDrone extends Container {
	
	public ContainerDrone(InventoryDrone inventory, InventoryPlayer playerInventory) {
		
		// Drone inv
        for (int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(inventory, (i * 9) + j, 8 + j * 18, 26 + i * 18));
            }
        }
        
		// Fuel slot
        addSlotToContainer(new Slot(inventory, 36 + 3, 8, 6));
        
        // Tool slots
        for (int i = 0; i < 3; i++) {
            addSlotToContainer(new Slot(inventory, 36 + i, 116 + i * 18, 6));
        }
		
		// Player inv
        for (int j = 0; j < 3; j++) {
            for(int i1 = 0; i1 < 9; i1++) {
                addSlotToContainer(new Slot(playerInventory, i1 + j * 9 + 9, 8 + i1 * 18, 84 + j * 18));
            }
        }
        
        // Player hotbar
        for (int k = 0; k < 9; k++) {
            addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
	}
	
	// Used to be transferStackInSlot
	@Override
	public ItemStack func_82846_b(EntityPlayer player, int slot) {
		 ItemStack stack = null;
		 Slot slotObject = (Slot) inventorySlots.get(slot);

		 // Null checks and checks if the item can be stacked (maxStackSize > 1)
		 if (slotObject != null && slotObject.getHasStack()) {
			 ItemStack stackInSlot = slotObject.getStack();
			 stack = stackInSlot.copy();

			 // Merges the item into player inventory since its in the tileEntity
			 if (slotObject.inventory instanceof InventoryDrone) {
				 if (!mergeItemStack(stackInSlot, 40, inventorySlots.size(), true)) {
					 return null;
				 }
			 // Placing it into the tileEntity is possible since its in the player inventory
			 } else if (!mergeItemStack(stackInSlot, 0, 1, false)) {
				 return null;
			 }

			 if (stackInSlot.stackSize == 0) {
				 slotObject.putStack(null);
			 } else {
				 slotObject.onSlotChanged();
			 }
		 }

		 return stack;
	 }
	
	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return true;
	}
}
