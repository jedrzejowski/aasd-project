import crypto from "crypto";
import dat_defaults from "dat-swarm-defaults";
import getPort from "get-port";
import swarm, {Options as SwarmOptions, Swarm} from "discovery-swarm";

const CHANNEL_PREFIX = "AASD_PROJECT_2020Z_";

export default class MeshNode {
    public readonly myId = crypto.randomBytes(32)
    private myPort: number = -1;
    private swarm: Swarm;
    private config: SwarmOptions;

    constructor() {

        this.config = dat_defaults({
            id: this.myId,
        });


        this.swarm = swarm(this.config);

        this.initNode();
    }

    private async initNode() {
        this.myPort = await getPort();
        this.swarm.listen(this.myPort)
        this.swarm.join('TO JEST MÓJ CHANNEL');

        this.swarm.on('connection', (conn, info) => {
            console.log(info.id.toString('hex'));
        });
    }

}

