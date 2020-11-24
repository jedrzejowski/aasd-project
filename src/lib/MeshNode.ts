import {Socket} from "net";
import {SwarmInfo} from "discovery-swarm";
import {NodeMeta} from "../types/NodeMeta";
import Json32Socket from "./Json32Socket";
import crypto from "crypto";
import noop from "./noop";
import TypedEvent from "./TypedEvent";
import {MeshAnswerI, MeshAnswerY, MeshQuestionI, MeshQuestionY} from "./mesh_msg_types";
import type MeshGate from "./MeshGate";

export const CHANNEL_PREFIX = "AASD_PROJECT_2020Z_";

export default class MeshNode {
    public readonly id;
    public readonly info: SwarmInfo;
    private socket: Socket;
    private jsonSocket: Json32Socket;
    private meshGate: MeshGate | null;

    readonly answerEvent = new TypedEvent<(answer: MeshAnswerI) => void>();
    readonly questionEvent = new TypedEvent<(question: MeshQuestionI) => void>();

    constructor(meshGate: MeshGate, socket: Socket, info: SwarmInfo) {
        this.meshGate = meshGate;
        this.info = info;
        this.socket = socket;
        this.jsonSocket = new Json32Socket(socket);

        this.id = info.id.toString("hex");

        this.jsonSocket.messageEvent.on(msg => {
            if (MeshQuestionY.isValidSync(msg)) {
                this.questionEvent.emit(msg);
            } else if (MeshAnswerY.isValidSync(msg)) {
                this.answerEvent.emit(msg);
            } else {
                this.close();
            }
        });

        this.questionEvent.on(async question => {
            try {
                const value = await this.handleQuestion(question);

                this.sendMsg({
                    answerId: question.questionId,
                    type: "resolve",
                    value: value
                });
            } catch (e) {
                this.sendMsg({
                    answerId: question.questionId,
                    type: "reject",
                    value: e
                })
            }
        });
    }

    close() {
        this.socket.destroy();
        this.meshGate = null;
    }

    private ask(name: string, ...args: any[]): Promise<unknown> {
        return new Promise((resolve, reject) => {

            const id = crypto.randomBytes(32).toString("hex");

            const timeout = setTimeout(() => {
                if (this.socket.destroyed) {
                    return;
                }

                dispose();
                reject(new Error("timeout"));
                reject = resolve = noop;
            }, 10000);

            const {dispose} = this.answerEvent.on(answer => {
                if (answer.answerId === id) {

                    clearTimeout(timeout);
                    dispose();

                    if (answer.type === "resolve") {
                        resolve(answer.value);
                    } else {
                        reject(answer.value);
                    }

                    reject = resolve = noop;
                }
            });

            this.jsonSocket.write({questionId: id, name, args});
        });
    }

    private sendMsg(msg: MeshQuestionI | MeshAnswerI) {
        this.jsonSocket.write(msg);
    }

    private async handleQuestion(question: MeshQuestionI): Promise<any> {
        switch (question.name) {
            case "getMeta": {
                return this.meshGate?.meta;
            }

            default:
                throw new Error("unknown question");
        }
    }

    private metaCache: Promise<NodeMeta> | undefined;

    public getMeta(): Promise<NodeMeta> {
        if (this.metaCache) {
            return this.metaCache;
        }

        return this.metaCache = this.ask("getMeta");
    }

}


