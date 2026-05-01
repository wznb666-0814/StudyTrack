#!/usr/bin/env sh
DIR="$(cd "$(dirname "$0")"; pwd)"
if [ -n "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD="java"
fi
exec "$JAVA_CMD" --enable-native-access=ALL-UNNAMED -classpath "$DIR/gradle/wrapper/gradle-wrapper.jar:$DIR/gradle/wrapper/gradle-wrapper-shared.jar:$DIR/gradle/wrapper/gradle-cli.jar" org.gradle.wrapper.GradleWrapperMain "$@"
