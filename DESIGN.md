
# Part 0: Overview

This design document will outline both the thought process behind, and the details of our system we will create for our    
game.

## Goals

We want to develop a multiplayer game. Almost all of the discussion that follows in this document eventually stems from    
the keyword, "multiplayer".

This document's goal is to outline how we design a system for a multiplayer game, and answer the following questions:

- How do we manage more than one concurrent game between players? (Alice and Bob might be playing a game, but so are    
  Charlie and David at the same time!)
- How do we define an <i>organized</i> method of communication between the code running for each of our players, and the    
  shared server that they communicate with?

The way we solve for these questions may seem very unobvious at first. Reading through this full document will give you    
the bigger picture understanding of how we tackle each of these issues.

### Why This Design?

We must also be aware that yes, this approach is an overcomplication, if our scope is narrowed to a functioning game: It    
is certainly possible to write a solution for this problem that doesn't involve most of the tools I am going to mention.    
But I believe this is a good choice for one (of many) different reasons: Everything described below is one of the many    
ways real teams write code for real projects*. Gaining experience with this design (or at least awareness of it) is    
important if you want to work on many different projects.

<i>*Note that the reason why teams choose this design is different than ours: they often needs systems that are    
reliable, and scale for their very large userbases. We instead seek to emulate this for our own experience, despite not    
having the same strict requirements.</i>

## Principals

We will operate around a few very basic key principals:

- There will be a <b>client-server</b> relationship for our application:
    - Our players each run a <i>client application</i> that communicates directly with a <i>server application</i>, as    
      opposed to the clients communicate directly with each other.
- We want to support having multiple concurrent games between players (as mentioned in the Goals). Therefore, <b>each    
  game between players will have its own instance of our server application</b>.
    - To put it extremely simply: imagine different computers each running their own instance of our `spydle-server.jar`    
      application on it.
    - This means that for different games, each user may have to connect to a different server application.

## What Does this Cover?

This document will cover:
- An architectural overview of how we intend to communicate between various components of our application
- How we will deploy our application
- How we will manage our application

This document will NOT cover:
- What the game logic must calculate and handle
- What the frontend must display and handle
- Concrete implementations of anything

# Part 1: Architecture Background

We are going to take a big step away from the goals and the key principals to consider when designing this system to    
instead familiarize ourselves with some tools that we will later use to accomplish this design.

## Understanding Our Toolset

### Tool: Docker

When building applications that may involve several different semi-independent components (such as one where we have    
multiple GameServers for each active game), <b>docker containers</b> is the first place we might look to abstract out    
certain functionalities.

- <b>Containers</b> allow us to run an app in an isolated, typically linux, environment. This is similar to a Virtual    
  Machine that is completely (mostly) detached from the Host OS layer, but more geared towards extremely minimal Linux    
  environments that have just the barebones essentials for running our application. This way we can keep it fast, and    
  create many containers on a single Host OS.
    - The benefits of running an application within a container are twofold:
        - We can precisely define what libraries and other files are necessary to download before running it.
        - The application's OS is separated from our host and has no chance of impacting what occurs below it.
- <b>Docker Images</b> are like templates for creating containers: They specify what container OS we might run (such as    
  ubuntu linux!), and what libraries we may need to download into our container if we want to use it to run our    
  application.
    - Docker images (templates) can be created with <b>Dockerfiles</b> that are a precise set of instructions for    
      creating a docker image from our codebase.

### Tool: Kubernetes

<b>Kubernetes</b> (K8s) is one layer of abstraction above this. It aims to answer the question: If we have an    
application that may require multiple docker containers running concurrently (like, one for each GameServer, and maybe a    
GameServer manager), how do we determine what container runs when? And how do we connect them together?

- To answer this question we might look to creating some sort of big script program that can manage each container    
  directly. This is certainly one solution (that has several tools for it that implement this), but is seldom    
  implemented because of how it becomes exponentially more complex the more components you have for your application.
- Kubernetes steps in with a different approach:
    - We have a single <b>Kubernetes Cluster</b> that is a place where all of our different application components run
    - Inside the cluster we define a <b>Resource</b> for everything we <b>deploy</b> (run) inside the cluster.
        - These resources are not themselves necessarily docker containers (although they can be). These include things    
          like Pods, Services, Deployments, Network Policies, Load Balancers, Ingress Controllers, and much    
          more. [This article](https://spacelift.io/blog/kubernetes-workload) can give you a basic idea of what    
          resources might look like, but understanding all of the options    
          comes [with experience](https://kubernetes.io/docs/tutorials/kubernetes-basics/).
    - These resources are configured by a <b>Manifest</b>, which is basically an extremely long set of requirements for    
      which resources we want and how we want to configure them.
    - Our applications will exist as resources (specifically, Pods) inside our cluster. We will be able to configure how    
      they communicate to eachother using other resources we deploy in our cluster.
- The benefits that Kubernetes provides here is the ability to define the specifications of each component of our system    
  in isolation, then <i>separately</i> define how they communicate and integrate with each other.
    - This is a really powerful tool, and is one of the many ways big companies can make incredibly complex projects    
      without needing to make every engineer aware of every subtle detail, and get lost in the sea of unclear    
      functionality definitions and specifications.
- I am not deluded and will not tell you that this comes without drawbacks:
    - Kubernetes can make it far more difficult to debug applications when complexity is mis-managed. A lot of the time    
      projects will evolve without a clear vision in mind, and find it difficult to constantly adapt around Kubernetes.
    - Deployment pipelines (how we modify what we are currently running), while having hugely higher potential, become    
      several thousand times more complex than with a traditional monolith application.
- (Small aside: we will also use a tool called <b>Kustomization</b> that lets us split our manifest into a bunch of    
  smaller YAML files, then combine them together into one large one for us to apply to the cluster)

### Tool: Agones

Next in line is <b>Agones</b>: A layer of abstraction on top of Kubernetes that defines new resources that we can deploy    
in our cluster, specifically with game development in mind:

- A <b>GameServer</b> (in Agones) is a special type of Pod (see explanation below) that enables communication between    
  its docker container, and multiple Agones services.
    - GameServers can also have a <b>state</b> assigned to them, which indicates what kind of server they are:
        - READY: Indicates that this GameServer has been started, but doesn't have any players connected with it to    
          start a game. (This implies it is waiting for players to use it).
        - ALLOCATED: Indicates that this GameServer is currently in use by some amount of players.
        - (There are several others    
          detailed [here](https://agones.dev/site/docs/reference/gameserver/#gameserver-state-diagram), but these are    
          less important to us).
- A <b>Fleet</b> is a resource that itself contains a set of GameServers, and can be scaled up or down (i.e. modify the    
  number of GameServers it contains).
- The <b>Agones Allocator Service</b> is an app that lives in our cluster that we can communicate directly with. At any    
  point in time, we can <i>request</i> from the allocator to give us the details of a GameServer resource that is    
  currently in READY state (no players connected), and the mark it as ALLOCATED so that we can start letting players    
  connect to it.

### Tool: SpringBoot

On a much more technical level: SpringBoot is both a framework and a very large set of libraries that help writing Java    
code for server applications.    
Libraries it offers can be for things like:

- Hosting HTTP servers (like handling requests to a server, such as    `/create-game`)
- Managing WebSockets where our server applications may communicate with clients directly.
    - For more details, look up what WebSockets are and how they might be different from an HTTP server.
- Connecting easily to external databases

Which are all things we will need to be doing for our server applications. Awesome!

Spring can be confusing in the beginning because it is <b>annotation-based</b>: Any time I want the spring framework to    
treat a class or a method differently, I write an `@Annotation` above it. Spring will then notice these annotation and    
behave accordingly.    
Also, JVM objects that Spring is allowed to manage directly are called <b>Beans</b>. The rules for these are simple:

- Beans can be created from a single object.
- Beans are singletons: there only exists one instance at all times of a bean
- Beans are loaded into the Spring framework on application startup: And once it is done, we can never add new beans.

The idea behind spring beans may seem arbitrary or unnecessary, but is actually incredibly helpful when    
considering [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection). How this is the case is once    
again not immediately obvious, but will be as you use Spring more and more.

Here is a [decent article](https://medium.com/@yashpatel007/spring-boot-for-dummies-part-1-1-f0bf717c862e) for    
understanding the basic tenets of the SpringBoot framework.

<i>Small aside: I, like many other people, am not the largest fans of the SpringBoot framework because it erases the    
Java language's largest benefit: its verbosity. When I write a class in Java, I can see directly how it acts, and links    
to how it interacts with other classes. When Spring hides all these layers of functionality behind @Annotations that we    
put on our classes and methods, we lose the ability to clearly see what functionality is linked together.</i>

### Tool: Redis

When designing a system on Kubernetes that can scale, it is often helpful to write <b>stateless</b> applications.

- A stateless program is one that doesn't store any important information about previous user requests.
- This implies that if we were to have a set of docker containers all running the same <i>stateless</i> application, it    
  shouldn't matter which one we send a request to because the responses will all be the same: the application does not    
  store any associated "state".

In Kubernetes, stateless applications are to our benefit because they allow us to run multiple instances of the same    
program, and then distribute requests across them for maximum performance.    
A common method of separating such storing of any state is through a <b>shared database</b>, where all instances of our    
program access the same data in a common external database.

<b>Redis</b> is a <b>key-value</b> database that by default stores things <b>in-memory</b> for extremely low latency.    
Think of it as a giant HashMap that we store in a separate Kubernetes resource, that we do not scale (we only have one    
database).

### Tool: Protobuf

When defining how different applications communicate with each other, they need to have a shared language that they both    
understand. The simplest example of this <b>JSON</b>: Applications can serialize or deserialize any data into/from JSON    
by embedding it in the curly braces, colons and brackets that represent object hierarchy.

But say we wanted to define a specific type of JSON structure for our client and server to send to each other. Maybe the    
JSON object sent should have certain fields (like "messageType" or "clientId") that need to be fulfilled with certain    
values. Maybe some of the fields are optional as well.

Enter <b>protobuf</b>: A tool designed to allow us to define structured data formats that both clients and servers can    
rely on. This tool can work on top of any programming language to specify structured data schemas and enforce them    
rigorously. Instead of simply passing arbitrary JSON, protobuf lets you define a **schema** that explicitly dictates    
which fields are required, which are optional, and what data types each field should have.

An example schema could be:

```proto message ExampleSchema {     
  string clientId = 1;     
string messageType = 2; optional string payload = 3; } ```   
Which the client and server can use to concretely define what messages they send to each other should look like.  
  
### (Smaller) Tool: OpenAPI/Swagger  
  
Swagger is a tool that we use to generate java POJOs that we can use as objects we pass to our spring boot REST endpoint, and to the REST web client.  
  
This serves as an easy way of enforcing structure in our POST requests. Take a look at this same configuration for swagger:  
  
```yaml  
paths:  
/create-game:    
  post:    
    operationId: createGame    
    summary: Create (allocate) new agones gameserver to client    
    requestBody:    
      required: true    
      content:    
        application/json:    
          schema:    
            $ref: '#/components/schemas/CreateGameRequestModel'    
  responses:    
      '200':    
        description: Success    
        content:    
          application/json:    
            schema:    
              $ref: '#/components/schemas/CreateGameResponseModel'  
components:  
 schemas: CreateGameRequestModel:      type: object    
      required: [ clientId, playerName ]    
      properties:    
        clientId:    
          type: string    
        playerName:    
          type: string    
    CreateGameResponseModel:    
      type: object    
      required: [ gameServer, clientId, playerName ]    
      properties:    
        gameServer:    
          $ref: '#/components/schemas/GameServer'    
        clientId:    
          type: string    
        playerName:  
 type: string  
```  
Then on the game-server side we can enforce this POJO's structure like so:
```java  
@PostMapping("/create-game") public ResponseEntity<?> createGame(@Valid @RequestBody CreateGameRequestModel request) { // ... Handle request}  
```  
This way all requests sent to the Spring Tomcat server on the `/create-game` endpoint will be required to have `clientId` and `playerName`, and all responses can have `gameServer`, `clientId` and `playerName`.

We can do a very similar configuration on the web client for our frontend application to send `/create-game` requests, to parse everything into these auto-generated POJOs.
## Additional Terminology

It is highly recommended you be familiar with these terms as you navigate this document.

### Kubernetes

- <b>Namespaces</b> are a certain kind of abstraction within Kubernetes:
    - Every resource in our cluster is defined as <i>belonging</i> to a certain namespace
    - This allows us to define blanket rules on each namespace:
        - Resources in Namespace A are allowed to communicate with resources in Namespace B
        - Resources in Namespace C are required to use a higher level of security when accessing data
        - Resources in Namespace D are not allowed to be exposed to public internet
- <b>Pods</b> are the Kubernetes wrapper around docker containers:
    - A pod represents a single instance of a running application
    - Pods can (but often do not) include several docker containers on them.
        - One of these containers is marked as the "main" container though
    - Example of a pod could be a single instance of our GameServer: note that for each new GameServer resource we    
      created, it lives inside its own individual Pod resource.
- <b>ReplicaSets</b> are a collection of pods where:
    - Each pod is identical in how it was created
    - We can specify to the replicaset how many pods we want it to contain.
        - This allows us to easily scale up and down how many pods we want.
    - Note that Agones <b>Fleets</b> are an abstraction on top of this specifically for GameServers.

### General

- <b>Stateless</b>: to different developers, this word can mean different things. For our purposes, a stateless application is one that does not store any data after it has handled a request.
    - This does NOT say that we aren't allowed to access an external storage, separate from our application when handling requests
    - This implies that all identical requests are handled similarly.
    - This is useful in Kubernetes because it means that all pods running an application are ephemeral in nature: We can destroy them at any time and replace them with other pods (that we know will be running an identical application).
        - Also, we can scale up our application by creating new pods running the same service, and then dividing requests among them. Since the application is stateless, no matter which pod a request hits, the response is guaranteed to be handled the same.

## Getting Technical with Kubernetes

### Nodes

If you want to understand the real resource benefit (like memory/cpu) that Kubernetes provides, this is for you:

- A Kubernetes cluster does not consist of a single server on which all our resources are deployed, but as a collection    
  of <b>Nodes</b>.
    - A node can be a virtual machine in the cloud, or a bare metal server that runs whatever you want.
    - This poses a question: If we are given a set of docker containers that our cluster must run, how do we decide    
      which node runs which one, and how do we <i>distribute the work</i> across all our nodes?
- The Kubernetes <b>Control Plane</b> is responsible for answering this question.
    - It will take into account the resources (memory/cpu) that each pod needs, and how much exists on each node, and    
      distribute the pods each to a separate host node accordingly.
        - Note that this implies that our various pods may likely not be all running on the same host node!
    - The goal of the control plane is to hide this abstraction completely away: You as a developer should not need to    
      be concerned about what node your pods run on, all you need to know is that they are run somewhere.
- This is powerful because it allows us to distribute the work of a system that requires a very high amount of    
  resources (memory/cpu) across a large set of machines (nodes), instead of having to build extremely large and powerful    
  individual bare-metal servers.
    - This is not extremely relevant in our case, but if we did have a hypothetical user-base of at least several    
      hundred, it would be very easy to scale our infrastructure up to meet it by adding a few more nodes to the    
      cluster. Neat!

### Kubernetes Backend
Kubernetes can be primarily viewed as its specification as a server-side API. The way we implement that API can vary depending on which Kubernetes backend we choose.
- We will be using [K3s](https://k3s.io/), a very light-weight but fully-fledged Kubernetes backend for deploying our cluster.
    - I will not go too in-depth here about the differences between different Kubernetes backends, but K3s gives us some unique advantages in that it natively supports creating load balancers and externally exposing them without using a tool like MetalLB. This feature will prove important for us.
- We are also going to be using two tools called [Helm](https://helm.sh/) and [Kustomize](https://kustomize.io/) for organizing our Kubernetes Manifests before we deploy them. I will not go into details on how these work, but you can think of them as templaters for our manifest files.

# Part 2: Application Component Specifications

Here we will attempt to define smaller components within our overall system that will each serve an important individual    
function, as well as have a role in the flow of information through our game system. These components are the <b>    
gameserver</b> app, the <b>matchmaker</b> app, the <b>shared</b> library, and the <b>frontend</b> app.

## Terminology

First we need some shared terminology between the application components:

- <b>Room</b>: This is what we call a game between players both in our UI, and often internally.
    - When a player launches our app, they would not be in a "room" until they either created one to invite other    
      players into, or were invited into one themselves.
    - Players may exist in a room while either playing the game, or waiting for others to join.
- <b>Game State</b>: Every room/game has a state:
    - READY: The room has no players joined, it is waiting for someone to call /create-game.
    - WAITING: The room has at least one player, but the game hasn't started. Other players can join
    - PLAYING: The players in the room are actively playing the game. No one can join.
    - <i>Note that these do not correspond to Agones's GameServer states, and are only used internally be our game    
      logic.</i>

## GameServer

This project is meant to represent a <b>single</b> game between however many players.    
If we have multiple games running, each will get their own instance of our running GameServer application.

A full explanation of the logic behind the GameServer application may be provided in a different document, and can be    
better understood by reading through its code. What follows is a much higher-level explanation.

### Functional Specification:

- Players (clients) should be able to form a direct connection via <b>Web Sockets</b> to our game server application.
- Both the client and the server should be able to send messages at will over the socket, and choose to handle receiving    
  messages however they want.
- (Smaller specification: The GameServer must tell the Agones Controller Service that it is "Ready" once it has started    
  up, so that the Agones Allocator Service can use it)
    - This detail should not concern us within the game server, as its logic should be completely oblivious to the fact    
      that it exists in a set of multiple gameservers.
- Gameservers should shutdown once the game is over.
    - We actually just send a `shutdown` signal to the Agones Controller Service, and it figures out how to manage the lifecycle of our pod.

### Implementation

- The GameServer application is written in Spring Boot so that we can utilize its various libraries for accomplishing    
  its function.
- The key detail of our implementation that is not vaguely specified by the functional requirements is the existence of    
  our <b>event based message handling system</b>.
    - Upon receiving a message over the websocket from the client, the gameserver will <i>fire</i> an event    
      corresponding to the message type we have receiving, with a payload that relates to what the client is telling us.
    - This system will be annotation based, and allow us for easy handling of messages anywhere within the gameserver    
      project.
- The GameServer should also store its information (like port and IP address) in a shared datastore (which will be redis). The reason for this will make sense once we get to the Matchmaker.

### Components

- <b>Agones</b> Designed to fulfill the final specification only.
- <b>Game Sockets</b>: A wrapper around the spring web socket library that handles opening and communicating over    
  sockets with our clients.
    - This is also responsible for firing the events we have specified
- <b>Redis</b>: Another wrapper around the spring redis library that is able to both store and lookup information on our    
  gameservers or clients that we store in redis.

## Matchmaker

This projects goal is to answer the question: <b>If a client wants to play our game (and start a new room), how does it    
know which GameServer in our backend to connect to?</b>

Our matchmaker will be responsible for taking information that Agones gives us on our GameServer Fleets, and using it to direct incoming clients to different game servers.

The matchmaker serves as the first point-of-contact for clients: Before they can join a gameserver, they must ask the matchmaker which one to join. Then their session information will be stored in the matchmaker's shared database with all the gameservers for later session validation.

- On the Matchmaker, we create a GRPC stub that is capable of accessing the [Agones Allocator Service](https://agones.dev/site/docs/advanced/allocator-service/)
    - At any time, we can request from the service to give us a GameServer that is currently in READY state (unused, just waiting to be utilized). It gives us details like the resource name, port, and IP of this server. Then it updates its status to ALLOCATED, so that we know it is marked as in-use.
- Note that every gameserver stores in our shared database (redis) information on itself, like its port and IP. We can then use this as a lookup table.
- The matchmaker should also store information of each client that creates or joins a game in the shared database with the gameservers (redis), so that gameservers can independently verify that clients are allowed to join them before they actually do.

### Functional Specification:
We have four endpoints:
- `/create-game`: Provided a client ID, give me an un-allocated, fresh gameserver that has no one connected to it. Mark it as allocated, add my client ID to the redis database, stating that I am allowed to connect to it. Then give me back the server's port and IP address
- `/join-game`: Provided a room code for a game server, give me the port and IP address of that game server. Also mark in the redis database that I am allowed to connect to it.
- `/list-games`: Give me a list of all the room codes of game servers that are currently WAITING on players.
- `/autoscale`: This one is beyond the scope of this design document, but in essence is responsible for communication between the Agones Controller Service and the matchmaker, and determines how we will autoscale our GameServer fleet.

### Implementation
Implementation will be done using the Spring boot Tomcat server with basic REST endpoint controllers.

## Redis
We have had a lot of foreshadowing of this tool. Lets introduce it for real:
- Redis is an In-memory datastore that is very fast, and uses a key-value storage system.
- While we may have multiple gameservers or matchmakers, we will only have one redis datastore.
- What we store in redis is as follows:
    - For each gameserver currently running, we store a key corresponding to its room code, and a value of its gameserver info (port, IP, etc)
    - For each player currently authorized to connect to a gameserver, we store a key corresponding to its client UUID and a value of the room code of its gameserver.

This way, we can use redis for two things:
- Matchmaker can now access a list of all the gameservers that are currently running. This is useful for the `/join-game` endpoint.
- Gameservers can now validate that a client is allowed to connect to them (their session ID must be in the database).
    - This is an extremely simple layer of authentication which is enough for our purposes, but would not fly in a real corporate environment.

## Frontend

This design document will not at all be concerned with the Java Swing implementation of our frontend, and just the implementation of connection to our backend.

### Function Specification
- The client should be able to send GET/POST requests to our matchmaker backend hosted in the cluster, and retrieve gameserver information using it.
- Then, the client should be able to open a websocket using the information given to it by the matchmaker, to connect to a gameserver.
- The client should be able to send protobuf messages over this connection to the gameserver, and also parse messages received from the gameserver back into protobuf messages.

### Implementation
- We will use the same spring boot websocket library for the client sockets, and the spring boot web client for making REST requests.

## Shared

This is a very small module on the same level as the Matchmaker, GameServer, and Frontend that has a shared set of classes that they may need to use.

Most of this is stuff like the POJOs we generate using swagger, and the protoc compiled java classes for our messages.

Some other common models may also be stored in this module.

# Part 3: Client-Server Communication

- We have one file called `game.proto` that specifies what client-server messaging should look like.
- Each message is prefixed with "Cb" for "client-bound" or "Sb" for "server-bound".
- The server-side handler will take in Cb messages and send them to all connected clients, and the client-side handler will take in Sb messages and send them to its gameserver.
- The server-side handler will receive and parse Sb messages for the game logic to handle, while the client-side handler will receive and parse Cb messages for the game logic to handle.
- All of this is done over websockets.

## List of Messages
### Server-bound
- `SbStartGame`: sent when the player presses the "start game button"
    - Marks this gameserver as PLAYING (rather than WAITING) and prevents further players from joining.
    - The game logic can handle this as needed.
- `SbGuess`: sent when the player guesses a word during their turn
    - I will not go into the details of how we intend to handle this on each end since this document is not concerned with game logic implementation
- `SbGuessUpdate`: sent every time the player types a letter
    - I will not go into the details, but this is necessary since we want other players to be able to see your guess as you are typing it.
### Client-bound
- `CbGameStart`: sent when someone presses the start game button and the game begins
- `CbGameEnd`: sent when the game ends
- `CbUpdatePlayerList`: sent whenever a player joins/leaves, and whenever player scores update
- `CbTimerTick`: sent every second to decrement the game timer
- `CbGuessResult`: sent in response to the `CbGuessWord`, includes the result (true/false) of their guess
- `CbGuessUpdate`: relayed back to all clients when we receive a `SbGuessUpdate`
- `CbNewTurn`: sent to the client when the player whose turn it is switches

# Part 4: Putting It All Together

## Enter: K8s and Agones

We have discussed what Kubernetes and Agones can do for us. Now we just need to deploy them in our cluster.

- As mentioned previous, we will use K3s as our backend.
- We will deploy Agones into the cluster using Helm
    - This includes the Agones Allocator and Agones Controller services for manage gameserver fleet
- We will deploy our Matchmaker as a ReplicaSet, with a HorizontalPodAutoscaler to accompany it.
    - This way our matchmaker can dynamically scale up when needed
- We deploy redis in the cluster as well
- We will deploy our game servers using the Agones Fleet CRD
- We will hide the Matchmaker behind an NGINX ingress controller endpoint to manage requests to it
- We will deploy custom network policies to define what access is allowed in our cluster.
-  We will create separate kustomize stacks for DEV and PRD environments
- These can specify different scaling targets and resource limits

## Overall Flow
Now the flow of traffic can look like this:
- Requests sent to the cluster on certain endpoints (like create-game) can be targeted to the matchmaker
    - Matchmaker can allocate servers to those requesting them using the Agones Allocator Service
    - Matchmaker returns to the client information about the gameserver (port, IP) that they can connect to
- Using Agones' dynamic port allocation, the client can connect directly to a gameserver via NodePort on the host node.
- The client can then communicate via spring websockets to the gameserver
    - Messages sent to the gameserver are serialized and deserialized using Protobuf messages
- Persistent data will be stored in redis for sharing between the matchmaker and the gameserver

Here is a diagram:

![backend lucid](https://github.com/user-attachments/assets/832dc8aa-e047-4f3e-afe5-84b4ec7b392b)

# Part 5: What Now?
Okay! We've addressed a lot of the design factors.

As a reminder, this document has covered:
- An architectural overview of how we intend to communicate between various components of our application
- How we will deploy our application
- How we will manage our application

What is left for us to do is:
- The game logic that handles every message in the game
- The frontend component that displays our game
- Testing

If you've actually read everything, let me know! I'm glad I'm not alone.
