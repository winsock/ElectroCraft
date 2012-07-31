package info.cerios.electrocraft.core.electricity;

import net.minecraft.src.Material;
import net.minecraft.src.NBTTagCompound;
import info.cerios.electrocraft.core.utils.ObjectTriplet;

public interface ElectricityReceiver {
	public abstract int getRequiredVoltage();
	public abstract float getCurrentDraw();
	public abstract boolean isOn();
	public abstract int getMaxVoltage();
}
