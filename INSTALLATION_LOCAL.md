
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
This is the simplest option if you just want to run the game.

1. You can find the file in the Releases page of our repository.
2. Running this jar requires that you have Java 17 installed on your system.
### Option B: Building from Source
This is the simplest option if you would like to modify the <b>frontend</b> code and run any changes you make.
1. Clone the repository in IntelliJ IDEA and wait for it to import gradle dependencies
2. Set the project JDK to corretto-17
3. Run the following gradle command: `gradle :frontend:build`
4. A .jar is located in `frontend/build/libs` directory. You can move it to any location, and double click to to run it.
5. Alternatively, you can also create a Spring Boot run configuration in IntelliJ IDEA Ultimate for the `FrontendApplication` file.

### Option C: Build from Source and Run Locally
This is the simplest option if you want to modify the <b>gameserver</b> and <b>frontend</b> code and run a full emulation of both the frontend and backend.
1. Follow the instructions in option B
2. Create Spring Boot run configurations for `FrontendApplication` and `GameServerApplication`. For both of them, set the active profiles in the run configuration to `local`.
3. Create a compound run configuration in IntelliJ with both the Frontend and the GameServer.
4. Running this configuration will start the gameserver in "local" mode, then you can connect to it using the frontend by entering the room code "LOCAL"

### Option D: Build from Source and Run Local Kubernetes Cluster
This is the option if you want a fully-fledge Kubernetes deployment of our system. This is good if you want to modify the <b>matchmaker</b> or <b>deployment</b> Kubernetes manifests in the deployment folder.

This may require at least an elementary understanding of Kubernetes and systems design.

Follow the instructions in the other installation instructions in INSTALLATION_LOCAL.MD

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

## License
MIT License    
Copyright (c) [2024] [FiveTotallySpies]