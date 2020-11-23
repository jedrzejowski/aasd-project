import {CHANNEL_PREFIX} from "./MeshNode";

export function petrolRoomName(petrolStationId: string): string {
    return [CHANNEL_PREFIX, 'PETROL_STATION', petrolStationId].join('_')
}

export function discoveryRoomName(cord: { lat: number, lng: number }): string {
    return [CHANNEL_PREFIX, 'DISCOVERY'].join('_')
}
