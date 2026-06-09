package com.daria.semantic;

public class StructFieldInfo {

    private final Type type;
    private final int index;

    public StructFieldInfo(Type type, int index) {
        this.type = type;
        this.index = index;
    }
    public Type getType() {
        return type;
    }
    public int getIndex() {
        return index;
    }

}
