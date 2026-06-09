package com.daria.codegen;

public class ArrayInfo {

    private final LLVMType elementType;
    private final int size;
    private final String llvmName;
    private final boolean global;

    public ArrayInfo(LLVMType elementType, int size,String llvmName,boolean global) {
        this.elementType = elementType;
        this.size = size;
        this.llvmName = llvmName;
        this.global = global;
    }

    public LLVMType getElementType() {
        return elementType;
    }
    public int getSize() {
        return size;
    }

    public String getLlvmName() {
        return llvmName;
    }

    public String getLlvmAddress() {
        return (global ? "@" : "%") + llvmName;
    }

    public String getLlvmArrayType() {
        return "[" + size + " x " + elementType.getLlvmName() + "]";
    }
}
