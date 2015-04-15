package repicea.serial;

import java.util.ArrayList;

@SuppressWarnings("serial")
class MemorizedArray extends ArrayList<MemorizerPackage> implements MemorizedRegistrable {

	private MemorizerWorker backupWorker;
	private final ArrayList<MemorizerPackage> backupList;
	
	private final Object lock = new Object();
	private int currentIndex;
	private boolean isCopying;
	
	protected MemorizedArray() {
		backupList = new ArrayList<MemorizerPackage>();
//		backupWorker = new MemorizerWorker("Backup Memorizer Thread", this);
//		backupWorker.start();
	}

	
	@Override
	public void clear() {
		lockMemorizerArray();
		super.clear();
		backupList.clear();
	}
	
	@Override
	public void add(int index, MemorizerPackage mp) {
		lockMemorizerArray();
		backupList.add(index, mp);
//		super.add(index, mp);
		backupThisMemorizerPackage(index, mp);		
	}
	
	private void backupThisMemorizerPackage(int index, MemorizerPackage mp) {
		currentIndex = index;
		isCopying = true;
		backupWorker.addToQueue(mp);
	}
	
	@Override
	public MemorizerPackage get(int index) {
		lockMemorizerArray();
		MemorizerPackage toBeRetrieve = super.get(index);		// avoid writing the new clone over the former one before it is sent back to the Memorizable instance
		backupThisMemorizerPackage(index, backupList.get(index));		
		return toBeRetrieve;
	}

	
	@Override
	public MemorizerPackage remove(int index) {
		lockMemorizerArray();
		backupList.remove(index);
		return super.remove(index);
	}
	
	
	@Override
	public void registerMemorizerPackage(MemorizerPackage mp) {
		if (currentIndex == size()) {
			super.add(currentIndex, mp);
		} else {
			super.set(currentIndex, mp);
		}
		synchronized(lock) {
			isCopying = false;
			lock.notify();
		}
	}

	private void lockMemorizerArray() {
		synchronized(lock) {
			while (isCopying) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} 
	}

	@Override
	public void setMemorizerWorkerEnabled(boolean enabled) {
		if (backupWorker == null || !backupWorker.isAlive()) {
			if (enabled) {
				backupWorker = new MemorizerWorker("Backup Memorizer Thread", this);
				backupWorker.start();
			}
		} else if (backupWorker != null) {
			if (!enabled) {
				backupWorker.addToQueue(MemorizerWorker.ShutDownMemorizerPackage);
			}
		}
	}
	
}
