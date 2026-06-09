package com.daria.semantic;

import java.util.HashMap;
import java.util.Map;

public class ClassInfo {

    private final String name;
    private final StructInfo fields;
    private final Map<String, FunctionInfo> methods = new HashMap<>();

    public ClassInfo(String name) {
        this.name = name;
        this.fields = new StructInfo(name);
    }

    public String getName() {
        return name;
    }

    public StructInfo getFields() {
        return fields;
    }

    public void addMethod(String methodName, FunctionInfo methodInfo) {
        if (methods.containsKey(methodName)) {
            throw new SemanticException("Method already defined: " + methodName + " in class " + name);
        }

        methods.put(methodName, methodInfo);
    }

    public FunctionInfo findMethod(String methodName) {
        return methods.get(methodName);
    }
}
