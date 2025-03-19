## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).

## run

javac -d mods --module-path "$PATH_TO_FX" --add-modules javafx.controls,javafx.graphics src/module-info.java src/game/FlightGame.java

java --module-path "$PATH_TO_FX:mods" --add-modules javafx.controls,javafx.graphics -m FlightGame/game.FlightGame
