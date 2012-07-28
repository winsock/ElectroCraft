package info.cerios.electrocraft.core.electricity;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.src.Material;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import info.cerios.electrocraft.core.utils.ObjectTriplet;
import info.cerios.electrocraft.core.utils.Orientations;

public interface ElectricityTransporter {
	public abstract float getMaxCurrent();
	public abstract float getResistance();
}
