import * as React from "react";
import {ChatApi} from "../../target/ts/websocket-services";
import {FormEvent} from "react";

interface Message {
    content: string;
    from: string;
    to?: string;
    isPrivate: boolean;
}

interface Props {
    username: string
    closeChat: () => void;
}

export function Chat({username, closeChat}: Props) {
    const [messages, setMessages] = React.useState<Message[]>([]);
    const [users, setUsers] = React.useState<string[]>([]);
    const [enteredMessage, setEnteredMessage] = React.useState<string>("");
    const [privateMessageRecipient, setPrivateMessageRecipient] = React.useState<string | undefined>();
    const bottomOfMessages = React.useRef<HTMLDivElement>(null);

    const ws: ChatApi = React.useMemo(() => new ChatApi({onOpen, onClose: closeChat}), [])

    function onOpen() {
        ws.onInitialJoin(username, ({users, messages}) => {
            setUsers(users)
            setMessages(messages.map(m => ({...m, isPrivate: false})))
        })
        ws.onMessage((message, sender) => addMessage({content: message, from: sender, to: username, isPrivate: false}))
        ws.onPrivateMessage(username, (message, sender) => addMessage({content: message, from: sender, to: username, isPrivate: true}))
        ws.onUserJoined(user => setUsers(users =>[...users, user]))
        ws.onUserLeft(user => setUsers(users => users.filter(u => u !== user)))
        ws.join(username)
    }

    function addMessage(message: Message) {
        setMessages(messages => [...messages, message])
    }

    React.useEffect(() => {
        bottomOfMessages.current?.scrollIntoView({ behavior: 'smooth' })
    }, [messages])

    React.useEffect(() => {
        const heartbeat = () => ws.heartbeat()
        const interval = setInterval(heartbeat, 60 * 1000)
        return () => clearInterval(interval)
    }, [])

    function sendMessage(e: FormEvent) {
        e.preventDefault()
        if (privateMessageRecipient) {
            ws.sendPrivate(privateMessageRecipient, enteredMessage)
        } else {
            ws.send(enteredMessage)
        }
        setEnteredMessage("")
    }

    function togglePrivateRecipient(user: string) {
        if (user === privateMessageRecipient) {
            setPrivateMessageRecipient(undefined)
        } else {
            setPrivateMessageRecipient(user)
        }
    }

    return <div className={"flex flex-wrap content-between bg-blue w-[400px] h-[400px]"}>
        <div className={"flex-grow-[4] max-h-80 overflow-y-scroll"}>
            {messages.map((message, i) => <div key={i} className={message.isPrivate ? "italic" : ""}>
                {message.isPrivate
                    ? `${message.from} to ${message.to}: ${message.content}`
                    : `${message.from}: ${message.content}`
                }
            </div>)}
            <span ref={bottomOfMessages}/>
        </div>
        <div className={"flex-grow-[1] max-h-80 overflow-y-scroll "}>
            <h2 className={"font-bold"}>Users</h2>
            {users.map(user => <div
                key={user}
                className={"cursor-pointer hover:bg-lightblue" + (privateMessageRecipient === user ? " bg-lightblue" : "")}
                onClick={() => togglePrivateRecipient(user)}
            >
                {user}
            </div>)}
        </div>
        <div className={"w-full p-4"}>
            <form onSubmit={sendMessage} className={"flex justify-center"}>
                <input className={"flex-grow-[4]"}
                       type={"text"}
                       value={enteredMessage} placeholder={"Send a message"}
                       onChange={e => setEnteredMessage(e.target.value)}/>
                <button className={"flex-grow-[1]"}
                        disabled={!enteredMessage}
                        type={"submit"}>
                    {privateMessageRecipient ? `Send to ${privateMessageRecipient}` : "Send"}
                </button>
            </form>
        </div>
    </div>
}