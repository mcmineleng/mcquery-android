#!/bin/sh

#
# Copyright © 2015-2021 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
#
#   Gradle POSIX 启动脚本
#
#   重要运行说明：
#
#   (1) 你需要一个 POSIX 兼容的 shell 来运行此脚本。如果你的 /bin/sh
#       不兼容，但有其他兼容 shell 如 ksh 或 bash，则运行此脚本时，
#       在该 shell 名称后输入完整命令行，例如：
#
#           ksh Gradle
#
#       Busybox 及类似精简 shell 将无法工作，因为此脚本需要以下所有
#       POSIX shell 特性：
#         * 函数；
#         * 扩展 «$var», «${var}», «${var:-default}», «${var+SET}»,
#           «${var#prefix}», «${var%suffix}», 和 «$( cmd )»；
#         * 具有可测试退出状态的复合命令，特别是 «case»；
#         * 各种内置命令，包括 «command», «set», 和 «ulimit»。
#
#   重要修补说明：
#
#   (2) 此脚本针对任何 POSIX shell，因此避免使用 Bash、Ksh 等提供的扩展；
#       特别是避免使用数组。
#
#       "传统"做法是将多个参数打包成空格分隔的字符串，这是众所周知
#       的 bug 和安全问题来源，因此这里（大部分）通过逐步累积选项
#       到 "$@" 中，并最终传递给 Java 来避免。
#
#       当继承的环境变量（DEFAULT_JVM_OPTS、JAVA_OPTS 和 GRADLE_OPTS）
#       依赖单词分割时，会显式执行；详见内联注释。
#
#       有针对特定操作系统的调整，如 AIX、CygWin、Darwin、MinGW 和 NonStop。
#
#   (3) 此脚本从 Groovy 模板生成
#       https://github.com/gradle/gradle/blob/HEAD/subprojects/plugins/src/main/resources/org/gradle/api/internal/plugins/unixStartScript.txt
#       位于 Gradle 项目内。
#
#       你可以在 https://github.com/gradle/gradle/ 找到 Gradle。
#
##############################################################################

# 尝试设置 APP_HOME

# 解析链接：$0 可能是一个链接
app_path=$0

# 处理链式符号链接
while
    APP_HOME=${app_path%"${app_path##*/}"}  # 保留尾部 /；如果没有前导路径则为空
    [ -h "$app_path" ]
do
    ls=$( ls -ld "$app_path" )
    link=${ls#*' -> '}
    case $link in             #(
      /*)   app_path=$link ;; #(
      *)    app_path=$APP_HOME$link ;;
    esac
done

# 通常不使用
# shellcheck disable=SC2034
APP_BASE_NAME=${0##*/}
# 丢弃 cd 的标准输出，以防设置了 CDPATH（https://github.com/gradle/gradle/issues/25036）
APP_HOME=$( cd "${APP_HOME:-./}" > /dev/null && pwd -P ) || exit

# 使用可用的最大值，或设置 MAX_FD != -1 来使用该值
MAX_FD=maximum

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# 操作系统特定支持（必须为 'true' 或 'false'）
cygwin=false
msys=false
darwin=false
nonstop=false
case "$( uname )" in                #(
  CYGWIN* )         cygwin=true  ;; #(
  Darwin* )         darwin=true  ;; #(
  MSYS* | MINGW* )  msys=true    ;; #(
  NONSTOP* )        nonstop=true ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar


# 确定用于启动 JVM 的 Java 命令
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM 的 JDK 在 AIX 上使用不寻常的可执行文件位置
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "错误：JAVA_HOME 设置为无效目录：$JAVA_HOME

请在环境中设置 JAVA_HOME 变量，以匹配
Java 安装的位置。"
    fi
else
    JAVACMD=java
    if ! command -v java >/dev/null 2>&1
    then
        die "错误：未设置 JAVA_HOME，且在 PATH 中未找到 'java' 命令。

请在环境中设置 JAVA_HOME 变量，以匹配
Java 安装的位置。"
    fi
fi

# 如果可能，增加最大文件描述符限制
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    case $MAX_FD in #(
      max*)
        # 在 POSIX sh 中，ulimit -H 未定义。因此检查结果是否有效
        # shellcheck disable=SC2039,SC3045
        MAX_FD=$( ulimit -H -n ) ||
            warn "无法查询最大文件描述符限制"
    esac
    case $MAX_FD in  #(
      '' | soft) :;; #(
      *)
        # 在 POSIX sh 中，ulimit -n 未定义。因此检查结果是否有效
        # shellcheck disable=SC2039,SC3045
        ulimit -n "$MAX_FD" ||
            warn "无法将最大文件描述符限制设置为 $MAX_FD"
    esac
fi

# 收集 java 命令的所有参数，按相反顺序堆叠：
#   * 来自命令行的参数
#   * 主类名
#   * -classpath
#   * -D...appname 设置
#   * --module-path（仅在需要时）
#   * DEFAULT_JVM_OPTS、JAVA_OPTS 和 GRADLE_OPTS 环境变量

# 对于 Cygwin 或 MSYS，在运行 java 前将路径转换为 Windows 格式
if "$cygwin" || "$msys" ; then
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
    CLASSPATH=$( cygpath --path --mixed "$CLASSPATH" )

    JAVACMD=$( cygpath --unix "$JAVACMD" )

    # 现在转换参数 - 限制自己使用 /bin/sh
    for arg do
        if
            case $arg in                                #(
              -*)   false ;;                            # 不要乱动选项 #(
              /?*)  t=${arg#/} t=/${t%%/*}              # 看起来像 POSIX 文件路径
                    [ -e "$t" ] ;;                      #(
              *)    false ;;
            esac
        then
            arg=$( cygpath --path --ignore --mixed "$arg" )
        fi
        # 将参数列表轮转与参数数量相同的次数，使每个参数回到
        # 起始位置，但可能已被修改。
        #
        # 注意：`for` 循环在开始前捕获迭代列表，因此
        # 在此更改位置参数既不影响迭代次数，
        # 也不影响在 `arg` 中呈现的值。
        shift                   # 移除旧参数
        set -- "$@" "$arg"      # 推入替换参数
    done
fi


# 在此处添加默认 JVM 选项。也可以通过 JAVA_OPTS 和 GRADLE_OPTS 传递 JVM 参数
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# 收集 java 命令的所有参数：
#   * DEFAULT_JVM_OPTS、JAVA_OPTS、JAVA_OPTS 和 optsEnvironmentVar 不允许包含 shell 片段，
#     且任何嵌入的 shell 特性都将被转义。
#   * 例如：用户不能期望 ${Hostname} 被展开，因为它是环境变量，
#     将在命令行中被视为 '${Hostname}' 本身。

set -- \
        "-Dorg.gradle.appname=$APP_BASE_NAME" \
        -classpath "$CLASSPATH" \
        org.gradle.wrapper.GradleWrapperMain \
        "$@"

# 当 "xargs" 不可用时停止
if ! command -v xargs >/dev/null 2>&1
then
    die "xargs 不可用"
fi

# 使用 "xargs" 解析带引号的参数
#
# 使用 -n1 时每行输出一个参数，移除引号和反斜杠
#
# 在 Bash 中我们可以简单地：
#
#   readarray ARGS < <( xargs -n1 <<<"$var" ) &&
#   set -- "${ARGS[@]}" "$@"
#
# 但 POSIX shell 既没有数组也没有命令替换，因此我们
# 后处理每个参数（作为 sed 的一行输入），对任何可能
# 是 shell 元字符的字符进行反斜杠转义，然后使用 eval
# 反转该过程（同时保持参数之间的分隔），并将整个
# 内容包装为单个 "set" 语句。
#
# 如果这些变量中的任何一个包含换行符或
# 不匹配的引号，这显然会出错。
#

eval "set -- $(
        printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS" |
        xargs -n1 |
        sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' |
        tr '\n' ' '
    )" '"$@"'

exec "$JAVACMD" "$@"