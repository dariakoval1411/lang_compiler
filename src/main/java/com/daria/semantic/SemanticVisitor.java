package com.daria.semantic;
import com.daria.codegen.ArraySemanticInfo;
import com.daria.parser.ExprBaseVisitor;
import com.daria.parser.ExprParser;

import java.util.*;


public class SemanticVisitor extends ExprBaseVisitor<Type> {

    private static class Scope {
        private final Map<String,Type> variables = new HashMap<>();
        private final Map<String,ArraySemanticInfo> arrays = new HashMap<>();
        private final Map<String,StructInfo>  structVariables = new HashMap<>();
        private final Map<String,ClassInfo> classVariables = new HashMap<>();
    }

    private final Deque<Scope> scopes = new ArrayDeque<>();
    private final Map<String,FunctionInfo> functions = new HashMap<>();
    private final Map<String,StructInfo> structs = new HashMap<>();
    private final Map<String, ClassInfo> classes = new HashMap<>();

    public SemanticVisitor(){
        scopes.push(new Scope());
    }

    private void enterScope(){
        scopes.push(new Scope());
    }

    private void exitScope(){
        scopes.pop();
    }

    private StructInfo findStructVariable(String variableName) {
        for(Scope scope : scopes){
            if(scope.structVariables.containsKey(variableName)) {
                return scope.structVariables.get(variableName);
            }
            if(scope.variables.containsKey(variableName) || scope.arrays.containsKey(variableName)) {
                return null;
            }
        }
        return null;
    }

    private Type findVariable(String variableName){
        for(Scope scope : scopes){
            if(scope.variables.containsKey(variableName)){
                return scope.variables.get(variableName);
            }
            if(scope.arrays.containsKey(variableName) || scope.structVariables.containsKey(variableName)){
                return null;
            }
        }
        return null;
    }

    private ArraySemanticInfo findArray(String arrayName){
        for(Scope scope : scopes){
            if(scope.arrays.containsKey(arrayName)){
                return scope.arrays.get(arrayName);
            }
            if(scope.variables.containsKey(arrayName) || scope.structVariables.containsKey(arrayName)) {
                return null;
            }
        }
        return null;
    }

    private FunctionInfo findFunction(String functionName){
        return functions.get(functionName);
    }

    private ClassInfo findClassVariable(String variableName){
        for(Scope scope : scopes){
            if(scope.classVariables.containsKey(variableName)){
                return scope.classVariables.get(variableName);
            }
            if (scope.variables.containsKey(variableName)
                    || scope.arrays.containsKey(variableName)
                    || scope.structVariables.containsKey(variableName)) {
                return null;
            }
        }
        return null;
    }

    private void declareClassVariable(String variableName, ClassInfo classInfo) {
        Scope currentScope = scopes.peek();

        if (currentScope != null) {
            if (currentScope.variables.containsKey(variableName)
                    || currentScope.arrays.containsKey(variableName)
                    || currentScope.structVariables.containsKey(variableName)
                    || currentScope.classVariables.containsKey(variableName)) {
                throw new SemanticException("Variable already defined: " + variableName);
            }

            currentScope.structVariables.put(variableName, classInfo.getFields());
            currentScope.classVariables.put(variableName, classInfo);
        }
    }

    private void declareFunction(String functionName,FunctionInfo returnType){
        if(functions.containsKey(functionName)){
            throw new SemanticException("Function already defined: " + functionName);
        }
        functions.put(functionName,returnType);
    }

    private void declareStructVariable(String variableName,StructInfo structVariable){
        Scope currentScope = scopes.peek();

        if(currentScope != null) {
            if(currentScope.structVariables.containsKey(variableName) || currentScope.arrays.containsKey(variableName)
            || currentScope.variables.containsKey(variableName)){
                throw new SemanticException("Variable already defined: " + variableName);
            }
            currentScope.structVariables.put(variableName, structVariable);
        }
    }

    private void declareVariable(String variableName, Type variableType){
        Scope currentScope = scopes.peek();

        if(currentScope != null){
            if(currentScope.variables.containsKey(variableName) || currentScope.arrays.containsKey(variableName)
            || currentScope.structVariables.containsKey(variableName)){
                throw new SemanticException("Variable already defined: " +variableName);
            }
            currentScope.variables.put(variableName, variableType);
        }
    }

    private void declareArray(String arrayName, Type elementType,int size){
        Scope currentScope = scopes.peek();

        if(currentScope != null){
            if(currentScope.arrays.containsKey(arrayName) || currentScope.variables.containsKey(arrayName)
            || currentScope.structVariables.containsKey(arrayName)){
                throw new SemanticException("Array already defined: " +arrayName);
            }
            currentScope.arrays.put(arrayName,new ArraySemanticInfo(elementType,size));
        }
    }

    public Type visitType(ExprParser.TypeContext ctx) {
        if(ctx.getText().equals("int")) {
            return Type.INT;
        }
        if(ctx.getText().equals("real")) {
            return Type.REAL;
        }

        throw new SemanticException("Unknow type: " + ctx.getText());
    }

    public Type visitMethodCall(ExprParser.MethodCallContext ctx) {
        String variableName = ctx.ID(0).getText();
        String methodName = ctx.ID(1).getText();

        ClassInfo classInfo = findClassVariable(variableName);
        if (classInfo == null) {
            throw new SemanticException("Class variable not declared: " + variableName);
        }

        FunctionInfo methodInfo = classInfo.findMethod(methodName);
        if (methodInfo == null) {
            throw new SemanticException("Method not declared: " + methodName + " in class " + classInfo.getName());
        }
        List<ExprParser.ExpressionContext> arguments = ctx.argumentList() == null ? List.of()
                        : ctx.argumentList().expression();

        List<Type> expectedTypes = methodInfo.getParameterTypes();

        if (arguments.size() != expectedTypes.size()) {
            throw new SemanticException("Method " + methodName + " expects " + expectedTypes.size() + " arguments, but got " + arguments.size());
        }

        for (int i = 0; i < arguments.size(); i++) {
            Type actualType = visit(arguments.get(i));
            Type expectedType = expectedTypes.get(i);

            if (!canAssign(expectedType, actualType)) {
                throw new SemanticException("Cannot assign " + actualType + " to " + expectedType);
            }
        }
        return methodInfo.getReturnType();
    }

    public Type visitClassDeclaration(ExprParser.ClassDeclarationContext ctx) {
        String className = ctx.ID().getText();

        if(classes.containsKey(className) || structs.containsKey(className)) {
            throw new SemanticException("Type already defined: " + className);
        }

        ClassInfo classInfo = new ClassInfo(className);

        for(ExprParser.StructFieldContext structField : ctx.structField()) {
            String fieldName = structField.ID().getText();
            Type fieldType = visitType(structField.type());

            classInfo.getFields().addField(fieldName, fieldType);
        }

        for(ExprParser.ClassMethodDeclarationContext method : ctx.classMethodDeclaration()) {
            String methodName = method.ID().getText();
            Type returnType = visitType(method.type());
            List<Type> parameterTypes = new ArrayList<>();

            if (method.parameterList() != null) {
                for (ExprParser.ParameterContext parameter
                        : method.parameterList().parameter()) {
                    parameterTypes.add(visitType(parameter.type()));
                }
            }

            classInfo.addMethod(methodName, new FunctionInfo(returnType, parameterTypes));
        }
        classes.put(className, classInfo);
        for(ExprParser.ClassMethodDeclarationContext method : ctx.classMethodDeclaration()) {
            validateClassMethod(classInfo, method);
        }
        return null;
    }

    public Type visitFieldAccess(ExprParser.FieldAccessContext ctx) {
        String variableName = ctx.ID(0).getText();
        String fieldName = ctx.ID(1).getText();

        StructInfo structInfo = findStructVariable(variableName);
        if(structInfo == null){
            throw new SemanticException("Struct variable not declared: " + variableName);
        }

        StructFieldInfo fieldInfo = structInfo.findField(fieldName);
        if(fieldInfo == null){
            throw new SemanticException("Field not declared: " + fieldName + " in struct: " + structInfo.getName());
        }
        return fieldInfo.getType();
    }

    public Type visitStructVariableDeclaration(ExprParser.StructVariableDeclarationContext ctx) {
        String structName = ctx.ID(0).getText();
        String variableName = ctx.ID(1).getText();

        StructInfo structInfo = structs.get(structName);
        if(structInfo != null){
            declareStructVariable(variableName, structInfo);
            return null;
        }

        ClassInfo classInfo = classes.get(structName);

        if(classInfo != null){
            declareClassVariable(variableName, classInfo);
            return null;
        }
        throw  new SemanticException("Struct or class not declared: " + structName);
    }

    public Type visitStructDeclaration(ExprParser.StructDeclarationContext ctx) {
        String structName = ctx.ID().getText();
        if(structs.containsKey(structName) || classes.containsKey(structName)){
            throw new SemanticException("Struct already defined: " + structName);
        }

        StructInfo structInfo = new StructInfo(structName);

        for(ExprParser.StructFieldContext field : ctx.structField()){
            String fieldName = field.ID().getText();
            Type fieldType = visitType(field.type());

            structInfo.addField(fieldName,fieldType);
        }

        structs.put(structName,structInfo);
        return null;
    }

    public Type visitGlobalDeclaration(ExprParser.GlobalDeclarationContext ctx) {
        String variableName = ctx.ID().getText();
        Type declarationType = visit(ctx.type());

        boolean isArray = ctx.INT_LITERAL() != null;

        if(isArray){
            if(ctx.literal() != null){
                throw new SemanticException("Global array initialization is not supported: " + variableName);
            }
            int size = Integer.parseInt(ctx.INT_LITERAL().getText());
            if (size <= 0) {
                throw new SemanticException("Array size must be greater than 0: " + variableName);
            }
            declareArray(variableName, declarationType, size);
            return declarationType;
        }
        if(ctx.literal() != null) {
            Type literalType = visit(ctx.literal());

            if(!canAssign(declarationType, literalType)){
                throw new SemanticException("Cannot assign : " + literalType + " to global variable of type" + declarationType);
            }
        }
        declareVariable(variableName, declarationType);
        return declarationType;
    }

    public Type visitLiteral(ExprParser.LiteralContext ctx) {
        if(ctx.INT_LITERAL() != null) {
            return Type.INT;
        }
        if(ctx.REAL_LITERAL() != null) {
            return Type.REAL;
        }
        throw new SemanticException("Unknow literal: " + ctx.getText());
    }

    public Type visitFunctionDeclaration(ExprParser.FunctionDeclarationContext ctx) {
        String functionName = ctx.ID().getText();
        Type returnType = visitType(ctx.type());

        List<Type> parameterTypes = new ArrayList<>();
        if(ctx.parameterList()!=null){
            for(ExprParser.ParameterContext parameter : ctx.parameterList().parameter()){
                Type parametrType = visitType(parameter.type());
                parameterTypes.add(parametrType);
            }
        }

        FunctionInfo functionInfo = new FunctionInfo(returnType,parameterTypes);
        declareFunction(functionName,functionInfo);

        enterScope();
        try{
            if(ctx.parameterList()!=null){
                for(ExprParser.ParameterContext parameter : ctx.parameterList().parameter()){
                    String parameterName = parameter.ID().getText();
                    Type parameterType = visitType(parameter.type());
                    declareVariable(parameterName,parameterType);
                }
            }

            for (ExprParser.StatementContext statement : ctx.statement()) {
                visit(statement);
            }

            Type actualReturnType = visit(ctx.expression());

            if(!canAssign(returnType,actualReturnType)){
                throw new SemanticException("Cannot return " + actualReturnType + " from function " + returnType);
            }
        }finally {
            exitScope();
        }

        return returnType;
    }

    public Type visitFunctionCall(ExprParser.FunctionCallContext ctx) {
        String functionName = ctx.ID().getText();
        FunctionInfo functionInfo = findFunction(functionName);

        if(functionInfo == null){
            throw new SemanticException("Function not declared : " + functionName);
        }

        List<Type> expressionTypes = functionInfo.getParameterTypes();
        List<ExprParser.ExpressionContext> arguments = ctx.argumentList() == null
                ? List.of() : ctx.argumentList().expression();

        if(arguments.size() != expressionTypes.size()){
            throw new SemanticException("Function " + functionName + " expects " + expressionTypes.size() + " arguments, but got " + arguments.size());
        }

        for(int i = 0; i < arguments.size(); i++){
            Type actualType = visit(arguments.get(i));
            Type expectedType = expressionTypes.get(i);

            if(!canAssign(expectedType,actualType)){
                throw new SemanticException("Cannot assign " + actualType + " to " + expectedType);
            }
        }
        return functionInfo.getReturnType();
    }

    public Type visitWhileStatement(ExprParser.WhileStatementContext ctx) {
        Type conditionType = visit(ctx.expression());

        if(conditionType != Type.BOOL){
            throw new SemanticException("Condition not defined: " + ctx.getText());
        }

        visit(ctx.block());
        return null;
    }

    public Type visitIfStatement(ExprParser.IfStatementContext ctx) {
        Type conditionType = visit(ctx.expression());

        if(conditionType != Type.BOOL){
            throw new SemanticException("If condition must be bool");
        }
        visit(ctx.block(0));
        if(ctx.block(1) != null){
            visit(ctx.block(1));
        }
        return null;
    }

    public Type visitProgram(ExprParser.ProgramContext ctx) {
        for(ExprParser.StructDeclarationContext struct : ctx.structDeclaration()){
            visit(struct);
        }

        for(ExprParser.ClassDeclarationContext classes : ctx.classDeclaration()){
            visit(classes);
        }

        for (ExprParser.GlobalDeclarationContext global : ctx.globalDeclaration()) {
            visit(global);
        }

        for (ExprParser.FunctionDeclarationContext function : ctx.functionDeclaration()) {
            visit(function);
        }

        for (ExprParser.StatementContext statement : ctx.statement()) {
            visit(statement);
        }

        return null;
    }

    public Type visitExpression(ExprParser.ExpressionContext ctx) {
        if(ctx.INT_LITERAL() != null) {
            return Type.INT;
        }
        if(ctx.REAL_LITERAL() != null) {
            return Type.REAL;
        }
        if(ctx.methodCall() != null) {
            return visitMethodCall(ctx.methodCall());
        }
        if(ctx.functionCall() != null) {
            return visit(ctx.functionCall());
        }
        if(ctx.fieldAccess() != null) {
            return visit(ctx.fieldAccess());
        }
        if(ctx.ID() != null) {
            String variableName = ctx.ID().getText();
            boolean isArrayAccess = !ctx.expression().isEmpty();

            if(isArrayAccess) {
                ArraySemanticInfo arrayInfo = findArray(variableName);

                if(arrayInfo == null) {
                    throw new SemanticException("Array not declared: " + variableName);
                }
                Type indexType = visit(ctx.expression(0));

                if(indexType != Type.INT) {
                    throw new SemanticException("Array index must be int");
                }

                if(ctx.expression(0).INT_LITERAL() !=null) {
                    int index = Integer.parseInt(ctx.expression(0).INT_LITERAL().getText());
                    if(index < 0 || index >= arrayInfo.getSize()) {
                        throw new SemanticException("Index out of bounds: " + index);
                    }
                }
                return arrayInfo.getElementType();
            }

            if(findArray(variableName)!=null) {
                throw new SemanticException("Cannot use array as normal expression: " + variableName);
            }

            Type variable = findVariable(variableName);

            if(variable == null) {
                throw new SemanticException("Variable not declared: " + variableName);
            }

            return variable;
        }

        if(ctx.expression().size() == 1) {
            return visit(ctx.expression(0));
        }
        Type left = visit(ctx.expression(0));
        Type right = visit(ctx.expression(1));

        String operator = ctx.op.getText();

        if(isComparisonOperator(operator)) {
            if(!isNumeric(left) || !isNumeric(right)) {
                throw new SemanticException("Invalid comparison operator: " + operator);
            }
            return Type.BOOL;
        }

        if(left == Type.REAL || right == Type.REAL) {
            return Type.REAL;
        }
        return Type.INT;
    }

    public Type visitDeclaration(ExprParser.DeclarationContext ctx) {
        String variableName = ctx.ID().getText();
        Type declaredType = visitType(ctx.type());

        boolean isArray = ctx.INT_LITERAL() != null;

        if(isArray) {
            if(ctx.expression() !=null) {
                throw new SemanticException("Array initialization is not supported: " + variableName);
            }
            int size = Integer.parseInt(ctx.INT_LITERAL().getText());
            if(size <=0) {
                throw new SemanticException("Array size must be greater then 0: " + variableName);
            }
            declareArray(variableName, declaredType, size);
            return declaredType;
        }

        if(ctx.expression() != null) {
            Type expressionType = visit(ctx.expression());

            if(!canAssign(declaredType, expressionType)) {
                throw new SemanticException("Cannot assign " + expressionType + " to " + declaredType);
            }
        }

        declareVariable(variableName, declaredType);
        return declaredType;
    }

    public Type visitAssignment(ExprParser.AssignmentContext ctx) {
        if(ctx.fieldAccess() != null) {
            Type fieldType = visit(ctx.fieldAccess());
            Type expressionType = visit(ctx.expression(0));

            if(!canAssign(fieldType, expressionType)) {
                throw new SemanticException("Cannot assign " + expressionType + " to struct field of type " + fieldType);
            }
            return fieldType;
        }

        String variableName = ctx.ID().getText();
        boolean isArrayAccess = ctx.expression().size() == 2;

        if(isArrayAccess) {
            ArraySemanticInfo arrayInfo = findArray(variableName);

            if(arrayInfo == null) {
                throw new SemanticException("Array not declared: " + variableName);
            }
            Type indexType = visit(ctx.expression(0));
            if(indexType != Type.INT) {
                throw new SemanticException("Array index must be int");
            }

            if(ctx.expression(0).INT_LITERAL() !=null) {
                int index = Integer.parseInt(ctx.expression(0).INT_LITERAL().getText());
                if(index < 0 || index >= arrayInfo.getSize()) {
                    throw new SemanticException("Index out of bounds: " + index);
                }
            }

            Type elementType = arrayInfo.getElementType();
            Type valueType = visit(ctx.expression(1));

            if(!canAssign(elementType, valueType)) {
                throw new SemanticException("Cannot assign " + valueType + " to array element of type " + elementType);
            }
            return elementType;
        }
        Type variable = findVariable(variableName);

        if(variable == null) {
            if(findArray(variableName)!=null) {
                throw new SemanticException("Cannot assign value to whole array: " + variableName);
            }
            throw new SemanticException("Variable not declared: " + variableName);
        }

        Type expressionType = visit(ctx.expression(0));

        if(!canAssign(variable, expressionType)) {
            throw new SemanticException("Cannot assign " + expressionType + " to " + variable);
        }
        return variable;
    }

    public Type visitInputStatement(ExprParser.InputStatementContext ctx) {
        ExprParser.ExpressionContext expression = ctx.expression();

        if(expression.fieldAccess() != null) {
            return visit(expression.fieldAccess());
        }

        if (expression.ID() == null) {
            throw new SemanticException("Input requires variable or array element");
        }

        String variableName = expression.ID().getText();
        boolean isArrayAccess = !expression.expression().isEmpty();

        if (isArrayAccess) {
            ArraySemanticInfo arrayInfo = findArray(variableName);
            if (arrayInfo == null) {
                throw new SemanticException("Array not declared: " + variableName);
            }

            Type indexType = visit(expression.expression(0));

            if (indexType != Type.INT) {
                throw new SemanticException("Array index must be int");
            }

            return arrayInfo.getElementType();
        }

        if (findArray(variableName)!=null) {
            throw new SemanticException("Input for whole array is not supported: " + variableName);
        }

        Type variable = findVariable(variableName);
        if(variable == null) {
            throw new SemanticException("Variable not declared: " + variableName);
        }

        return variable;
    }

    public Type visitShowStatement(ExprParser.ShowStatementContext ctx){
        ExprParser.ExpressionContext expression = ctx.expression();

        if(expression.ID() != null && expression.expression().isEmpty()) {
            String variableName = expression.ID().getText();
            ArraySemanticInfo arrayInfo = findArray(variableName);
            if (arrayInfo != null) {
                return arrayInfo.getElementType();
            }
        }
        return visit(ctx.expression());
    }

    public Type visitBlock(ExprParser.BlockContext ctx) {
        enterScope();
        try {
            for(ExprParser.StatementContext statement : ctx.statement()) {
                visit(statement);
            }
        }finally {
            exitScope();
        }
        return null;
    }

    private boolean canAssign(Type targetType,Type sourceType) {
        if(targetType == sourceType) {
            return true;
        }
        return targetType == Type.REAL && sourceType ==Type.INT;
    }

    private boolean isComparisonOperator(String operator) {
        return operator.equals("<")
                || operator.equals(">")
                || operator.equals("<=")
                || operator.equals(">=")
                || operator.equals("==")
                || operator.equals("!=");
    }

    private boolean isNumeric(Type type) {
        return type == Type.INT || type == Type.REAL;
    }

    private void validateClassMethod(ClassInfo  classInfo,ExprParser.ClassMethodDeclarationContext ctx) {
        Type returnType = visitType(ctx.type());

        enterScope();
        try {
            declareClassVariable("this", classInfo);

            if(ctx.parameterList() != null) {
                for(ExprParser.ParameterContext parameterContext : ctx.parameterList().parameter()) {
                    declareVariable(parameterContext.ID().getText(),visitType(parameterContext.type()));
                }
            }
            for(ExprParser.StatementContext statement : ctx.statement()) {
                visit(statement);
            }

            Type actualReturnType = visit(ctx.expression());

            if (!canAssign(returnType, actualReturnType)) {
                throw new SemanticException("Cannot return " + actualReturnType + " from method " + ctx.ID().getText());
            }
        }finally {
            exitScope();
        }
    }
}
