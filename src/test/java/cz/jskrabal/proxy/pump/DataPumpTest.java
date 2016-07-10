package cz.jskrabal.proxy.pump;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

/**
 * Created by janskrabal on 08/07/16.
 */
public class DataPumpTest {
	private static final String STRING_DATA = "STRING_DATA";
	@Mock
	private ReadStream<String> input;

	@Mock
	private WriteStream<String> output;

	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void tearDown(){
		verifyNoMoreInteractions(input, output);
	}

	@Test
	public void dataPumpTest(){
		new DataPump<>(input, output, data -> {}).start();
		Handler<String> inputHandler = getInputHandler(input);

		inputHandler.handle(STRING_DATA);

		verify(input).handler(any());
		verify(output).write(STRING_DATA);
		verify(output).writeQueueFull();
	}

	@Test
	public void dataPumpWithFullWriteQueueTest(){
		when(output.writeQueueFull()).thenReturn(true);
		new DataPump<>(input, output, data -> {}).start();

		Handler<String> inputHandler = getInputHandler(input);
		inputHandler.handle(STRING_DATA);

		verify(input).handler(any());
		verify(input).pause();
		verify(output).drainHandler(any());
		verify(output).write(STRING_DATA);
		verify(output).writeQueueFull();
	}

	@Test
	public void dataPumpInputRestoredAfterOutputDrainedTest(){
		when(output.writeQueueFull()).thenReturn(true);
		new DataPump<>(input, output, data -> {}).start();

		Handler<String> inputHandler = getInputHandler(input);

		inputHandler.handle(STRING_DATA);

		verify(input).handler(any());
		verify(input).pause();
		verify(output).drainHandler(any());

		when(output.writeQueueFull()).thenReturn(false);
		getDrainHandler(output).handle(null);
		verify(input).resume();

		inputHandler.handle(STRING_DATA);
		verify(output, times(2)).write(STRING_DATA);
		verify(output, times(2)).writeQueueFull();
	}

	@SuppressWarnings("unchecked")
	private Handler<String> getInputHandler(ReadStream stream) {
		ArgumentCaptor<Handler> inputHandlerCaptor = ArgumentCaptor.forClass(Handler.class);
		verify(stream).handler(inputHandlerCaptor.capture());

		return inputHandlerCaptor.getValue();
	}

	@SuppressWarnings("unchecked")
	private Handler<Void> getDrainHandler(WriteStream stream) {
		ArgumentCaptor<Handler> drainHandlerCaptor = ArgumentCaptor.forClass(Handler.class);
		verify(stream).drainHandler(drainHandlerCaptor.capture());

		return drainHandlerCaptor.getValue();
	}
}
