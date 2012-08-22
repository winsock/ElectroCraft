package info.cerios.electrocraft.core.computer.commands;

import java.io.File;

import info.cerios.electrocraft.core.computer.Computer;

public class MakeDirectoryCommand implements IComputerCommand {
	@Override
	public void onCommand(Computer computer, int argc, String[] argv) {
		File directoryFile = new File(computer.getBaseDirectory().getAbsolutePath() + File.separator + computer.getCurrentDirectory() + argv[0]);
		
		if (directoryFile.exists()) {
			computer.getTerminal().print("ERROR: Error, requested folder already exists!");
			return;
		}
		
		directoryFile.mkdirs();
		if (!directoryFile.exists())
			computer.getTerminal().print("ERROR: Error, could not make the folder!");
	}
}
