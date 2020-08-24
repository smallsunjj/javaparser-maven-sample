package com.yourorganization.maven_sample;//package main.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import javassist.expr.MethodCall;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JavaParserTask {
  private static CombinedTypeSolver combinedSolver;
  private static final String src_dir = "src/main/resources/avro/lang/java/";
  private static final String file_name_1 = "avro/src/test/java/org/apache/avro/io/parsing/TestResolvingGrammarGenerator.java";
  private static final String file_name_2 = "avro/src/test/java/org/apache/avro/TestDataFileMeta.java";
  private static final String file_name_3 = "ipc/src/test/java/org/apache/avro/TestProtocolGeneric.java";
  private static final String jar_dir = "C:/Users/small/.m2/repository";
  private static final String[] file_names = new String[] {file_name_1, file_name_2, file_name_3};
  private static void initCombinedSolver() throws IOException {
    combinedSolver = new CombinedTypeSolver();
    TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();

    // get src root
    final ProjectRoot projectRoot = new SymbolSolverCollectionStrategy().collect(Paths.get(src_dir));
    List<SourceRoot> sourceRootList = projectRoot.getSourceRoots();

    // add java jre solver
    combinedSolver.add(reflectionTypeSolver);

    // add src roots into combined solver
    for (SourceRoot sourceRoot : sourceRootList) {
      combinedSolver.add(new JavaParserTypeSolver(sourceRoot.getRoot()));
    }

    // get all the jar files and add jar solver
    List<File> jar_files = getJarFiles(new File(jar_dir));
    for (File f : jar_files) {
      combinedSolver.add(new JarTypeSolver(f));
    }
/*    if (jar_dir != null) {
      for (File jar_File : new File(jar_dir).listFiles()) {
        combinedSolver.add(new JarTypeSolver(jar_File));
      }
    }*/
    //this.combinedSolver = combinedSolver;
  }

  // Recursively collect all jar files
  private static List<File> getJarFiles(File f) {
    List<File> list = new ArrayList<>();
    for (File nextFile : f.listFiles()) {
      if (nextFile.isFile() && nextFile.getName().endsWith("jar")) list.add(nextFile.getAbsoluteFile());
      else if (nextFile.isDirectory()) list.addAll(getJarFiles(nextFile));
    }
    return list;
  }

  // Collect all MethodDeclarations with @Test
  private static class MethodDeclarationCollector extends VoidVisitorAdapter<List<MethodDeclaration>> {
    @Override
    public void visit(MethodDeclaration md, List<MethodDeclaration> collector) {
      try {
        if (md.isAnnotationPresent("Test")) collector.add(md);
      } catch (Exception e) {
        System.err.println(e);
      }
    }
  }

  // Collect all assert statements
  private static class MethodCallExprCollector extends VoidVisitorAdapter<List<MethodCallExpr>> {
    @Override
    public void visit(MethodCallExpr mce, List<MethodCallExpr> collector) {
      try {
        if (mce.getNameAsString().contains("assert") || (mce.getScope().isPresent() && mce.getScope().get().toString().endsWith("Assert"))) {
          collector.add(mce);
        }
      } catch (Exception e) {
        System.err.println(e);
      }
    }
  }

  // Recursively traverse on an Expression and print out all resolved method names
  private static void helper(Expression e) {
    if (e == null) return;
    else if (e.isMethodCallExpr()) {
      if (!e.asMethodCallExpr().getScope().isPresent()) return;
      System.out.println(e.asMethodCallExpr().resolve().getQualifiedName());
      helper(e.asMethodCallExpr().getScope().get());
    }
    else if (e.isEnclosedExpr()) helper(e.asEnclosedExpr().getInner());
    else if (e.isCastExpr()) {
      helper(e.asCastExpr().getExpression());
    }
  }

  private static List<CompilationUnit> getCompilationUnit(JavaParser jvp) throws FileNotFoundException {
    List<CompilationUnit> result = new ArrayList<>();
    for (String filename: file_names) {
      result.add(jvp.parse(new File(src_dir + filename)).getResult().get());
    }
    return result;
  }

  private static List<List<MethodDeclaration>> getMethodNameWithTest(List<CompilationUnit> cuList, VoidVisitor<List<MethodDeclaration>> MethodDeclarationCollector) {
    System.out.println("======================");
    System.out.println("Task2 begins:");
    List<List<MethodDeclaration>> result = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      List<MethodDeclaration> list = new ArrayList<>();
      MethodDeclarationCollector.visit(cuList.get(i), list);
      System.out.println("\nParsed file: " + file_names[i] + ":");
      list.forEach(n -> System.out.println(n.resolve().getQualifiedName()));
      result.add(new ArrayList<>(list));
    }
    return result;
  }

  private static List<List<MethodCallExpr>> getAssertMethod(List<List<MethodDeclaration>> methodWithTestList, VoidVisitor<List<MethodCallExpr>> MethodCallExprCollector) {
    System.out.println("======================");
    System.out.println("Task3 begins:");
    List<List<MethodCallExpr>> result = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      List<MethodCallExpr> assertList = new ArrayList<>();
      methodWithTestList.get(i).forEach(mdc -> MethodCallExprCollector.visit(mdc, assertList));
      System.out.println("\nParsed file: " + file_names[i] + ":");
      assertList.forEach(System.out::println);
      result.add(new ArrayList<>(assertList));
    }
    return result;
  }

  private static void getInvokedMethodName(List<List<MethodCallExpr>> assertMethoList) {
    System.out.println("======================");
    System.out.println("Task4 begins:");
    for (int i = 0; i < 3; i++) {
      System.out.println("\nParsed file: " + file_names[i] + ":");
      for (MethodCallExpr mce : assertMethoList.get(i)) {
        for (Expression e : mce.getArguments()) {
          helper(e);
        }
      }
    }
  }

  public static void main(String[] args) throws IOException {
    // JavaSymbolSolver configuration
    initCombinedSolver();
    ParserConfiguration parserConfiguration = new ParserConfiguration();
    JavaParser jvp = new JavaParser(parserConfiguration);
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
    jvp.getParserConfiguration().setSymbolResolver(symbolSolver);

    // task1
    List<CompilationUnit> cuList= getCompilationUnit(jvp);

    // task2
    VoidVisitor<List<MethodDeclaration>> MethodDeclarationCollector = new MethodDeclarationCollector();
    List<List<MethodDeclaration>> methodWithTestList = getMethodNameWithTest(cuList, MethodDeclarationCollector);
    System.out.println();

    // task3
    VoidVisitor<List<MethodCallExpr>> MethodCallExprCollector = new MethodCallExprCollector();
    List<List<MethodCallExpr>> assertMethodList =  getAssertMethod(methodWithTestList, MethodCallExprCollector);
    System.out.println();

    // task4
    getInvokedMethodName(assertMethodList);
  }
}
