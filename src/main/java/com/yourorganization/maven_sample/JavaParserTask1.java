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

public class JavaParserTask1 {
  private static CombinedTypeSolver combinedSolver;
  private static final String src_dir = "src/main/resources/avro/lang/java/";
  private static final String file_name_1 = "avro/src/test/java/org/apache/avro/io/parsing/TestResolvingGrammarGenerator.java";
  private static final String file_name_2 = "avro/src/test/java/org/apache/avro/TestDataFileMeta.java";
  private static final String file_name_3 = "ipc/src/test/java/org/apache/avro/TestProtocolGeneric.java";
  private static final String jar_dir = "C:/Users/small/.m2/repository";

  private void initCombinedSolver() throws IOException {
    CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
    TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();

    // get src root
    final ProjectRoot projectRoot = new SymbolSolverCollectionStrategy().collect(Paths.get(src_dir));
    List<SourceRoot> sourceRootList = projectRoot.getSourceRoots();
    System.out.println(sourceRootList.size());

    // add java jre solver
    combinedSolver.add(reflectionTypeSolver);
    // add src roots into combined solver
    for (SourceRoot sourceRoot : sourceRootList) {
      combinedSolver.add(new JavaParserTypeSolver(sourceRoot.getRoot()));
    }
    // get all the jar files and add jre solver
    List<File> jar_files = getJarFiles(new File(jar_dir));
    for (File f : jar_files) {
      combinedSolver.add(new JarTypeSolver(f));
    }
/*    if (jar_dir != null) {
      for (File jar_File : new File(jar_dir).listFiles()) {
        combinedSolver.add(new JarTypeSolver(jar_File));
      }
    }*/
    this.combinedSolver = combinedSolver;
  }

  // Recursively collect all jar files
  private static List<File> getJarFiles(File f) {
    List<File> list = new ArrayList<>();
    for (File nextFile : f.listFiles()) {
      //System.out.println(nextFile.getName());
      if (nextFile.isFile() && nextFile.getName().endsWith("jar")) {
        list.add(nextFile.getAbsoluteFile());
        //System.out.println(nextFile.getName());
      }
      else if (nextFile.isDirectory()) list.addAll(getJarFiles(nextFile));
    }
    return list;
  }

/*  private static class MethodNameCollector extends VoidVisitorAdapter<List<String>> {
    @Override
    public void visit(MethodDeclaration md, List<String> collector) {
      super.visit(md, collector);
      collector.add(md.getNameAsString());
    }
  }*/

  private static class MethodDeclarationCollector extends VoidVisitorAdapter<List<MethodDeclaration>> {
    @Override
    public void visit(MethodDeclaration md, List<MethodDeclaration> collector) {
      super.visit(md, collector);
      collector.add(md);
    }
  }

  private static class AssertCollector extends VoidVisitorAdapter<List<String>> {
    @Override
    public void visit(AssertStmt as, List<String> collector) {
      super.visit(as, collector);
      collector.add(as.toString());
    }
  }

  private static class MethodCallExpCollector extends VoidVisitorAdapter<List<MethodCallExpr>> {
    @Override
    public void visit(MethodCallExpr mce, List<MethodCallExpr> collector) {
      super.visit(mce, collector);
      //System.out.println(mce);
      collector.add(mce);
    }
  }
  private static class MethodCallExpressionVisitor extends VoidVisitorAdapter<Void> {
    @Override
    public void visit(MethodCallExpr mce, Void arg) {
      System.out.println("======================");
      System.out.println("Method Call Expression: \n" + mce);
      try {
        mce.resolve();
      } catch (Exception e) {
        System.err.println(e);
      }

    }
  }

  public static void main(String[] args) throws IOException {
    // JavaSymbolSolver configuration
    JavaParserTask1 jp = new JavaParserTask1();
    jp.initCombinedSolver();
    ParserConfiguration parserConfiguration = new ParserConfiguration();
    JavaParser jvp = new JavaParser(parserConfiguration);
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
    jvp.getParserConfiguration().setSymbolResolver(symbolSolver);

    CompilationUnit cu1 = jvp.parse(new File(src_dir + file_name_1)).getResult().get();
    CompilationUnit cu2 = jvp.parse(new File(src_dir + file_name_2)).getResult().get();
    CompilationUnit cu3 = jvp.parse(new File(src_dir + file_name_3)).getResult().get();



    List<MethodDeclaration> methodModules = new ArrayList<>();
    VoidVisitor<List<MethodDeclaration>> MethodDeclarationCollector = new MethodDeclarationCollector();
    VoidVisitor<List<MethodCallExpr>> MethodCallExpCollector = new MethodCallExpCollector();
    VoidVisitor<List<String>> AssertCollector = new AssertCollector();
    MethodDeclarationCollector.visit(cu1, methodModules);

    VoidVisitor<Void> MethodCallExpressionVisitor = new MethodCallExpressionVisitor();
    List<String> res = new ArrayList<>();
    List<MethodCallExpr> assertList = new ArrayList<>();
    List<MethodCallExpr> finalMethodList = new ArrayList<>();
    //NodeList<Expression> expressionList = new NodeList<>();
    for (MethodDeclaration md : methodModules) {

      if (md.isAnnotationPresent("Test")) {
/*        res.add(md.getNameAsString());
        md.accept(AssertCollector, assertList); */
        //System.out.println(md);
        MethodCallExpCollector.visit(md, assertList);
        //md.accept(MethodCallExpCollector, assertList);
        for (MethodCallExpr mce : assertList) {
          System.out.println(mce);
          if (mce.getNameAsString().contains("assert")) {
            //System.out.println(mce);
            for (Expression e : mce.getArguments()) {
              //List<MethodCallExpr> finalMethodList = new ArrayList<>();
              e.accept(MethodCallExpCollector, finalMethodList);

            }
          }
        }
      }
    }
/*    res.forEach(n -> System.out.println("Method Name Collected: " + n));
    assertList.forEach(n -> System.out.println("Method Name Collected: " + n));*/
    //finalMethodList.forEach(n -> System.out.println(n.getName().));
    for (MethodCallExpr n : finalMethodList) {
      //System.out.println(n);
      //System.out.println(n.resolve().getQualifiedName());
    }
    //finalMethodList.forEach(n -> System.out.println("Method Name Collected: " + n.resolve().getQualifiedName()));

  }
}
