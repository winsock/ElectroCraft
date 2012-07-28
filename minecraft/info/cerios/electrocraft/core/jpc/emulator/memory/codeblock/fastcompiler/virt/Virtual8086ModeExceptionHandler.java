/*
    JPC: A x86 PC Hardware Emulator for a pure Java Virtual Machine
    Release Version 2.0

    A project from the Physics Dept, The University of Oxford

    Copyright (C) 2007-2009 Isis Innovation Limited

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 2 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

    Details (including contact information) can be found at: 

    www-jpc.physics.ox.ac.uk
*/

package info.cerios.electrocraft.core.jpc.emulator.memory.codeblock.fastcompiler.virt;

import java.io.*;
import java.util.Map;

import info.cerios.electrocraft.core.jpc.classfile.ClassFile;
import info.cerios.electrocraft.core.jpc.emulator.processor.*;

import info.cerios.electrocraft.core.jpc.emulator.memory.codeblock.fastcompiler.ExceptionHandler;

import static info.cerios.electrocraft.core.jpc.classfile.JavaOpcode.*;
/**
 * 
 * @author Chris Dennis
 */
public class Virtual8086ModeExceptionHandler extends ExceptionHandler
{
    public Virtual8086ModeExceptionHandler(int lastX86Position, Virtual8086ModeRPNNode initialNode, Map stateMap)
    {
	super(lastX86Position, initialNode, stateMap);
    }

    protected void writeHandlerRoutine(OutputStream byteCodes, ClassFile cf) throws IOException
    {
	byteCodes.write(ALOAD_1);
	byteCodes.write(SWAP);
	byteCodes.write(INVOKEVIRTUAL);
	try {
	    int cpIndex = cf.addToConstantPool(Processor.class.getDeclaredMethod("handleVirtual8086ModeException", new Class[] {ProcessorException.class}));
	    if (cpIndex > 0xffff)
		throw new IllegalStateException("Compilation ran out of constant pool slots");
	    byteCodes.write(cpIndex >>> 8);
	    byteCodes.write(cpIndex & 0xff);
	} catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
	}
    }
}