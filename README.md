# TCPSailorGame

General description of the solution:

The program is written on the basis of a simple chat in the local network (source: 
https://www.youtube.com/watch?v=pcIoZA2c-5k). The user interface is implemented in the GUI using Swing. The main units 
involved in the game are Server and TCPConnection, which in turn is a player. The player turns on by running the ClientWindow
class.
Each connection to the server supports the TCPConnectionObserver interface, which describes the events that occur during the 
connection. This interface has 4 methods:
• connectionReady (TCPConnection connection) - is responsible for handling the new connection
• recievedMessage (TCPConnection connection, String message) - is responsible for handling the message received from the client
• disconnected (TCPConnection connection) - is responsible for handling the event of client disconnection from the game
• unexpectedError (TCPConnection connection, Exception e) - is used to correctly handle exceptions that may arise during
the game
The server accepts all connections and saves them in a list. Multi-threading is implemented to guarantee the operation of
each client in a separate thread.

Detailed description of the solution:

It is worth noting that each client when joining the game has two states:
• Inside the "lobby" - this means that the client is already connected to the server, but is not added to the player list. He
must take a few steps to join the game
• Inside the game - this means that the client is added to the player list and has the right to play the game with all other
players
More precisely, when joining the first player, he will be directly attached to the game, because he is his own entry agent,
but he is unable to play and other clients cannot join the game until the first player uses the / SET command that he sets
for the first player's name (WARNING! /SET can be used ONLY by the first player). All other players will first be joined to
the "lobby" until they use the /JOIN command (spoken of later), indicating immediately their name and the name of the player
who is already in the game.
As said before, the server receives messages from clients. They are divided into 3 types: commands, plain text for
communication with players and the numbers given in the game. Commands, in turn, are preceded by the sign '/'. In this case,
the Server accepts 4 basic commands:
• /JOIN own_name first name of the current player - is used by the client to join the game and establish a unique name
• /SET own_name - used to determine the name of the first player
• /ASK - used by the client to obtain information about current players
• /QUIT - used to disconnect a player from the game and remove him from the player list. At the same time, it is not
completely disconnected from the server, it can join the game at any time by using the / JOIN command. (NOTE! This command
may not be used during the game)
All other messages containing the sign '/' will be ignored by the Server, the client will receive a message regarding the
inappropriate use of the sign '/'. All other messages will be displayed to other players for communication (there is no
communication between players during the game) or will be saved as numbers during the game.
After joining the game, the Server will check the presence of at least 2 players to start the game. When the condition is met,
the Server will begin a countdown of up to 15 seconds, through which the Server will create a list of all game rounds
between all current players. During these 15 seconds, players who are not included in the game will still be able to join.
After 15 seconds, all players will receive a message about all competitions between current players and their order. The game
between the first participants of this list will start immediately.
The gameplay looks as follows:
a) A pair of players will receive a message informing them of the player from whom the countdown will begin. This player is
designated randomly
b) Player A and player B give one integer
c) After the numbers have been saved, the Server sums the numbers and calculates
the winner: even sum + deduction from A - B wins, even sum + deduction from B - A will win; same with an odd number, only on
retreat.
d) After the game a pair of players will receive a message informing about the winner
e) The next pair plays the game in the same way until the pair ends
f) Eventually all players will receive the leaderboard, Server will start
a countdown of up to 15 seconds during which new players can join the game and already existing players can leave the game and
a renewal game starts if at least 2 players are present
It is worth noting that for the fairness of the game, the "Caesar cipher" cryptography method has been added, allowing for safe
number transfer between the player and the server. The method works as follows:
a) When joining a player, a key from 0 to 100 is randomly assigned
b) When choosing a number during the game, the key to is automatically added
selected number
c) When calculating, the key is automatically subtracted from the selected number

Observations, experiments and conclusions:

I would like to note some restrictions defined for the fair and orderly operation of the program:
General:
1. Messages sent by the server will only be received by players connected to the game 
2. It is not allowed to specify names that are repeated in the list of players
During the game:
1. There is no communication between players
2. All messages except numbers will be ignored
3. It is not possible to join new players (it is possible between
competitions
4. Do not change the number you have chosen
5. Do not leave the game by the / QUIT command. In case of
leaving the game systemically, the game will end automatically and start from the beginning with players present at that time
Using commands:
All messages containing '/' other than existing commands will be ignored
