# Snakes-And-Ladders

# Snakes & Ladders – Online Multiplayer Game

A real-time, two-player Snakes & Ladders game built in Java, with a custom TCP socket-based client-server architecture deployed on AWS EC2 for global play.

## Features

- **Online multiplayer** – players connect to a central server hosted on AWS EC2 and play against each other from anywhere.
- **Lobby system** – logged-in players see a real-time list of available users and their status (Available / Pending / In Game).
- **Invite system** – players can invite others to a match; invites can be accepted or rejected.
- **Live gameplay sync** – dice rolls, positions, and turn order are synchronized between both clients in real time.
- **Snakes & ladders board logic** – 100-square board with configurable ladder and snake positions.
- **Disconnect handling** – if a player disconnects mid-game, the opponent is notified and both players are safely returned to the lobby.

## Tech Stack

- **Language:** Java (Swing for UI)
- **Networking:** Custom protocol over TCP sockets (`java.net.Socket`, `ServerSocket`)
- **Deployment:** AWS EC2 (public server, global access)
- **Concurrency:** Multithreading — each connected client is handled on its own thread, with synchronized access to shared game/lobby state

## Architecture

**Server side:**
- `Server` – accepts incoming socket connections and spawns a `ClientHandler` thread for each client.
- `ClientHandler` – handles all communication with a single client (login, invites, dice rolls, disconnects).
- `LobbyManager` – tracks connected players, their status, and pending invites; broadcasts lobby updates to all clients.
- `GameRoom` – runs an individual match between two players: dice logic, board (snakes/ladders) lookup, turn management, win condition.
- `GameState` – holds the live state of a single match (positions, current turn, game-over status).

**Client side:**
- `GameClient` – manages the socket connection to the server, sends user actions, and routes incoming messages to the correct UI screen (lobby or game board).
- UI screens (Start, Lobby, Game) update in real time based on messages received from the server.

## Network Protocol

Communication uses a simple pipe-delimited text protocol over TCP, e.g.:

| Message | Direction | Meaning |
|---|---|---|
| `LOGIN\|username` | Client → Server | Player logs in |
| `LOBBY_UPDATE\|user1:STATUS\|user2:STATUS...` | Server → Client | Current lobby state |
| `INVITE\|targetUser` | Client → Server | Send a match invite |
| `INVITE_REQUEST\|fromUser` | Server → Client | Notify of incoming invite |
| `GAME_START\|opponent\|FIRST/SECOND` | Server → Client | Match begins, turn order assigned |
| `ROLL_REQUEST` | Client → Server | Player rolls the dice |
| `GAME_STATE\|dice\|pos1\|pos2\|currentPlayer` | Server → Client | Broadcast updated board state |
| `GAME_OVER\|winner` | Server → Client | Match result |

## What I Learned

This project was my introduction to network programming and client-server architecture: designing a custom communication protocol, managing concurrent client connections with multithreading, handling synchronization issues (e.g. turn order, shared lobby state), and deploying a server to the cloud (AWS EC2) for real-world, global accessibility — directly relevant to the network/telemetry work I do on the rocket avionics team.
