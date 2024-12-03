
# Spydle :bomb:

Spydle is a word guessing game. On your turn, you are given a substring of 2/3 characters, like "ent". You need to guess a word that has those characters. Longer words get more points, and the player with the most points wins!

This program was developed by [Alexandra Worms](https://github.com/Lftw), [Ihor Chovpan](https://github.com/chopikus), [Kai Lo](https://github.com/klokailo), [Polyna German](https://github.com/nanogotalk), & [Xin Lei Lin](https://github.com/xinlei55555) for our CSC207 class.

# Table of Contents
- [Installation Instructions](#installation-instructions)
- [Usage](#usage)
- [Design](#design)
- [License](#license)
- [Feedback and Contributions](#feedback-and-contributions)

# Installation
There are a few options to run the program, for

### Option A: Download Release Jar
You can find the file in the Releases page of our repository.

Running this jar requires that you have Java 17 installed on your system.
### Option B: Building from Source
1. Clone the repository in IntelliJ IDEA and wait for it to import gradle dependencies
2. Set the project JDK to corretto-17
3. Run the following gradle command: `gradle :frontend:build`
4. A .jar is located in `frontend/build/libs` directory. You can move it to any location, and double click to to run it.
5. Alternatively, you can also create a Spring Boot run configuration in IntelliJ IDEA Ultimate for the `FrontendApplication` file.

### Option C: Build from Source and Run Locally
1. Follow the instructions in option B
2. Create Spring Boot run configurations for `FrontendApplication` and `GameServerApplication`. For both of them, set the active profiles in the run configuration to `local`.
3. Create a compound run configuration in IntelliJ with both the Frontend and the GameServer.
4. Running this configuration will start the gameserver in "local" mode, then you can connect to it using the frontend by entering the room code "LOCAL"

# Usage

### Frontend Use Cases and Features:
- **Create Game**: A user may create a room from the Welcome View
- **Join Game**: User may join a game by entering the game's room code, followed by a click of the **_Join Game_** button.
- **List Games**: A user may see a list of all possible rooms by selecting the **_View Rooms_** Button on the welcome page.
- **Guess Word**: The guess of the player is received, when they type **_Enter_** or click on the **_Submit_** button.
- **Update Guess**: As a player types their guess, their guess is displayed on top of their player, for every letter that is typed or removed.

### TODO (how to play the game)

### This video contains an example of game-play:
https://github.com/user-attachments/assets/e2d70dd6-6ddd-4364-957b-332f424d9bd3

# Design

The design of this project is one that intends to mirror the patterns that exist in real-world software development and game design. Therefore, some choices (specifically in the Backend Design document) may take us a little bit past the scope of our CSC207 course. An in-depth understanding of the full architecture would take a while to understand, but our basic design philosophies should be clear.


## Backend Design
Backend design has been split into its own document. TODO

This document may serve as proof of the scope of our project.

## Clean Architecture

TODO

## SOLID Principles

TODO

## SpringBoot Dependency Injection

TODO

### Design Patterns

We used quite a lot of design patterns when implementing this project, like [Builder](https://github.com/FiveTotallySpies/Spydle/blob/86e788d76d42f62a60780a0737f4ac25da6dace9/gameserver/src/main/java/dev/totallyspies/spydle/gameserver/game/GameLogicEvents.java#L260-L270), [Decorator](https://github.com/FiveTotallySpies/Spydle/blob/main/shared/src/main/java/dev/totallyspies/spydle/shared/message/MessageHandler.java), [Singleton](https://github.com/FiveTotallySpies/Spydle/blob/main/shared/src/main/java/dev/totallyspies/spydle/shared/Clock.java), [Proxy](https://github.com/FiveTotallySpies/Spydle/blob/86e788d76d42f62a60780a0737f4ac25da6dace9/gameserver/src/main/java/dev/totallyspies/spydle/gameserver/game/GameLogicEvents.java#L71-L83)

### Builder pattern example

Builder pattern is used to create messages that are sent between a client and a server.

Each time a message property is set (for example, in `setAssignedString` method), a builder reference is returned. That way we can configure the message in a variety of ways.

Example:
```
public CbMessage newTurnMessage() {
    return CbMessage.newBuilder()
        .setNewTurn(
            CbNewTurn.newBuilder()
                .setAssignedString(gameLogic.getCurrentSubString())
                .setCurrentPlayer(
                    Player.newBuilder()
                        .setPlayerName(gameLogic.getCurrentPlayer().getName())
                        .setScore(gameLogic.getCurrentPlayer().getScore())))
        .build();
}
```

[This](https://github.com/FiveTotallySpies/Spydle/blob/86e788d76d42f62a60780a0737f4ac25da6dace9/gameserver/src/main/java/dev/totallyspies/spydle/gameserver/game/GameLogicEvents.java#L260-L270) method creates a message that the backend sends on every new turn.

### Decorator pattern example

`SbMessageListener` is a custom decorator. The implementation for it is almost empty.

Here is the [SbMessageListener](https://github.com/FiveTotallySpies/Spydle/blob/86e788d76d42f62a60780a0737f4ac25da6dace9/gameserver/src/main/java/dev/totallyspies/spydle/gameserver/socket/SbMessageListener.java#L8-L10):
```
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SbMessageListener {}
```

Then, the class [`MessageHandler`](https://github.com/FiveTotallySpies/Spydle/blob/main/shared/src/main/java/dev/totallyspies/spydle/shared/message/MessageHandler.java) remembers every method annotated with @SbMessageListener and, when a new message is received, calls all those methods.

We listen for client bound messages in a similar way using `CbMessageListener` annotation.

## Design Discussions

TODO

# Feedback and Contributions
## Google Java Format
We adhered to the Google Java Format, and checked our code with the Google Java Format styler.

#### To contribute to our project:
You may 'Fork' this repository, and create a pull request!
We will strive to review the pull request shortly and come back to you for any further improvements.
Pull requests should have a description of changes which were implemented, including any potential deletions, or redeployments which should be performed!
Finally, make sure that all the Unit tests that were developed pass before creating a pull request!

Happy coding!

#### Here is a form where you may enter any feedback:
[Microsoft Feedback Form: Suggestions are Welcomed!](https://forms.office.com/r/LXEcKxfLuT)
If any Issues arise, please create a new issue, and it will be addressed by one of our developers shortly.
We are happy to handle any issues such as new features, new tests, and fixing bugs.

# License
MIT License    
Copyright (c) [2024] [FiveTotallySpies]
