/**
 * Find Security Bugs
 * Copyright (c) Philippe Arteau, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.h3xstream.findsecbugs.common;

import org.apache.bcel.generic.*;

public class ByteCode {


    public static void printOpCode(InstructionHandle insHandle, ConstantPoolGen cpg) {
        System.out.print("[" + String.format("%02d", insHandle.getPosition()) + "] ");
        printOpCode(insHandle.getInstruction(),cpg);
    }

    /**
     * Print the the detail of the given instruction (class, method, etc.)
     *
     * @param ins Instruction
     * @param cpg Constant Pool
     */
    public static void printOpCode(Instruction ins, ConstantPoolGen cpg) {

        if (ins instanceof InvokeInstruction) {
            InvokeInstruction invokeIns = (InvokeInstruction) ins;
            System.out.println(ins.getClass().getSimpleName() + " " + invokeIns.getClassName(cpg).replaceAll("\\.", "/") + "." + invokeIns.getMethodName(cpg) + invokeIns.getSignature(cpg));
        } else if (ins instanceof LDC) {
            LDC i = (LDC) ins;
            System.out.println(ins.getClass().getSimpleName() + " \""+i.getValue(cpg).toString()+"\"");
        } else if (ins instanceof NEW) {
            NEW i = (NEW) ins;
            ObjectType type = i.getLoadClassType(cpg);
            System.out.println(ins.getClass().getSimpleName() + " " + type.toString());
        } else if (ins instanceof LoadInstruction) {
            LoadInstruction i = (LoadInstruction) ins;
            System.out.println(ins.getClass().getSimpleName() +" "+i.getIndex() + " => [stack]");
        } else if (ins instanceof StoreInstruction) {
            StoreInstruction i = (StoreInstruction) ins;
            System.out.println(ins.getClass().getSimpleName() +" (?prev?) => "+i.getIndex() + "");
        } else if (ins instanceof FieldInstruction) {
            FieldInstruction i = (FieldInstruction) ins;
            System.out.println(ins.getClass().getSimpleName() +" "+i.getFieldName(cpg) + "");
        }  else if (ins instanceof IfInstruction) {
            IfInstruction i = (IfInstruction) ins;
            System.out.println(ins.getClass().getSimpleName() +" target => "+i.getTarget().toString()+ "");
        } else if (ins instanceof ICONST) {
            ICONST i = (ICONST) ins;
            System.out.println(ins.getClass().getSimpleName() +" "+i.getValue()+" ("+i.getType(cpg)+")");
        } else if (ins instanceof GOTO) {
            GOTO i = (GOTO) ins;
            System.out.println(ins.getClass().getSimpleName() +" target => "+i.getTarget().toString());
        } else {
            System.out.println(ins.getClass().getSimpleName());
        }
    }

    /**
     * Get the constant value of the given instruction.
     * (The instruction must refer to the Constant Pool otherwise null is return)
     *
     * @param <T> Type of the constant value
     * @param h Instruction Handle
     * @param cpg Constant Pool
     * @param clazz Type of the constant being read
     * @return The constant value if any is found
     */
    public static <T> T getConstantLDC(InstructionHandle h, ConstantPoolGen cpg, Class<T> clazz) {
        Instruction prevIns = h.getInstruction();
        if (prevIns instanceof LDC) {
            LDC ldcInst = (LDC) prevIns;
            Object val = ldcInst.getValue(cpg);
            if (val.getClass().equals(clazz)) {
                return clazz.cast(val);
            }
        }

        return null;
    }

    public static Integer getConstantInt(InstructionHandle h) {
        Instruction prevIns = h.getInstruction();
        if (prevIns instanceof ICONST) {
            ICONST ldcCipher = (ICONST) prevIns;
            Number num = ldcCipher.getValue();
            return num.intValue();
        }

        return null;
    }

    /**
     * Extract the number from a push operation (BIPUSH/SIPUSH).
     *
     * @param h Instruction Handle
     * @return The constant number if any is found
     */
    public static Number getPushNumber(InstructionHandle h) {
        Instruction prevIns = h.getInstruction();
        if (prevIns instanceof BIPUSH) {
            BIPUSH ldcCipher = (BIPUSH) prevIns;
            return ldcCipher.getValue();
        } else if (prevIns instanceof SIPUSH) {
            SIPUSH ldcCipher = (SIPUSH) prevIns;
            return ldcCipher.getValue();
        }
        return null;
    }

    /**
     * Get the previous instruction matching the given type of instruction (second parameter)
     *
     * @param startHandle Location to start from
     * @param clazz Type of instruction to look for
     * @return The instruction found (null if not found)
     */
    public static <T> T getPrevInstruction(InstructionHandle startHandle, Class<T> clazz) {
        InstructionHandle curHandle = startHandle;
        while (curHandle != null) {
            curHandle = curHandle.getPrev();

            if (curHandle != null && clazz.isInstance(curHandle.getInstruction())) {
                return clazz.cast(curHandle.getInstruction());
            }
        }
        return null;
    }
}
