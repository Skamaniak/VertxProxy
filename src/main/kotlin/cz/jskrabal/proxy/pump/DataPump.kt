package cz.jskrabal.proxy.pump

import io.vertx.core.streams.Pump
import io.vertx.core.streams.ReadStream
import io.vertx.core.streams.WriteStream
import java.util.function.Consumer

/**
 * Created by janskrabal on 01/07/16.
 */
class DataPump<T> private constructor(private val input: ReadStream<T>,
                                      private val output: WriteStream<T>,
                                      private inline val interceptor: Consumer<T>) : Pump {

    private var pumped: Int = 0

    override fun start(): Pump {
        input.handler { data ->
            interceptor.accept(data)

            output.write(data)
            if (output.writeQueueFull()) {
                input.pause()
                output.drainHandler { input.resume() }
            }
        }
        return this
    }

    override fun stop(): Pump {
        output.drainHandler(null)
        input.handler(null)
        return this
    }

    override fun setWriteQueueMaxSize(maxSize: Int): Pump {
        this.output.setWriteQueueMaxSize(maxSize)
        return this
    }

    override fun numberPumped(): Int {
        return pumped
    }

    companion object {
        fun <T> create(input: ReadStream<T>, output: WriteStream<T>): Pump {
            return Pump.pump(input, output)
        }

        fun <T> create(input: ReadStream<T>, output: WriteStream<T>, interceptor: Consumer<T>): Pump {
            return DataPump(input, output, interceptor)
        }
    }
}


