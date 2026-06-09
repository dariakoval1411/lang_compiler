package com.daria;
import com.daria.codegen.LLVMGeneratorVisitor;
import com.daria.semantic.SemanticException;
import com.daria.semantic.SemanticVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main
{
    public static void main( String[] args ) throws IOException {

        if(args.length == 0) {
            System.out.println("Usage: nexa <file.nex>");
            return;
        }

        String sourceFile = args[0];
        String outputFile;

        int dotIndex = sourceFile.lastIndexOf('.');
        if(dotIndex != -1) {
            outputFile = sourceFile.substring(0, dotIndex) + ".ll";
        }else {
            outputFile = sourceFile + ".ll";
        }
        Files.deleteIfExists(Paths.get(outputFile));

        var lexer = new com.daria.parser.ExprLexer(CharStreams.fromFileName(sourceFile));

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        var parser = new com.daria.parser.ExprParser(tokens);
        ParseTree parseTree = parser.program();
        if(parser.getNumberOfSyntaxErrors() > 0) {
            System.exit(1);
        }
        SemanticVisitor semanticVisitor = new SemanticVisitor();
        try {
            semanticVisitor.visit(parseTree);
        }catch(SemanticException e) {
            System.out.println("Semantic Error: " + e.getMessage());
            System.exit(1);
        }

        LLVMGeneratorVisitor llvmGeneratorVisitor = new LLVMGeneratorVisitor();
        try {
            llvmGeneratorVisitor.visit(parseTree);
        }catch(Exception e) {
            System.out.println("LLVM Error: " + e.getMessage());
        }
        String llvCode = llvmGeneratorVisitor.getLLMVCode();
        Files.writeString(Paths.get(outputFile), llvCode);
    }
}
