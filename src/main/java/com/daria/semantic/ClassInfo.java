package com.daria.semantic;

import java.util.HashMap;
import java.util.Map;

public class SemanticInfo {

    private final String name;
    private final StructInfo fields;
    private final Map<String,FunctionInfo> methods = new HashMap<>();

    public SemanticInfo(String name, StructInfo fields) {
        this.name = name;
        this.fields = new StructInfo(name);
    }

    public String getName() {
        return name;
    }
    public StructInfo getFields() {
        return fields;
    }

    public void addMethod(String name,FunctionInfo function){
        if(methods.containsKey(name)){
            throw new SemanticException("Method already defined: " + function + " in class " + name);
        }
        methods.put(name,function);
    }

    public FunctionInfo findMethod(String name){
        return methods.get(name);
    }
}
