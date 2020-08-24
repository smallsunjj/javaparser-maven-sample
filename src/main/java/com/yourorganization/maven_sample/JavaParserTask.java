package com.yourorganization.maven_sample;//package main.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JavaParserTask {
  private static CombinedTypeSolver combinedSolver;
  private static final String src_dir = "src/main/resources/avro/lang/java/";
  private static final String file_name_1 = "avro/src/test/java/org/apache/avro/io/parsing/TestResolvingGrammarGenerator.java";
  private static final String file_name_2 = "avro/src/test/java/org/apache/avro/TestDataFileMeta.java";
  private static final String file_name_3 = "ipc/src/test/java/org/apache/avro/TestProtocolGeneric.java";
  private static final String jar_dir = "C:/Users/small/.m2/repository";

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

/*  private static class MethodCallExprCollector extends VoidVisitorAdapter<List<MethodCallExpr>> {
    @Override
    public void visit(MethodCallExpr mce, List<MethodCallExpr> collector) {
      System.out.println("======================");
      System.out.println("Method Call Expression: \n" + mce);
      try {
       // mce.resolve();
        collector.add(mce.get)
      } catch (Exception e) {
        System.err.println(e);
      }

    }
  }*/

  private static List<MethodCallExpr> getMethodDeclarationWithTest(CompilationUnit cu) {
    List<MethodCallExpr> result = new ArrayList<>();
    return result;
  }

  public static void main(String[] args) throws IOException {
    // JavaSymbolSolver configuration
    initCombinedSolver();
    ParserConfiguration parserConfiguration = new ParserConfiguration();
    JavaParser jvp = new JavaParser(parserConfiguration);
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
    jvp.getParserConfiguration().setSymbolResolver(symbolSolver);

    CompilationUnit cu1 = jvp.parse(new File(src_dir + file_name_1)).getResult().get();
    CompilationUnit cu2 = jvp.parse(new File(src_dir + file_name_2)).getResult().get();
    CompilationUnit cu3 = jvp.parse(new File(src_dir + file_name_3)).getResult().get();
    // To-do: task2...
    VoidVisitor<List<MethodDeclaration>> MethodDeclarationCollector = new MethodDeclarationCollector();
    List<MethodDeclaration> mdc1 = new ArrayList<>();
    MethodDeclarationCollector.visit(cu1, mdc1);
    List<MethodDeclaration> mdc2 = new ArrayList<>();
    MethodDeclarationCollector.visit(cu2, mdc2);
    List<MethodDeclaration> mdc3 = new ArrayList<>();
    MethodDeclarationCollector.visit(cu3, mdc3);
    System.out.println(file_name_1);
    mdc1.forEach(n -> System.out.println("Method name with @Test : " + n.resolve().getQualifiedName()));
    System.out.println(file_name_2);
    mdc2.forEach(n -> System.out.println("Method name with @Test : " + n.resolve().getQualifiedName()));
    System.out.println(file_name_3);
    mdc3.forEach(n -> System.out.println("Method name with @Test : " + n.resolve().getQualifiedName()));
  }
}
