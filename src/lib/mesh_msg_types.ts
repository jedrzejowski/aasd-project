import {array, InferType, mixed, object, string} from "yup";
import {hex_id_regex} from "../regex";

export const MeshQuestionY = object({
    questionId: string().matches(hex_id_regex).defined().required(),
    name: string().defined().required(),
    args: array().of(mixed().defined()).ensure()
}).defined();

export type MeshQuestionI = InferType<typeof MeshQuestionY>;

export const MeshAnswerY = object({
    answerId: string().matches(hex_id_regex).defined().required(),
    type: string().oneOf(["reject", "resolve"]).defined().required(),
    value: mixed(),
}).defined();

export type MeshAnswerI = InferType<typeof MeshAnswerY>;