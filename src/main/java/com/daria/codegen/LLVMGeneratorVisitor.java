package com.daria.codegen;
import com.daria.parser.ExprBaseVisitor;
import com.daria.parser.ExprParser;
import java.util.*;

public class LLVMGeneratorVisitor extends ExprBaseVisitor<LLVMValue> {

    private static class Scope {
        private final Map<String, VariableInfo> variables = new HashMap<>();
        private final Map<String,ArrayInfo> arrays = new HashMap<>();
        private final Map<String, StructVariableInfo> structVariableInfo = new HashMap<>();
    }

    private final Deque<Scope> scopes = new ArrayDeque<>();
    private final StringBuilder codeBuilder = new StringBuilder();
    private final Map<String,List<LLVMType>> functionParameterTypes = new HashMap<>();
    private final Map<String,LLVMType> functionReturnTypes = new HashMap<>();
    private final Map<String,Map<String,StructFieldLLVMInfo>> structFields = new HashMap<>();
    private int registerCounter = 1;
    private int labelCounter = 1;

    private String newLabel(String prefix){
        return prefix + "." +labelCounter++;
    }

    private String newRegister() {
        return "%r"  + registerCounter++;
    }

    public String getLLMVCode() {
        return codeBuilder.toString();
    }

    private void emit(String line){
        codeBuilder.append(line).append("\n");
    }

    public LLVMGeneratorVisitor(){
        scopes.push(new Scope());
    }

    public void enterScope(){
        scopes.push(new Scope());
    }

    public void exitScope(){
        scopes.pop();
    }

    public StructVariableInfo declareStructVariable(String variableName,String structName){
        String llvmName = variableName + "." + registerCounter++;
        StructVariableInfo info = new StructVariableInfo(structName, llvmName);

        scopes.peek().structVariableInfo.put(variableName, info);
        return info;
    }

    public ArrayInfo declareGlobalArray(String arrayName,LLVMType type,int size){
        ArrayInfo arrayInfo = new ArrayInfo(type,size,arrayName,true);

        scopes.peek().arrays.put(arrayName,arrayInfo);
        return arrayInfo;
    }

    public VariableInfo declareVariable(String variableName, LLVMType type) {
        String llvmName = variableName + "." + registerCounter++;
        VariableInfo variableInfo = new VariableInfo(llvmName, type,false);

        scopes.peek().variables.put(variableName, variableInfo);
        return variableInfo;
    }

    private StructVariableInfo findStructVariableInfo(String variableName){
        for(Scope scope : scopes){
            if(scope.structVariableInfo.containsKey(variableName)){
                return scope.structVariableInfo.get(variableName);
            }
            if(scope.variables.containsKey(variableName) || scope.arrays.containsKey(variableName)){
                return null;
            }
        }
        return null;
    }

    private LLVMValue generateFieldPointer(ExprParser.FieldAccessContext ctx){
        String variableName = ctx.ID().get(0).getText();
        String fieldName = ctx.ID().get(1).getText();

        StructVariableInfo structInfo = findStructVariableInfo(variableName);
        StructFieldLLVMInfo fieldInfo = structFields.get(structInfo.getStructName()).get(fieldName);

        String pointer = newRegister();

        emit("  " + pointer
                + " = getelementptr "
                + structInfo.getLlvmType()
                + ", "
                + structInfo.getLlvmType()
                + "* "
                + structInfo.getLlvmAddress()
                + ", i32 0, i32 "
                + fieldInfo.getIndex());

        return new LLVMValue(pointer,fieldInfo.getType());
    }

    private VariableInfo findVariableInfo(String variableName){
        for(Scope scope: scopes){
            if(scope.variables.containsKey(variableName)){
                return scope.variables.get(variableName);
            }

            if(scope.arrays.containsKey(variableName)){
                return null;
            }
        }
        return null;
    }


    public VariableInfo declareGlobalVariable(String variableName, LLVMType type) {
        VariableInfo variableInfo = new VariableInfo(variableName, type,true);

        scopes.peek().variables.put(variableName, variableInfo);
        return variableInfo;
    }

    public ArrayInfo declareArray(String arrayName, LLVMType type,int size) {
        String llvmName = arrayName + "." + registerCounter++;
        ArrayInfo arrayInfo = new ArrayInfo(type,size,llvmName,false);

        scopes.peek().arrays.put(arrayName, arrayInfo);
        return arrayInfo;
    }

    private ArrayInfo findArrayInfo(String arrayName){
        for(Scope scope: scopes){
            if(scope.arrays.containsKey(arrayName)){
                return scope.arrays.get(arrayName);
            }
            if(scope.variables.containsKey(arrayName)){
                return null;
            }
        }
        return null;
    }

    public LLVMValue visitMethodCall(ExprParser.MethodCallContext ctx) {
        String variableName = ctx.ID(0).getText();
        String methodName = ctx.ID(1).getText();
        StructVariableInfo objectInfo = findStructVariableInfo(variableName);
        String methodKey = objectInfo.getStructName() + "." + methodName;

        LLVMType returnType = functionReturnTypes.get(methodKey);
        List<LLVMType> expectedTypes = functionParameterTypes.get(methodKey);
        List<ExprParser.ExpressionContext> arguments = ctx.argumentList() == null ? List.of() : ctx.argumentList().expression();
        StringJoiner llvmArguments = new StringJoiner(", ");

        llvmArguments.add(objectInfo.getLlvmType() + "* " + objectInfo.getLlvmAddress());

        for (int i = 0; i < arguments.size(); i++) {
            LLVMValue value = visit(arguments.get(i));
            LLVMType expectedType = expectedTypes.get(i);

            if (expectedType == LLVMType.DOUBLE && value.getType() == LLVMType.I32) {
                value = convertIntToDouble(value);
            }

            llvmArguments.add(expectedType.getLlvmName() + " " + value.getValue());
        }

        String register = newRegister();
        emit("  " + register
                + " = call "
                + returnType.getLlvmName()
                + " @" + methodKey
                + "(" + llvmArguments + ")");

        return new LLVMValue(register, returnType);
    }

    public LLVMValue visitClassDeclaration(ExprParser.ClassDeclarationContext ctx) {
        String className = ctx.ID().getText();
        StringJoiner fields = new StringJoiner(", ");
        Map<String, StructFieldLLVMInfo> fieldInfos = new HashMap<>();

        int index = 0;

        for (ExprParser.StructFieldContext field : ctx.structField()) {
            LLVMType fieldType = mapType(field.type().getText());
            String fieldName = field.ID().getText();

            fields.add(fieldType.getLlvmName());
            fieldInfos.put(fieldName, new StructFieldLLVMInfo(fieldType, index));
            index++;
        }

        structFields.put(className, fieldInfos);
        emit("%struct." + className + " = type { " + fields + " }");

        for (ExprParser.ClassMethodDeclarationContext method : ctx.classMethodDeclaration()) {
            String methodKey = className + "." + method.ID().getText();
            LLVMType returnType = mapType(method.type().getText());
            List<LLVMType> parameterTypes = new ArrayList<>();

            if (method.parameterList() != null) {
                for (ExprParser.ParameterContext parameter : method.parameterList().parameter()) {
                    parameterTypes.add(mapType(parameter.type().getText()));
                }
            }

            functionReturnTypes.put(methodKey, returnType);
            functionParameterTypes.put(methodKey, parameterTypes);
        }
        return null;
    }

    public LLVMValue visitStructVariableDeclaration(ExprParser.StructVariableDeclarationContext ctx) {
        String structName = ctx.ID().get(0).getText();
        String variableName = ctx.ID().get(1).getText();

        StructVariableInfo info = declareStructVariable(variableName, structName);
        emit("  " + info.getLlvmAddress()
                + " = alloca " + info.getLlvmType());
        return null;
    }

    public LLVMValue visitStructDeclaration(ExprParser.StructDeclarationContext ctx){
        String structName = ctx.ID().getText();
        StringJoiner fields = new  StringJoiner(", ");
        Map<String,StructFieldLLVMInfo> fieldInfos = new HashMap<>();

        int index = 0;

        for(ExprParser.StructFieldContext field : ctx.structField()){
            LLVMType fieldType = mapType(field.type().getText());
            String fieldName = field.ID().getText();

            fields.add(fieldType.getLlvmName());
            fieldInfos.put(fieldName,new StructFieldLLVMInfo(fieldType,index));
            index++;
        }

        structFields.put(structName,fieldInfos);
        emit("%struct." + structName + " = type { " + fields + " }");
        return null;
    }

    public LLVMValue visitGlobalDeclaration(ExprParser.GlobalDeclarationContext ctx) {
        String variableName = ctx.ID().getText();
        LLVMType type = mapType(ctx.type().getText());


        boolean isArray = ctx.INT_LITERAL() != null;
        if(isArray){
            int size = Integer.parseInt(ctx.INT_LITERAL().getText());
            ArrayInfo arrayInfo = declareGlobalArray(variableName,type,size);

            emit(arrayInfo.getLlvmAddress()
                    + " = global "
                    + arrayInfo.getLlvmArrayType()
                    + " zeroinitializer");
            return null;
        }

        declareGlobalVariable(variableName, type);

        String initialValue = "0";

        if (ctx.literal() != null) {
            initialValue = ctx.literal().getText();

            if (type == LLVMType.DOUBLE
                    && ctx.literal().INT_LITERAL() != null) {
                initialValue += ".0";
            }
        } else if (type == LLVMType.DOUBLE) {
            initialValue = "0.0";
        }
        emit("@" + variableName
                + " = global " + type.getLlvmName()
                + " " + initialValue);
        return null;
    }

    public LLVMValue visitFunctionDeclaration(ExprParser.FunctionDeclarationContext ctx) {
        String functionName = ctx.ID().getText();
        LLVMType returnType = mapType(ctx.type().getText());
        functionReturnTypes.put(functionName, returnType);

        List<ExprParser.ParameterContext> parameters = ctx.parameterList() == null
                ? List.of()
                : ctx.parameterList().parameter();

        List<LLVMType> parameterTypes = new ArrayList<>();

        StringJoiner joiner = new StringJoiner(", ");
        for(ExprParser.ParameterContext parameter: parameters){
            LLVMType type = mapType(parameter.type().getText());
            String parameterName = parameter.ID().getText();

            parameterTypes.add(type);
            joiner.add(type.getLlvmName() + " %arg." + parameterName);
        }

        functionParameterTypes.put(functionName, parameterTypes);

        emit("define " + returnType.getLlvmName()
                + " @" + functionName + "(" + joiner + ") {");
        emit("entry:");

        enterScope();

        try{
            for(ExprParser.ParameterContext parameter: parameters){
                LLVMType type = mapType(parameter.type().getText());
                String parameterName = parameter.ID().getText();

                VariableInfo variableInfo = declareVariable(parameterName, type);
                emit("  " + variableInfo.getLlvmAddress()
                        + " = alloca " + type.getLlvmName());

                emit("  store " + type.getLlvmName()
                        + " %arg." + parameterName
                        + ", " + type.getLlvmName()
                        + "* " + variableInfo.getLlvmAddress());
            }
            for (ExprParser.StatementContext statement : ctx.statement()) {
                visit(statement);
            }
            LLVMValue returnValue = visit(ctx.expression());
            if (returnType == LLVMType.DOUBLE && returnValue.getType() == LLVMType.I32) {
                returnValue = convertIntToDouble(returnValue);
            }

            emit("  ret " + returnType.getLlvmName()
                    + " " + returnValue.getValue());
        }finally{
            exitScope();
        }

        emit("}");
        emit("");

        return null;
    }

    public LLVMValue visitFunctionCall(ExprParser.FunctionCallContext ctx) {
        String functionName = ctx.ID().getText();
        LLVMType returnType = functionReturnTypes.get(functionName);

        List<ExprParser.ExpressionContext> arguments = ctx.argumentList() == null
                ? List.of()
                : ctx.argumentList().expression();

        List<LLVMType> expectedTypes = functionParameterTypes.get(functionName);
        StringJoiner llvmArguments = new StringJoiner(", ");

        for(int i = 0; i < arguments.size(); i++){
            LLVMValue value = visit(arguments.get(i));
            LLVMType expectedType = expectedTypes.get(i);

            if(expectedType == LLVMType.DOUBLE && value.getType() == LLVMType.I32){
                value = convertIntToDouble(value);
            }
            llvmArguments.add(expectedType.getLlvmName() + " " + value.getValue());

        }

        String register = newRegister();

        emit("  " + register
                + " = call " + returnType.getLlvmName()
                + " @" + functionName
                + "(" + llvmArguments + ")");

        return new LLVMValue(register,returnType);
    }

    public LLVMValue visitWhileStatement(ExprParser.WhileStatementContext ctx) {
        String conditionLabel = newLabel("while.condition");
        String bodyLabel = newLabel("while.body");
        String endLabel = newLabel("while.end");

        emit("  br label %" + conditionLabel);

        emit(conditionLabel + ":");

        LLVMValue condition = visit(ctx.expression());

        emit("  br i1 " + condition.getValue()
                + ", label %" + bodyLabel
                + ", label %" + endLabel);

        emit(bodyLabel + ":");
        visit(ctx.block());
        emit("  br label %" + conditionLabel);

        emit(endLabel + ":");
        return null;
    }

    public LLVMValue visitIfStatement(ExprParser.IfStatementContext ctx) {
        LLVMValue condition = visit(ctx.expression());

        String thenLabel = newLabel("if.then");
        String endLabel = newLabel("if.end");

        boolean hasElse = ctx.block().size() == 2;

        if(hasElse){
            String elseLabel = newLabel("if.else");

            emit("  br i1 " + condition.getValue()
                    + ", label %" + thenLabel
                    + ", label %" + elseLabel);

            emit(thenLabel + ":");
            visit(ctx.block(0));
            emit("  br label %" + endLabel);

            emit(elseLabel + ":");
            visit(ctx.block(1));
            emit("  br label %" + endLabel);
        }else {
            emit("  br i1 " + condition.getValue()
                    + ", label %" + thenLabel
                    + ", label %" + endLabel);

            emit(thenLabel + ":");
            visit(ctx.block(0));
            emit("  br label %" + endLabel);
        }

        emit(endLabel + ":");
        return null;
    }

    public LLVMValue visitProgram(ExprParser.ProgramContext ctx) {
        for(ExprParser.StructDeclarationContext struct : ctx.structDeclaration()){
            visit(struct);
        }
        for(ExprParser.ClassDeclarationContext classes : ctx.classDeclaration()){
            visit(classes);
        }

        if(!ctx.structDeclaration().isEmpty() || !ctx.classDeclaration().isEmpty()){
            emit("");
        }

        emit("@.int_format = private constant [4 x i8] c\"%d\\0A\\00\"");
        emit("@.real_format = private constant [4 x i8] c\"%f\\0A\\00\"");

        emit("@.int_read_format = private constant [3 x i8] c\"%d\\00\"");
        emit("@.real_read_format = private constant [4 x i8] c\"%lf\\00\"");

        emit("");
        emit("declare i32 @printf(i8*, ...)");
        emit("declare i32 @scanf(i8*, ...)");
        emit("");

        for (ExprParser.ClassDeclarationContext classDeclaration : ctx.classDeclaration()) {
            String className = classDeclaration.ID().getText();

            for (ExprParser.ClassMethodDeclarationContext method : classDeclaration.classMethodDeclaration()) {
                generateClassMethod(className, method);
            }
        }

        for (ExprParser.GlobalDeclarationContext global : ctx.globalDeclaration()) {
            visit(global);
        }
        emit("");

        for (ExprParser.FunctionDeclarationContext function : ctx.functionDeclaration()) {
            visit(function);
        }
        emit("define i32 @main() {");
        emit("entry:");

        for(ExprParser.StatementContext statementContext : ctx.statement()) {
            visit(statementContext);
        }
        emit("  ret i32 0");
        emit("}");

        return null;
    }

    public LLVMValue visitDeclaration(ExprParser.DeclarationContext ctx) {
        String variableName = ctx.ID().getText();
        LLVMType type =  mapType(ctx.type().getText());

        boolean isArray = ctx.INT_LITERAL() != null;

        if(isArray) {
            int size = Integer.parseInt(ctx.INT_LITERAL().getText());

            ArrayInfo arrayInfo = declareArray(variableName,type,size);
            emit("  " + arrayInfo.getLlvmAddress() + " = alloca "
                    + arrayInfo.getLlvmArrayType());
            return null;
        }

        var variableInfo = declareVariable(variableName,type);
        emit("  " + variableInfo.getLlvmAddress()
                + " = alloca " + type.getLlvmName());

        if(ctx.expression()!=null) {
            LLVMValue value = visit(ctx.expression());

            if (type == LLVMType.DOUBLE && value.getType() == LLVMType.I32) {
                value = convertIntToDouble(value);
            }

            emit("  store " + type.getLlvmName() + " " + value.getValue()
                    + ", " + type.getLlvmName() + "* " + variableInfo.getLlvmAddress());
        }
        return null;
    }

    public LLVMValue visitAssignment(ExprParser.AssignmentContext ctx) {
        if(ctx.fieldAccess() !=null) {
            LLVMValue pointer = generateFieldPointer(ctx.fieldAccess());
            LLVMValue value = visit(ctx.expression(0));

            if(pointer.getType() == LLVMType.DOUBLE && value.getType() == LLVMType.I32) {
                value = convertIntToDouble(value);
            }
            emit("  store "
                    + pointer.getType().getLlvmName()
                    + " "
                    + value.getValue()
                    + ", "
                    + pointer.getType().getLlvmName()
                    + "* "
                    + pointer.getValue());

            return null;
        }

        String variableName = ctx.ID().getText();
        boolean isArray = ctx.expression().size() == 2;

        if(isArray) {
            LLVMValue index = visit(ctx.expression(0));
            LLVMValue value = visit(ctx.expression(1));

            ArrayInfo arrayInfo = findArrayInfo(variableName);
            if(arrayInfo.getElementType() == LLVMType.DOUBLE && value.getType() == LLVMType.I32) {
                value = convertIntToDouble(value);
            }
            String pointer = newRegister();

            emit("  " + pointer + " = getelementptr "
                    + arrayInfo.getLlvmArrayType()
                    + ", "
                    + arrayInfo.getLlvmArrayType()
                    + "* "
                    + arrayInfo.getLlvmAddress()
                    + ", i32 0, i32 "
                    + index.getValue());

            emit("  store "
                    + arrayInfo.getElementType().getLlvmName()
                    + " "
                    + value.getValue()
                    + ", "
                    + arrayInfo.getElementType().getLlvmName()
                    + "* "
                    + pointer);
            return null;
        }

        VariableInfo variableInfo = findVariableInfo(variableName);
        LLVMType type = variableInfo.getType();

        LLVMValue value = visit(ctx.expression(0));

        if (type == LLVMType.DOUBLE && value.getType() == LLVMType.I32) {
            value = convertIntToDouble(value);
        }

        emit("  store " + type.getLlvmName() + " " + value.getValue()
                + ", " + type.getLlvmName() + "* " + variableInfo.getLlvmAddress());
        return null;
    }

    public LLVMValue visitShowStatement(ExprParser.ShowStatementContext ctx) {
        ExprParser.ExpressionContext expression = ctx.expression();

        if (expression.ID() != null && expression.expression().isEmpty()) {
            String variableName = expression.ID().getText();

            ArrayInfo arrayInfo = findArrayInfo(variableName);
            if (arrayInfo != null) {
                printArray(variableName);
                return null;
            }
        }

        LLVMValue value = visit(expression);

        if (value.getType() == LLVMType.I32) {
            emit("  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 "
                    + value.getValue() + ")");
        } else {
            emit("  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.real_format, i32 0, i32 0), double "
                    + value.getValue() + ")");
        }

        return null;
    }

    public LLVMValue visitInputStatement(ExprParser.InputStatementContext ctx) {
        ExprParser.ExpressionContext expression = ctx.expression();

        if(expression.fieldAccess() != null) {
            LLVMValue pointer = generateFieldPointer(expression.fieldAccess());

            if(pointer.getType() == LLVMType.I32) {
                emit("  call i32 (i8*, ...) @scanf("
                        + "i8* getelementptr ([3 x i8], [3 x i8]* @.int_read_format, i32 0, i32 0), "
                        + "i32* " + pointer.getValue() + ")");
            }else {
                emit("  call i32 (i8*, ...) @scanf("
                        + "i8* getelementptr ([4 x i8], [4 x i8]* @.real_read_format, i32 0, i32 0), "
                        + "double* " + pointer.getValue() + ")");
            }

            return null;
        }

        if(expression.ID() != null) {
            String variableName = expression.ID().getText();
            boolean isArrayAccess = !expression.expression().isEmpty();

            if (isArrayAccess) {
                LLVMValue index = visit(expression.expression(0));
                ArrayInfo arrayInfo = findArrayInfo(variableName);
                String pointer = newRegister();

                emit("  " + pointer + " = getelementptr "
                        + arrayInfo.getLlvmArrayType()
                        + ", "
                        + arrayInfo.getLlvmArrayType()
                        + "* "
                        + arrayInfo.getLlvmAddress()
                        + ", i32 0, i32 "
                        + index.getValue());

                if (arrayInfo.getElementType().getLlvmName().equals("i32")) {
                    emit("  call i32 (i8*, ...) @scanf("
                            + "i8* getelementptr ([3 x i8], [3 x i8]* @.int_read_format, i32 0, i32 0), "
                            + "i32* "
                            + pointer
                            + ")");
                } else {
                    emit("  call i32 (i8*, ...) @scanf("
                            + "i8* getelementptr ([4 x i8], [4 x i8]* @.real_read_format, i32 0, i32 0), "
                            + "double* "
                            + pointer
                            + ")");
                }
                return null;
            }

            VariableInfo variableInfo = findVariableInfo(variableName);
            LLVMType type = variableInfo.getType();
            String llvmAddress = variableInfo.getLlvmAddress();

            if (type == LLVMType.I32) {
                emit("  call i32 (i8*, ...) @scanf(i8* getelementptr ([3 x i8], [3 x i8]* @.int_read_format, i32 0, i32 0), i32* "
                        + llvmAddress + ")");
            } else {
                emit("  call i32 (i8*, ...) @scanf(i8* getelementptr ([4 x i8], [4 x i8]* @.real_read_format, i32 0, i32 0), double* "
                        + llvmAddress + ")");
            }
        }
        return null;
    }

    public LLVMValue visitExpression(ExprParser.ExpressionContext ctx) {
        if(ctx.INT_LITERAL() != null) {
            return new LLVMValue(ctx.INT_LITERAL().getText(),LLVMType.I32);
        }

        if(ctx.REAL_LITERAL() != null) {
            return new LLVMValue(ctx.REAL_LITERAL().getText(),LLVMType.DOUBLE);
        }
        if(ctx.methodCall() != null) {
            return visitMethodCall(ctx.methodCall());
        }
        if(ctx.functionCall() != null){
            return visit(ctx.functionCall());
        }

        if(ctx.fieldAccess() != null){
            LLVMValue pointer = generateFieldPointer(ctx.fieldAccess());
            String value = newRegister();

            emit("  " + value
                    + " = load "
                    + pointer.getType().getLlvmName()
                    + ", "
                    + pointer.getType().getLlvmName()
                    + "* "
                    + pointer.getValue());
            return new LLVMValue(value, pointer.getType());
        }

        if(ctx.ID()!=null) {
            String variableName = ctx.ID().getText();

            boolean isArrayAccess = !ctx.expression().isEmpty();
            if(isArrayAccess) {
                LLVMValue index = visit(ctx.expression(0));
                ArrayInfo arrayInfo = findArrayInfo(variableName);

                String pointer = newRegister();
                String value = newRegister();

                emit("  " + pointer + " = getelementptr "
                        + arrayInfo.getLlvmArrayType()
                        + ", "
                        + arrayInfo.getLlvmArrayType()
                        + "* "
                        + arrayInfo.getLlvmAddress()
                        + ", i32 0, i32 "
                        + index.getValue());
                emit("  " + value + " = load "
                        + arrayInfo.getElementType().getLlvmName()
                        + ", "
                        + arrayInfo.getElementType().getLlvmName()
                        + "* "
                        + pointer);
                return new LLVMValue(value,arrayInfo.getElementType());
            }

            VariableInfo variableInfo = findVariableInfo(variableName);

            if(variableInfo == null) {
                throw new RuntimeException("Variable not found: " + variableName);
            }

            LLVMType type = variableInfo.getType();
            String llvmAdress = variableInfo.getLlvmAddress();

            if(type == null) {
                throw new RuntimeException("Variable not found " +variableName);
            }
            String register = newRegister();
            emit("  " + register + " = load " + type.getLlvmName()
                    + ", " + type.getLlvmName() + "* " + llvmAdress);
            return new LLVMValue(register, type);
        }
        if(ctx.expression().size()==1) {
            return visit(ctx.expression().get(0));
        }

        LLVMValue left =  visit(ctx.expression().get(0));
        LLVMValue right = visit(ctx.expression().get(1));

       String operator = ctx.op.getText();
       if(isComparisonOperator(operator)) {
            return generateComparison(operator,left,right);
       }

        LLVMType resultType = left.getType() == LLVMType.DOUBLE || right.getType() == LLVMType.DOUBLE
                ? LLVMType.DOUBLE : LLVMType.I32;

        if(resultType == LLVMType.DOUBLE) {
            if(left.getType() == LLVMType.I32) {
                left = convertIntToDouble(left);
            }
            if(right.getType() == LLVMType.I32) {
                right = convertIntToDouble(right);
            }
        }
        String register = newRegister();

        String llvmInstructions = getInstruction(operator,resultType);
        emit(" " + register + " = " + llvmInstructions + " " + resultType.getLlvmName() + " "
                + left.getValue() + ", " + right.getValue());

        return new LLVMValue(register,resultType);
    }

    public LLVMValue visitBlock(ExprParser.BlockContext ctx) {
        enterScope();

        try{
            for(ExprParser.StatementContext statement : ctx.statement()) {
                visit(statement);
            }
        }finally {
            exitScope();
        }
        return null;
    }

    private void generateClassMethod(String className, ExprParser.ClassMethodDeclarationContext ctx) {
        String methodName = ctx.ID().getText();
        String methodKey = className + "." + methodName;
        LLVMType returnType = functionReturnTypes.get(methodKey);

        List<ExprParser.ParameterContext> parameters = ctx.parameterList() == null ? List.of() : ctx.parameterList().parameter();

        StringJoiner arguments = new StringJoiner(", ");
        arguments.add("%struct." + className + "* %this");

        for (ExprParser.ParameterContext parameter : parameters) {
            LLVMType type = mapType(parameter.type().getText());

            arguments.add(type.getLlvmName() + " %arg." + parameter.ID().getText());
        }

        emit("define " + returnType.getLlvmName()
                + " @" + methodKey
                + "(" + arguments + ") {");
        emit("entry:");

        enterScope();

        try {
            scopes.peek().structVariableInfo.put("this", new StructVariableInfo(className, "this"));

            for (ExprParser.ParameterContext parameter : parameters) {
                String parameterName = parameter.ID().getText();
                LLVMType type = mapType(parameter.type().getText());
                VariableInfo info = declareVariable(parameterName, type);

                emit("  " + info.getLlvmAddress()
                        + " = alloca " + type.getLlvmName());

                emit("  store " + type.getLlvmName()
                        + " %arg." + parameterName
                        + ", " + type.getLlvmName()
                        + "* " + info.getLlvmAddress());
            }

            for (ExprParser.StatementContext statement : ctx.statement()) {
                visit(statement);
            }

            LLVMValue value = visit(ctx.expression());

            if (returnType == LLVMType.DOUBLE && value.getType() == LLVMType.I32) {
                value = convertIntToDouble(value);
            }

            emit("  ret " + returnType.getLlvmName()
                    + " " + value.getValue());
        } finally {
            exitScope();
        }

        emit("}");
        emit("");
    }

    private String getInstruction(String operator,LLVMType type) {
        if(type == LLVMType.DOUBLE) {
            return switch (operator) {
                case "+" -> "fadd";
                case "-" -> "fsub";
                case "*" -> "fmul";
                case "/" -> "fdiv";
                default -> throw new RuntimeException("Unknown operator" + operator);
            };
        }
            return switch (operator) {
                case "+" -> "add";
                case "-" -> "sub";
                case "*" -> "mul";
                case "/" -> "sdiv";
                default -> throw new RuntimeException("Unknown operator" + operator);
            };
    }

    private LLVMType mapType(String typeName) {
        return switch (typeName) {
            case "int" -> LLVMType.I32;
            case "real" -> LLVMType.DOUBLE;
            default -> throw new RuntimeException("Unknown type " + typeName);
        };
    }

    private LLVMValue convertIntToDouble(LLVMValue value) {
        String register = newRegister();
        emit("  " + register + " = sitofp i32 " + value.getValue() + " to double");
        return new LLVMValue(register, LLVMType.DOUBLE);
    }

    private boolean isComparisonOperator(String operator) {
        return operator.equals("<")
                || operator.equals(">")
                || operator.equals("<=")
                || operator.equals(">=")
                || operator.equals("==")
                || operator.equals("!=");
    }

    private String getComparisonInstruction(String operator, boolean useDouble) {
        if(useDouble) {
            return switch (operator) {
                case "<" -> "fcmp olt";
                case ">" -> "fcmp ogt";
                case "<=" -> "fcmp ole";
                case ">=" -> "fcmp oge";
                case "==" -> "fcmp oeq";
                case "!=" -> "fcmp one";
                default -> throw new RuntimeException("Unknown operator" + operator);
            };
        }

        return switch(operator){
            case "<" -> "icmp slt";
            case ">" -> "icmp sgt";
            case "<=" -> "icmp sle";
            case ">=" -> "icmp sge";
            case "==" -> "icmp eq";
            case "!=" -> "icmp ne";
            default -> throw new RuntimeException("Unknown operator" + operator);
        };
    }

    private LLVMValue generateComparison(String operator,LLVMValue left,LLVMValue right) {

        boolean useDouble = left.getType() == LLVMType.DOUBLE || right.getType() == LLVMType.DOUBLE;
        if(useDouble) {
            if(left.getType() == LLVMType.I32) {
                left = convertIntToDouble(left);
            }
            if(right.getType() == LLVMType.I32) {
                right = convertIntToDouble(right);
            }
        }

        String register = newRegister();
        String instruction = getComparisonInstruction(operator, useDouble);
        emit("  " + register + " = " + instruction + " "
                + (useDouble ? "double" : "i32") + " "
                + left.getValue() + ", " + right.getValue());
        return new LLVMValue(register, LLVMType.I1);
    }

    private void printArray(String variableName) {
        ArrayInfo arrayInfo = findArrayInfo(variableName);
        for (int i = 0; i < arrayInfo.getSize(); i++) {

            String pointer = newRegister();
            String value = newRegister();

            emit("  " + pointer + " = getelementptr "
                    + arrayInfo.getLlvmArrayType()
                    + ", "
                    + arrayInfo.getLlvmArrayType()
                    + "* "
                    + arrayInfo.getLlvmAddress()
                    + ", i32 0, i32 "
                    + i);

            emit("  " + value + " = load "
                    + arrayInfo.getElementType().getLlvmName()
                    + ", "
                    + arrayInfo.getElementType().getLlvmName()
                    + "* "
                    + pointer);
            if (arrayInfo.getElementType() == LLVMType.I32) {
                emit("  call i32 (i8*, ...) @printf("
                        + "i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), "
                        + "i32 "
                        + value
                        + ")");
            }else {
                emit("  call i32 (i8*, ...) @printf("
                        + "i8* getelementptr ([4 x i8], [4 x i8]* @.real_format, i32 0, i32 0), "
                        + "double "
                        + value
                        + ")");
            }
        }
    }

}