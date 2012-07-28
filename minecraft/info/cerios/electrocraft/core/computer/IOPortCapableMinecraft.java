package info.cerios.electrocraft.core.computer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraft.src.Material;
import info.cerios.electrocraft.core.electricity.ElectricBlock;
import info.cerios.electrocraft.core.jpc.emulator.AbstractHardwareComponent;
import info.cerios.electrocraft.core.jpc.emulator.HardwareComponent;
import info.cerios.electrocraft.core.jpc.emulator.motherboard.IOPortCapable;
import info.cerios.electrocraft.core.jpc.emulator.motherboard.IOPortHandler;

public abstract class IOPortCapableMinecraft extends ElectricBlock implements IOPortCapable, HardwareComponent {

	private boolean ioportRegistered = false;
    protected ComputerHandler computerHandler;
	
	public IOPortCapableMinecraft(ComputerHandler computerHandler) {
		this.computerHandler = computerHandler;
		this.computerHandler.registerIOPortToAllComputers(this);
	}
    
	@Override
	public boolean initialised() {
		return ioportRegistered;
	}
	
	@Override
	public void reset() {
		ioportRegistered = false;
	}

	@Override
	public boolean updated() {
		return ioportRegistered;
	}

	@Override
	public void updateComponent(HardwareComponent component) {
		if ((component instanceof IOPortHandler) && component.updated()) {
			((IOPortHandler) component).registerIOPortCapable(this);
			ioportRegistered = true;
		}
	}

	@Override
	public void acceptComponent(HardwareComponent component) {
		if ((component instanceof IOPortHandler) && component.initialised()) {
			((IOPortHandler) component).registerIOPortCapable(this);
			ioportRegistered = true;
		}
	}
}
