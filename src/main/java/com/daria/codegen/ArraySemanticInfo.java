package com.daria.codegen;

import com.daria.semantic.Type;

public class ArraySemanticInfo {

    private final Type elementType;
    private final int size;

    public ArraySemanticInfo(Type elementType, int size) {
        this.elementType = elementType;
        this.size = size;
    }
    public Type getElementType() {
        return elementType;
    }
    public int getSize() {
        return size;
    }
}
