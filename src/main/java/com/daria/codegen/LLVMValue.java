package com.daria.codegen;

public class LLVMValue {

    private final String value;
    private final LLVMType type;

        public LLVMValue(String value, LLVMType type) {
        this.value = value;
        this.type = type;
    }
    public String getValue() {
        return value;
    }
    public LLVMType getType() {
        return type;
    }
}
