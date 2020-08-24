JavaParser Task
---
Some problems and key steps for each task
### Task 1:
1. The project structure is based on [javaparser-maven-sample](https://github.com/javaparser/javaparser-maven-sample)
2. My result - JavaParserTask.java is under scr/main/java/com/yourorganization/maven_sample/
3. The Avro project is under src/main/resources
4. The major part I modified in the initCombinedSolver() function is adding the jar solver. Here is the major **problem**:  
According to my understanding, my local IDE with maven framework automatically downloads all the dependecies (jar files) to my local central repository. So the jar_dir is hard-coded, which means you need to update that dir to your jar location.

### Task 2:
1. Based on Task 1, MethodDeclarationCollector.class is implemented to collect all MethodDeclarations with @Test.
2. Call .resolve().getQualifiedName() to get qualified names.

### Task 3:
1. Based on Task 2, MethodCallExprCollector.class is implemented to collect all assert statements
2. In current branch, the assert statement is defined as:  
Scope equals to "Assert", e.g. Assert.xxx;  
Or method name contains "assert", e.g. xxx.assertYYY() or assertYYY()