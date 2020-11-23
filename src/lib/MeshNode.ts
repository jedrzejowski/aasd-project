import {Socket} from "net";
import {SwarmInfo} from "discovery-swarm";
import {NodeMeta} from "../types/NodeMeta";
import Json32Socket from "./Json32Socket";
import crypto from "crypto";
import noop from "./noop";

export const CHANNEL_PREFIX = "AASD_PROJECT_2020Z_";

export default class MeshNode {
    public readonly id;
    public readonly info: SwarmInfo;
    private socket: Socket;
    private jsonSocket: Json32Socket;

    constructor(socket: Socket, info: SwarmInfo) {

        this.info = info;
        this.id = info.id.toString("hex");

        this.socket = socket;
        this.jsonSocket = new Json32Socket(socket);
    }

    private metaCache: Promise<NodeMeta> | undefined;

    public sendMessage(name: string, ...args: any[]) {
        return new Promise((resolve, reject) => {

            const messageId = crypto.randomBytes(32).toString("hex");

            this.jsonSocket.write({messageId, name, args});

            setTimeout(() => {
                reject(new Error("timeout"));
                reject = resolve = noop;
            }, 1000);

            const {dispose} = this.jsonSocket.messageEvent.on((answer: any) => {
                if (answer.messageId === messageId) {
                    resolve(answer.answer);
                    reject = resolve = noop;
                    dispose();
                }
            });
        });
    }

    public getMeta(): Promise<NodeMeta> {
        if (this.metaCache) {
            return this.metaCache;
        }

        return this.metaCache = this.sendMessage("getMeta");
    }

}
