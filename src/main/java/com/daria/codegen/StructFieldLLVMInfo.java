package com.daria.codegen;

public class StructFieldLLVMInfo {

    private final LLVMType type;
    private final int index;

    public StructFieldLLVMInfo(LLVMType type, int index) {
        this.type = type;
        this.index = index;
    }

    public LLVMType getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }
}
