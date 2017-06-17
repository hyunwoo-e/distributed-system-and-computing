package server;

public class PassiveQueue<E> {
	GenericQueue<E> queue = new GenericQueue<E>();
	private boolean shouldStop = false;

	public synchronized void accept(E m) {
		queue.enqueue(m);
		notify();
	}

	public synchronized E release() {
		while(!shouldStop) {
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
		return null;
	}
	
	public synchronized void destroy() {
		shouldStop = true;
		notify();
	}
}
