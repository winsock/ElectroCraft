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
 * Constant pool element for a floating point float constant
 * @author Mike Moleschi
 */
public class FloatInfo extends ConstantPoolInfo
{
    private final float value;

    FloatInfo(DataInputStream in) throws IOException
    {
        this(in.readFloat());
    }

    /**
     * Constructs a constant pool element for the given float value.
     * @param val constant float value
     */
    public FloatInfo(float val)
    {
        super();
        value = val;
        hashCode = (FloatInfo.class.hashCode() * 31) ^ (Float.floatToRawIntBits(value) * 37);
    }

    /**
     * Returns the float value of this constant pool element.
     * @return constant float value
     */
    public float getValue()
    {
        return value;
    }

    public void write(DataOutputStream out) throws IOException
    {
        out.writeByte(FLOAT);
        out.writeFloat(value);
    }

    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (!(obj instanceof FloatInfo)) return false;

        return getValue() == ((FloatInfo) obj).getValue();
    }

    public String toString()
    {
        return "CONSTANT_Float_info : value=" + getValue();
    }
}