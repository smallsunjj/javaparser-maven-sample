JavaParser Task
---
Some problems and key steps for each task
### Task 1:
1. The project structure uses [javaparser-maven-sample](https://github.com/javaparser/javaparser-maven-sample)
2. My result - JavaParserTask.java is under scr/main/java/com/yourorganization/maven_sample/
3. The Avro project is under src/main/resources
4. The major part I modified in the initCombinedSolver() function is adding the jar solver. Here is the major **problem**:  
According to my understanding, my local IDE with maven framework automatically downloads all the dependecies (jar files) to my local central repository. So the jar_dir is hard-coded, which means you need to update that dir to your jar location.
  
