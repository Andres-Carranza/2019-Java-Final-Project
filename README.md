This README was composed by Andres Carranza on 5/28/2019

This completed program was written by Andres Carranza, Brandon Butsch, and Ethan Pereira.

Our program is based off of famous apple application, Game Pigeon. Our program has two parts: a client part, and a server part. Different clients can each run on different computers, and connect to each other via the server. With this connection established, the clients will be able to chat with one another, and even play multiplayer games.

The program will provide a texting service which is able to text any other computer connected to the same local service. The program will also allow users to play Tic-Tac-Toe, Connect Four, and Pool with one another from different computers. 

Classes:
        
1) Ball.java: Andres 
	- This class handles everything related to a ball in pool

2) Client.java: Andres
	- This class relays messages from the Server to Main and vice versa

3) ConnectFour.java: Andres and Brandon
	- This class handles the gameplay for connect four

4) ConnectScene.java: Andres
	- This class prompts the user to connect to the server

5) Conversation.java: Andres
	- This class handles an instance of a conversation

6) FontWidth.java: Andres
	- This class aids in creating graphics by calculating the width of a font

7) Game.java: Andres
	- This class is the parent class to all games

8) LocalToolkit.java: Andres
	- This class provides useful methods to aid other classes

9)Main.java: Andres
	- This class controls the whole program

10) MessageScene.java: Andres
	- This class provides the menu for creating a new conversation 

11) Pool.java: Andres
	- This class controls the gameplay for a pool game

12) Server.java: Andres
	- This class relays messages from client to client

13) Stick.java: Andres
	- This class handles everything related to a stick in pool

14) Table.java: Andres
	- This class holds the coordinates for the pool table

15) TicTacToe.java: Andres and Ethan
	- This class handles the gameplay for Tic-Tac-Toe

16) Vector.java: Andres
	- This class represents a vector

Responsibilities:
Andres Carranza: Maintenance of server, GUI design, and grouping all components of the program together and the pool game.
Brandon Butsch: creating the Connect 4 game.
Ethan Pereira: Creating the tic-tac-toe game 



