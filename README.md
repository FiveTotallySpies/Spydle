# Spydle :bomb:

Spydle is a word guessing game. On your turn, you are given a substring of 2/3 characters, like "ent". You need to guess a word that has those characters. Longer words get more points!

Player with the most points wins.

This program was developed by Alexandra Worms, Ihor Chovpan, Kai Lo, Polyna Germa, & Xin Lei Lin in the context of CSC207.

## Table of Contents
- [Features](#features)
- [Installation Instructions](#installation-instructions)
- [Usage](#usage)
- [License](#license)
- [Feedback and Contributions](#feedback-and-contributions)

## Features
### Frontend Use Cases and Features:
- **Create Game**: A user may createa a room from the Welcome View
- **Join Game**: User may join a game by entering the game's room code, followed by a click of the **_Join Game_** button.
- **List Games**: A user may see a list of all possible rooms by selecting the **_View Rooms_** Button on the welcome page.
- **Guess Word**: The guess of the player is received, when they type **_Enter_** or click on the **_Submit_** button.
- **Update Guess**: As a player types their guess, their guess is displayed on top of their player, for every letter that is typed or removed.

## Installation Instructions
There are a few options to run the program, for 

## Option 1: run the frontend that will connect to our backend server

### Option 1.1: download the frontend .jar file
You can find the file in the Releases page of our repository.

### Option 1.2: Building the frontend .jar
1. Clone the repository:
   ```bash
   git clone https://github.com/FiveTotallySpies/Spydle.git

2. Build the .jar, run the following command from the project root directory:
   ```bash
   gradle :frontend:build

   This will build a file called `frontend-1.0.jar`.

3. The .jar is located in `frontend/build/libs` directory. You can move it to any location.

## Option 2: Running frontend/backend locally

Please refer to INSTALLATION_LOCAL.md for more details.

2. Run ```dev/totallyspies/spydle/frontend/FrontendApplication.java``` file with your favorite IDE / compiler to boot up and enjoy the game!

## Usage



### This video contains an example of GamePlay:
https://github.com/user-attachments/assets/e2d70dd6-6ddd-4364-957b-332f424d9bd3

## Feedback and Contributions
#### To contribute to our project, you may always fork the repo, and create a pull request!
If any Issues arise, please create a new issue, and it will be addressed by one of our developers shortly.

#### Here is a microsoft form, where you may enter any suggestions you like!
[Microsoft Feedback Form: Suggestions are Welcomed!](https://forms.office.com/r/LXEcKxfLuT)

## License
MIT License
Copyright (c) [2024] [FiveTotallySpies]

