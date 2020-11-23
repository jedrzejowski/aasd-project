//https://github.com/basarat/typescript-book/blob/master/docs/tips/typed-event.md


export interface Disposable {
    dispose(): void;
}

export default class TypedEvent<T extends (...args: any[]) => void = () => void> {
    private listeners: T[] = [];
    private listenersOnce: T[] = [];


    on(listener: T): Disposable {
        this.listeners.push(listener);
        return {
            dispose: () => this.off(listener)
        };
    }

    once(listener: T): void {
        this.listenersOnce.push(listener);
    }

    off(listener: T) {

        let index = this.listeners.indexOf(listener);
        if (index > -1) this.listeners.splice(index, 1);

        index = this.listenersOnce.indexOf(listener);
        if (index > -1) this.listenersOnce.splice(index, 1);
    }

    emit(...args: Parameters<T>) {

        if (this.listeners.length > 0) {
            for (let f of this.listeners) f(...args);
        }

        if (this.listenersOnce.length > 0) {
            for (let f of this.listenersOnce) f(...args);
            this.listenersOnce = [];
        }
    }

    pipe(te: TypedEvent<T>): Disposable {
        // @ts-ignore
        return this.on((...args: Parameters<T>) => {
            te.emit(...args)
        });
    }

    oncePromise(): Promise<Parameters<T>> {
        return new Promise(resolve => {
            // @ts-ignore
            this.listenersOnce.push((...args) => resolve(args));
        });
    }
}