# Crash Bandicoot PS1 Launcher for Android

A small Android app that lets you select a PlayStation disc image and open it
with an installed emulator. The app does not contain the game, a BIOS, or an
emulator.

## Build and install

Open this project in Android Studio, let Gradle sync, then run the `app`
configuration on an Android device or emulator. The project uses Java 17 and
Android SDK 35.

## Use

1. Install and configure a PlayStation emulator that can open files shared by
   other Android apps.
2. Open Crash Bandicoot PS1 Launcher and select your legally obtained disc
   image. The selection is remembered.
3. Tap **PLAY CRASH BANDICOOT** and choose the emulator in Android's app
   chooser.

For multi-track discs, `.chd` is often the easiest format to share with an
Android emulator. If using a `.cue` file, the emulator must also be able to
access its referenced track files. BIOS setup, when required, is done in the
emulator. Obtain game and BIOS files from your own lawful sources.
