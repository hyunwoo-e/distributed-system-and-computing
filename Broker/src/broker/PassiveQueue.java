package broker;

public class PassiveQueue<E> {
	GenericQueue<E> queue = new GenericQueue<E>();

	public synchronized void accept(E r) {
		queue.enqueue(r);
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
