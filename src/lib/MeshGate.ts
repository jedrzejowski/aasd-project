import crypto from "crypto";
import dat_defaults from "dat-swarm-defaults";
import getPort from "get-port";
import swarm, {Options as SwarmOptions, Swarm} from "discovery-swarm";
import {NodeMeta} from "../types/NodeMeta";
import {discoveryRoomName, petrolRoomName} from "./roomNames";
import MeshNode from "./MeshNode";
import TypedEvent from "./TypedEvent";

export const CHANNEL_PREFIX = "AASD_PROJECT_2020Z_";

export default class MeshGate {
    public readonly myId = crypto.randomBytes(32)
    private myPort: number = -1;
    public readonly swarm: Swarm;
    public readonly config: SwarmOptions;
    public readonly meta: NodeMeta;
    public readonly peers: Record<string, MeshNode | undefined> = {};

    public readonly newPeerEvent = new TypedEvent<(mechNode: MeshNode) => void>()

    constructor(meta: NodeMeta, cb?: (error: any) => void) {

        this.meta = meta;

        this.config = dat_defaults({
            id: this.myId,
        });

        this.swarm = swarm(this.config);

        this.initNode().then(cb).catch(cb);
    }

    private async initNode() {
        this.myPort = await getPort();
        this.swarm.listen(this.myPort)

        this.swarm.on("connection", (conn, info) => {
            const node = new MeshNode(this, conn, info);
            this.peers[node.id] = node;

            this.newPeerEvent.emit(node);

            conn.on("close", () => {

                if (this.peers[node.id] === node) {
                    delete this.peers[node.id]
                }
            })
        });
    }

    async joinRoom(key: string): Promise<void> {
        return new Promise((resolve, reject) => {
            this.swarm.join(key, {}, () => resolve());
        });
    }

    leaveRoom(key: string) {
        this.swarm.leave(key);
    }

    joinPetrol(petrolStationId: string) {
        return this.joinRoom(petrolRoomName(petrolStationId));
    }

    joinDiscovery(cord: { lat: number, lng: number }) {
        return this.joinRoom(discoveryRoomName(cord));
    }
}
