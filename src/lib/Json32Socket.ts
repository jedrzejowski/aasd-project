import {Socket} from "net";
import TypedEvent from "./TypedEvent";
import Deferred from "./Deferred";

export default class Json32Socket {

    private _msgBuffer: string | null = null;
    private _msgLeft: number = 0;

    readonly errorEvent = new TypedEvent<(err: any) => void>();
    readonly messageEvent = new TypedEvent<(message: unknown) => void>();
    readonly socket: Socket;

    public nextMessage = new Deferred<unknown>();

    constructor(socket: Socket) {

        this.socket = socket;

        let read = (chunk: Buffer) => {
            try {
                this._onData(chunk);
            } catch (e) {
                this.errorEvent.emit(e)
            }
        };
        this.socket.on("data", read);
        this.socket.on("end", () => {
            this.socket.off("data", read);
        });

        this.messageEvent.on(body => {
            const currentMessage = this.nextMessage;
            this.nextMessage = new Deferred<unknown>();

            currentMessage.resolve(body);
        });

        this.errorEvent.on(error => {
            const currentMessage = this.nextMessage;
            this.nextMessage = new Deferred<unknown>();

            currentMessage.reject(error);
        });
    }

    private _onData(chunk: Buffer) {

        let offset = 0;

        while (offset < chunk.length) {

            if (this._msgBuffer === null) {
                this._msgBuffer = "";
                this._msgLeft = chunk.readUInt32LE(offset);
                offset += 4;
            }

            let toRead = Math.min(chunk.length - offset, this._msgLeft);
            this._msgBuffer += chunk.toString('utf8', offset, offset + toRead);
            offset += toRead;
            this._msgLeft -= toRead;

            if (this._msgLeft === 0) {

                this.messageEvent.emit(JSON.parse(this._msgBuffer));

                this._msgBuffer = null;
            }
        }
    }

    write(data: any) {

        let buf_data = Buffer.from(JSON.stringify(data));
        let buf_size = Buffer.alloc(4);
        buf_size.writeUInt32LE(buf_data.length, 0);

        this.socket.write(buf_size);
        this.socket.write(buf_data);
    }

}

