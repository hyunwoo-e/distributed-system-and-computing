package server;

public class PassiveQueue<E> {
	GenericQueue<E> queue = new GenericQueue<E>();

	public synchronized void accept(E m) {
		queue.enqueue(m);
		notify();
	}

	public synchronized E release() {
		for (;;) {
			if (queue.isEmpty()) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.getStackTrace();
				}
			} else {
				return queue.dequeue();
			}
		}
	}
}
