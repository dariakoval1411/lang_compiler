package com.daria.codegen;

public class StructVariableInfo {

    private final String structName;
    private final String llvmName;

    public StructVariableInfo(String structName, String llvmName) {
        this.structName = structName;
        this.llvmName = llvmName;
    }

    public String getStructName() {
        return structName;
    }

    public String getLlvmAddress() {
        return "%" + llvmName;

    }

    public String getLlvmType() {
        return "%struct." + structName;
    }
}
