#!/bin/sh
# Run this once in Firebase Studio terminal to bootstrap the project
echo "Downloading Gradle wrapper jar..."
mkdir -p gradle/wrapper
curl -L -o gradle/wrapper/gradle-wrapper.jar \
  "https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar"
chmod +x gradlew
echo "Done! Now run: ./gradlew assembleDebug"
