@echo off
gradlew -q -Dfile.encoding=utf-8 run --args="%*" || (kotlinc src\main\kotlin -include-runtime -d diff.jar && java -Dfile.encoding=utf-8 -jar diff.jar %*)
