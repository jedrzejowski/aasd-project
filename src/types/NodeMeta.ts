import {array, InferType, number, object, string} from "yup";

export enum NodeType {
    PetrolStation = "petrol_station",
    NormalClient = "normal_client",
}

export const PetrolStationY = object({
    petrolStationId: string().required().defined(),
    type: string<NodeType.PetrolStation>().oneOf([NodeType.PetrolStation]).defined(),
    name: string().defined().required(),
    address: string().defined().required(),
    coords: object({
        lat: number().min(-90).max(90),
        lng: number().min(-180).max(180),
    }).defined().required(),
    petrolAvailable: array().of(string()).defined().required()
}).defined().required();

export const NormalClientY = object({
    type: string<NodeType.NormalClient>().oneOf([NodeType.NormalClient]).defined(),
}).defined().required();

export type NodeMeta =
    | InferType<typeof PetrolStationY>
    | InferType<typeof NormalClientY>
    ;