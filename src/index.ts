import {NodeType} from "./types/NodeMeta";
import MeshGate from "./lib/MeshGate";

const mesh_node = new MeshGate({
    petrolStationId: "shell1",
    type: NodeType.PetrolStation,
    name: "Shell",
    address: "Puławska 437, 02-801 Warszawa",
    coords: {
        lng: 52.140240,
        lat: 21.018811,
    },
    petrolAvailable: ["Diesel", "LPG", "PB95", "PB98"]
}, () => {
    mesh_node.joinDiscovery({lat: 0, lng: 0})
});


console.log(`my id = ${mesh_node.myId.toString("hex")}`);

mesh_node.newPeerEvent.on(async peer => {
    console.log(peer.id, await peer.getMeta());
})

