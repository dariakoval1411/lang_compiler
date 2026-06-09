package com.daria.codegen;

public class VariableInfo {

    private LLVMType type;
    private String llvmName;
    private final boolean global;

    public VariableInfo(String llvmName, LLVMType type, boolean global) {
        this.llvmName = llvmName;
        this.type = type;
        this.global = global;
    }

    public LLVMType getType() {
        return type;
    }

    public String getLlvmName() {
        return llvmName;
    }

    public String getLlvmAddress() {
        return (global ? "@" : "%") + llvmName;
    }
    public boolean isGlobal() {
        return global;
    }

}