import { vi } from 'vitest';
import 'vitest-localstorage-mock';

const canvasContextStub = new Proxy({}, { get: () => vi.fn(), set: () => true });

HTMLCanvasElement.prototype.getContext = vi.fn(() => canvasContextStub) as unknown as HTMLCanvasElement['getContext'];
