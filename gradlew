#!/bin/sh

# Gradle wrapper startup script for Unix.
# See: https://docs.gradle.org/current/user/gradle_wrapper.html

APP_HOME=$(cd "$(dirname "$0")" || exit 1; pwd)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -z "$JAVA_HOME" ]; then
    if command -v java >/dev/null 2>&1; then
        JAVA_HOME=$(dirname "$(dirname "$(readlink -f "$(which java)")")")
        export JAVA_HOME
    else
        echo "Error: JAVA_HOME is not set and java was not found on PATH."
        exit 1
    fi
fi

exec "$JAVA_HOME/bin/java" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
