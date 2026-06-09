package com.daria.semantic;

import java.util.LinkedHashMap;
import java.util.Map;

public class StructInfo {

    private final String name;
    private final Map<String,StructFieldInfo> fields = new LinkedHashMap<>();

    public StructInfo(String name) {
        this.name = name;
    }

    public void addField(String name, Type type) {
        if(fields.containsKey(name)){
            throw new SemanticException("Field with name " + name + " already exists");
        }
        fields.put(name, new StructFieldInfo(type,fields.size()));
    }

    public StructFieldInfo findField(String name) {
        return fields.get(name);
    }

    public String getName() {
        return name;
    }

    public Map<String, StructFieldInfo> getFields() {
        return fields;
    }
}
