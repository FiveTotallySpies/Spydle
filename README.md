
# Spydle :bomb:

Spydle is a word guessing game. On your turn, you are given a substring of 2/3 characters, like "ent". You need to guess a word that has those characters. Longer words get more points, and the player with the most points wins!

This program was developed by [Alexandra Worms](https://github.com/Lftw), [Ihor Chovpan](https://github.com/chopikus), [Kai Lo](https://github.com/klokailo), [Polyna Germa](https://github.com/nanogotalk), & [Xin Lei Lin](https://github.com/xinlei55555) for our CSC207 class.

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
- **Create Game**: A user may createa a room from the Welcome View
- **Join Game**: User may join a game by entering the game's room code, followed by a click of the **_Join Game_** button.
- **List Games**: A user may see a list of all possible rooms by selecting the **_View Rooms_** Button on the welcome page.
- **Guess Word**: The guess of the player is received, when they type **_Enter_** or click on the **_Submit_** button.
- **Update Guess**: As a player types their guess, their guess is displayed on top of their player, for every letter that is typed or removed.

### TODO (how to play the game)

### This video contains an example of game-play:
https://github.com/user-attachments/assets/e2d70dd6-6ddd-4364-957b-332f424d9bd3

# Design

The design of this project is one that intends to mirror the patterns that exist in real-world software development and game design. Therefore, some choices (specifically in the Backend Design document) may take us a little bit past the scope of our CSC207 course. An in-depth understanding of fully architecture would take a while to understand, but our basic design philosophies should be clear.


## Backend Design
Backend design has been split into its own document. TODO

## Clean Architecture

TODO

## SOLID Principles

TODO

## SpringBoot Dependency Injection

TODO

## Design Patterns

TODO

## Design Discussions

TODO



# Feedback and Contributions
#### To contribute to our project, you may always fork the repo, and create a pull request!
If any Issues arise, please create a new issue, and it will be addressed by one of our developers shortly.

#### Here is a form where you may enter any suggestions you like!
[Microsoft Feedback Form: Suggestions are Welcomed!](https://forms.office.com/r/LXEcKxfLuT)

# License
MIT License    
Copyright (c) [2024] [FiveTotallySpies]