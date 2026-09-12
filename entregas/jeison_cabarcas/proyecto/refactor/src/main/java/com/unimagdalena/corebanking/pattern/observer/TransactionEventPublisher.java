package com.unimagdalena.corebanking.pattern.observer;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventPublisher {

	private final List<TransactionObserver> observers;

	public TransactionEventPublisher(List<TransactionObserver> observers) {
		this.observers = observers;
	}

	public void publish(TransactionCompletedEvent event) {
		observers.forEach(observer -> observer.onTransactionCompleted(event));
	}
}
