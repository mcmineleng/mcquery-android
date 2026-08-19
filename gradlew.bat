@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle  Windows 启动脚本
@rem
@rem ##########################################################################

@rem 设置本地变量，使用 Windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem 通常不使用
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem 解析 APP_HOME 中的 "." 和 ".." 以缩短路径
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem 在此处添加默认 JVM 选项。也可以通过 JAVA_OPTS 和 GRADLE_OPTS 传递 JVM 参数
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem 查找 java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo 错误：未设置 JAVA_HOME，且在 PATH 中未找到 'java' 命令。
echo.
echo 请在环境中设置 JAVA_HOME 变量，以匹配
echo Java 安装的位置。

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo 错误：JAVA_HOME 设置为无效目录：%JAVA_HOME%
echo.
echo 请在环境中设置 JAVA_HOME 变量，以匹配
echo Java 安装的位置。

goto fail

:execute
@rem 设置命令行

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar


@rem 执行 Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem 结束本地变量作用域（Windows NT shell）
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem 如果需要 _script_ 返回值而非 _cmd.exe /c_ 返回值，设置变量 GRADLE_EXIT_CONSOLE
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega