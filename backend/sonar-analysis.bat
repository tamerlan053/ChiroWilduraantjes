@echo off
echo Running SonarQube analysis...
mvn sonar:sonar ^
  -Dsonar.projectKey=2425-REPO-AON00-develop-backend ^
  -Dsonar.projectName=2425-REPO-AON00-develop-backend ^
  -Dsonar.host.url=http://136.144.209.114 ^
  -Dsonar.login=sqp_88251041cfbff1eaf4d330cbdd459a371b9d878b ^
  -Dsonar.scm.exclusions.disabled=true ^
  -Dsonar.language=java
echo Done!
pause