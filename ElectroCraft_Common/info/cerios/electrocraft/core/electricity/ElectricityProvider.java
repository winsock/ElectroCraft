package info.cerios.electrocraft.core.electricity;

import net.minecraft.src.Material;
import net.minecraft.src.NBTTagCompound;
import info.cerios.electrocraft.core.utils.ObjectTriplet;

public interface ElectricityProvider {
	public abstract int getVoltage();
	public abstract float getCurrent();
	public abstract ElectricityTypes getTypeOfCurrent();
}
