package cz.jskrabal.proxy.pump


import com.nhaarman.mockito_kotlin.*
import io.vertx.core.Handler
import io.vertx.core.streams.ReadStream
import io.vertx.core.streams.WriteStream
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Created by janskrabal on 08/07/16.
 */
class DataPumpTest {

    @Mock
    private lateinit var input: ReadStream<String>

    @Mock
    private lateinit var output: WriteStream<String>

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(input, output)
    }

    @Test
    fun dataPumpTest() {
        DataPump(input, output) { _ -> }.start()
        val inputHandler = getInputHandler(input)

        inputHandler.handle(STRING_DATA)

        verify(input).handler(any())
        verify(output).write(STRING_DATA)
        verify(output).writeQueueFull()
    }

    @Test
    fun dataPumpWithFullWriteQueueTest() {
        whenever(output.writeQueueFull()).thenReturn(true)
        DataPump(input, output) { _ -> }.start()

        val inputHandler = getInputHandler(input)
        inputHandler.handle(STRING_DATA)

        verify(input).handler(any())
        verify(input).pause()
        verify(output).drainHandler(any())
        verify(output).write(STRING_DATA)
        verify(output).writeQueueFull()
    }

    @Test
    fun dataPumpInputRestoredAfterOutputDrainedTest() {
        whenever(output.writeQueueFull()).thenReturn(true)
        DataPump(input, output) { _ -> }.start()

        val inputHandler = getInputHandler(input)

        inputHandler.handle(STRING_DATA)

        verify(input).handler(any())
        verify(input).pause()
        verify(output).drainHandler(any())

        whenever(output.writeQueueFull()).thenReturn(false)
        getDrainHandler(output).handle(null)
        verify(input).resume()

        inputHandler.handle(STRING_DATA)
        verify(output, times(2)).write(STRING_DATA)
        verify(output, times(2)).writeQueueFull()
    }

    private fun getInputHandler(stream: ReadStream<String>): Handler<String> {
        return argumentCaptor<Handler<String>>().apply {
            verify(stream).handler(capture())
        }.firstValue
    }

    private fun getDrainHandler(stream: WriteStream<String>): Handler<Void> {
        return argumentCaptor<Handler<Void>>().apply {
            verify(stream).drainHandler(capture())
        }.firstValue
    }

    companion object {
        private val STRING_DATA = "STRING_DATA"
    }
}
