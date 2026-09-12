package com.unimagdalena.corebanking.pattern.observer;

public interface TransactionObserver {

	void onTransactionCompleted(TransactionCompletedEvent event);
}
