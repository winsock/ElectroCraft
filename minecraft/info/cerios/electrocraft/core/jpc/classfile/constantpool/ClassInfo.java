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

package info.cerios.electrocraft.core.jpc.classfile.constantpool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Constant pool element for a class reference.
 * @author Mike Moleschi
 */
public class ClassInfo extends ConstantPoolInfo
{
    private final int nameIndex;

    ClassInfo(DataInputStream in) throws IOException
    {
        this(in.readUnsignedShort());
    }

    /**
     * Constructs a new ClassInfo object referencing the UTF8 class name at the
     * given constant pool index
     * @param val class name index
     */
    public ClassInfo(int val)
    {
        super();
        nameIndex = val;
        hashCode = (ClassInfo.class.hashCode() * 31) ^ (val * 37);
    }

    /**
     * Returns the constant pool index for the UTF8 entry containing the class
     * name.
     * @return class name index
     */
    public int getNameIndex()
    {
        return nameIndex;
    }

    public void write(DataOutputStream out) throws IOException
    {
        out.writeByte(CLASS);
        out.writeShort(nameIndex);
    }

    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (!(obj instanceof ClassInfo)) return false;

        return getNameIndex() == ((ClassInfo) obj).getNameIndex();
    }

    public String toString()
    {
        return "CONSTANT_Class_info : name=" + getNameIndex();
    }
}
