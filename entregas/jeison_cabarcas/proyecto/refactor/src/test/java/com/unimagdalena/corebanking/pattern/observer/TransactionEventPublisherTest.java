package com.unimagdalena.corebanking.pattern.observer;

import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionEventPublisherTest {

	@Mock
	private TransactionObserver firstObserver;

	@Mock
	private TransactionObserver secondObserver;

	@Test
	void publishNotifiesAllRegisteredObservers() {
		TransactionEventPublisher publisher = new TransactionEventPublisher(List.of(firstObserver, secondObserver));
		TransactionCompletedEvent event = TransactionCompletedEvent.builder().build();

		publisher.publish(event);

		verify(firstObserver).onTransactionCompleted(event);
		verify(secondObserver).onTransactionCompleted(event);
	}
}
