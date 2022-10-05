import * as React from "react";
import * as ReactDOM from "react-dom/client";
import {Chat} from "./Chat";

function UsernameForm({setUsername}: { setUsername: (username: string) => void }) {
    const [input, setInput] = React.useState<string>("")

    function onSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setUsername(input);
    }

    return <div className={"w-[400px] h-[400px] flex items-center justify-center bg-blue"}>
        <form onSubmit={onSubmit}>
            <input id="username-input" type={"text"} name={"username"} placeholder={"Username"} onChange={e => setInput(e.target.value)}/>
            <button type={"submit"}>Go</button>
        </form>
    </div>
}

function App(): JSX.Element {
    const [username, setUsername] = React.useState<string>();

    return <main className={"max-w-lg bg-yellow p-4"}>
        <h1 className={"text-lg text-center font-bold"}>Live Chat with a slick API</h1>
        {!username
            ? <UsernameForm setUsername={setUsername} />
            : <Chat username={username} closeChat={() => setUsername(undefined)}/>
        }
    </main>
}

const chatDiv = document.querySelector<HTMLDivElement>("#chat")!!
ReactDOM.createRoot(chatDiv).render(<App />);