# kotlin-typescript-websocket-api
A demonstration of a tight WebSocket integration between a React Typescript frontend and a Kotlin backend

![Diagram describing how the system works](https://malcolmcrum.com/assets/websockets.png)

## Installation

First, ensure you have modern Java, Maven, and npm installed (tested with Java 18, Maven 3.5, and npm 8.18.0).

1. `mvn package` to build the TypeScript client
2. `cd frontend && npm install` to pull frontend dependencies (React and TypeScript)
3. `npm run build` to generate the CSS and JS, or `npm run watch` to monitor for changes
4. Run the server in your IDE, or at the command line: `mvn compile exec:java -Dexec.mainClass="com.malcolmcrum.typescriptwebsocketapi.demo.ServerKt"`
5. Visit http://localhost:8080 in multiple tabs to demonstrate the chat app.

## Modifying the interface

After adding an additional method to [ChatApi.kt](src/main/kotlin/com/malcolmcrum/typescriptwebsocketapi/demo/api/ChatApi.kt)
or [ChatEvents.kt](src/main/kotlin/com/malcolmcrum/typescriptwebsocketapi/demo/api/ChatEvents.kt), you can use these
immediately in your Kotlin code, and after running `mvn package` you can see the updated `target/ts/websocket-services.ts`
code and use the calls from your frontend.

## Technology used

The backend is written in [Kotlin](https://kotlinlang.org/) with the lovely to use
[Javalin web framework](https://javalin.io/) to handle WebSockets.
[Pebble](https://pebbletemplates.io/) renders our TypeScript client from a twig template, and
[typescript-generator](https://github.com/vojtechhabarta/typescript-generator) turns our Kotlin data classes
into TypeScript interfaces.

The frontend is written in [TypeScript](https://www.typescriptlang.org/) with [React](https://reactjs.org/),
using [esbuild](https://esbuild.github.io/) to transpile JS and [Tailwind](https://tailwindcss.com/) for CSS.
