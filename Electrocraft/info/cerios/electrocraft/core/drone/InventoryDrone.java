package info.cerios.electrocraft.core.drone;

import info.cerios.electrocraft.api.drone.upgrade.ICard;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.entites.EntityDrone;
import info.cerios.electrocraft.core.network.CustomPacket;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class InventoryDrone implements IInventory {

    public ItemStack[] mainInventory = new ItemStack[36];
    public ItemStack[] tools = new ItemStack[3];
    public ItemStack fuel;
    private EntityDrone drone;

    public InventoryDrone(EntityDrone drone) {
        this.drone = drone;
    }

    @Override
    public int getSizeInventory() {
        return mainInventory.length + tools.length + 1;
    }

    @Override
    public ItemStack getStackInSlot(int var1) {
        if (var1 >= 36) {
            var1 -= 36;
            if (var1 >= 3)
                return fuel;
            else
                return tools[var1];
        }
        return mainInventory[var1];
    }

    @Override
    public ItemStack decrStackSize(int var1, int var2) {
        ItemStack returnStack = null;
        ItemStack[] slots;

        if (var1 >= 36) {
            var1 -= 36;
            if (var1 >= 3) {
                var1 = 0;
                slots = new ItemStack[] { fuel };
            } else {
                slots = tools;
            }
        } else {
            slots = mainInventory;
        }

        if (slots[var1] == null)
            return null;
        if (var2 >= slots[var1].stackSize) {
            returnStack = slots[var1].copy();
            returnStack.stackSize = var2;
            slots[var1] = null;
        } else if (var2 > 0 && var2 < slots[var1].stackSize) {
            returnStack = slots[var1].splitStack(var2);
        }
        if (slots[var1] != null && slots[var1].stackSize <= 0) {
            slots[var1] = null;
        }

        if (slots.length == 36) {
            mainInventory = slots;
        } else if (slots.length == 3) {
            tools = slots;
        } else if (slots.length == 1) {
            fuel = slots[0];
        }

        return returnStack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int var1) {
        return getStackInSlot(var1);
    }

    @Override
    public void setInventorySlotContents(int var1, ItemStack var2) {
        ItemStack[] slots;

        if (var1 >= 36) {
            var1 -= 36;
            if (var1 >= 3) {
                var1 = 0;
                slots = new ItemStack[] { fuel };
            } else {
                slots = tools;
            }
        } else {
            slots = mainInventory;
        }
        slots[var1] = var2;
        if (slots.length == 36) {
            mainInventory = slots;
        } else if (slots.length == 3) {
            tools = slots;
        } else if (slots.length == 1) {
            fuel = slots[0];
        }
    }

    @Override
    public boolean hasCustomInventoryName() { return true; }

    @Override
    public String getInventoryName() {
        return "info.cerios.electrocraft.drone.inventory";
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        if (!drone.worldObj.isRemote) {
            if (drone.getDrone() != null) {
                if (tools[0] == null) {
                    drone.getDrone().setLeftCard(null, null);
                } else if (tools[0].getItem() instanceof ICard) {
                    drone.getDrone().setLeftCard((ICard) tools[0].getItem(), tools[0]);
                }
                if (tools[2] == null) {
                    drone.getDrone().setRightCard(null, null);
                } else if (tools[2].getItem() instanceof ICard) {
                    drone.getDrone().setRightCard((ICard) tools[2].getItem(), tools[2]);
                }
            }
            CustomPacket packet = new CustomPacket();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            NBTTagCompound inventory = new NBTTagCompound();
            writeToNBT(inventory);
            try {
                dos.writeInt(drone.getEntityId());
                CompressedStreamTools.write(inventory, dos);
                packet.id = 4;
                packet.data = bos.toByteArray();
                ElectroCraft.instance.getNetworkWrapper().sendToDimension(packet, drone.worldObj.provider.dimensionId);
            } catch (IOException e) {
                ElectroCraft.instance.getLogger().fine("Error sending inventory update to entity!");
            }
        }
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1) {
        return true;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        return true;
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        NBTTagList tagList = tagCompound.getTagList("inventory", Constants.NBT.TAG_LIST);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            byte slot = tag.getByte("slot");
            if (slot >= 0 && slot < getSizeInventory()) {
                setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(tag));
            }
        }
        if (!drone.worldObj.isRemote) {
            if (tools[0] == null) {
                drone.getDrone().setLeftCard(null, null);
            } else if (tools[0].getItem() instanceof ICard) {
                drone.getDrone().setLeftCard((ICard) tools[0].getItem(), tools[0]);
            }
            if (tools[2] == null) {
                drone.getDrone().setRightCard(null, null);
            } else if (tools[2].getItem() instanceof ICard) {
                drone.getDrone().setRightCard((ICard) tools[2].getItem(), tools[2]);
            }
        }
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (stack != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        tagCompound.setTag("inventory", itemList);
    }
}
