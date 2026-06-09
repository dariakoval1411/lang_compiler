package com.daria.semantic;

import java.util.List;

public class FunctionInfo {

    private final Type returnType;
    private final List<Type> parameterTypes;

    public FunctionInfo(Type returnType, List<Type> parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public Type getReturnType() {
        return returnType;
    }
    public List<Type> getParameterTypes() {
        return parameterTypes;
    }
}
