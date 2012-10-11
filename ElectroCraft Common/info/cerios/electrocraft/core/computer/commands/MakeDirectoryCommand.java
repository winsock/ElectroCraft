package info.cerios.electrocraft.core.computer.commands;

import java.io.File;

import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.luaapi.ComputerFile;

public class MakeDirectoryCommand implements IComputerCommand {
	@Override
	public void onCommand(Computer computer, int argc, String[] argv) {
		ComputerFile directoryFile = new ComputerFile(computer.getBaseDirectory().getAbsolutePath() + "/" + computer.getCurrentDirectory() + "/" + argv[0], computer);
		
		if (directoryFile.exists()) {
			computer.getTerminal().print("ERROR: Error, requested folder already exists!");
			return;
		}
		
		directoryFile.mkdirs();
		if (!directoryFile.exists())
			computer.getTerminal().print("ERROR: Error, could not make the folder!");
	}
}
