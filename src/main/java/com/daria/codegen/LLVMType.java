package com.daria.codegen;

public enum LLVMType {
    I32("i32"), DOUBLE("double"), I1("i1");

    private final String llvmName;

    LLVMType(String llvmName) {
        this.llvmName = llvmName;
    }
    public String getLlvmName() {
        return llvmName;
    }
}
