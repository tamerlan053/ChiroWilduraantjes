@echo off
echo Running SonarQube analysis for frontend...
sonar-scanner ^
  -Dsonar.projectKey=2425-REPO-AON00-develop-frontend ^
  -Dsonar.projectName=2425-REPO-AON00-develop-frontend ^
  -Dsonar.sources=. ^
  -Dsonar.host.url=http://136.144.209.114 ^
  -Dsonar.login=sqp_e1101520cba82efa09371d02b2d1b28a0af3c2b4 ^
  -Dsonar.scm.exclusions.disabled=true
echo Done!
pause