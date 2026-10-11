import { vi } from 'vitest';
import 'vitest-localstorage-mock';

const canvasContextStub = new Proxy({}, { get: () => vi.fn(), set: () => true });

HTMLCanvasElement.prototype.getContext = vi.fn(() => canvasContextStub) as unknown as HTMLCanvasElement['getContext'];

HTMLDialogElement.prototype.showModal = vi.fn(function showModal(this: HTMLDialogElement) {
  this.setAttribute('open', '');
});
