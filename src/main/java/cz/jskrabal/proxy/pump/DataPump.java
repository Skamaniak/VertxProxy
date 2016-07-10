package cz.jskrabal.proxy.pump;

import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

import java.util.function.Consumer;

/**
 * Created by janskrabal on 01/07/16.
 */
public class DataPump<T> {
	private final ReadStream<T> input;
	private final WriteStream<T> output;
	private final Consumer<T> interceptor;

	public DataPump(ReadStream<T> input, WriteStream<T> output, Consumer<T> interceptor) {
		this.input = input;
		this.output = output;
		this.interceptor = interceptor;
	}

	public void start(){
		input.handler(data -> {
			interceptor.accept(data);

			output.write(data);
			if(output.writeQueueFull()) {
				input.pause();
				output.drainHandler(v -> input.resume());
			}
		});
	}
}
