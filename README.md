From now on, use this order whenever you open a new PowerShell terminal:

$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12.1"
cd D:\MY_PROJECTS\EnterpriseProjectManagement\backend
mvn clean test

You don't need to run mvn clean test repeatedly right now—we've completed the environment verification.

In CMD, use this

For the current CMD window:

set JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1

Then verify:

echo %JAVA_HOME%

It should show:

C:\Program Files\Java\jdk-21.0.12.1

Then:

mvn -version

You should see:

Java version: 21.0.12.1
Then run your project

You are already here:

D:\MY_PROJECTS\EnterpriseProjectManagement\backend>

So run:

mvn clean test
mvn spring-boot:run

STRUCTURE:
tree /F src\main\java\com\enterprise\project_management




Docker:to start
docker compose up -d

to close:docker compose down