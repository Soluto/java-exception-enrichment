package exceptionEnrichment;


import com.soluto.exceptionEnrichment.ExtraData;
import com.sun.javaws.exceptions.LaunchDescException;
import org.junit.Test;

import java.util.UUID;

import static com.soluto.exceptionEnrichment.ExtraData.*;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ExceptionEnrichmentTests {
    @Test
    public void enrichException_addExtraDataToExceptionWithoutExtraData() {
        try {
            throw enrichedException(new LaunchDescException(), ExtraData.create().with("testKey", "testValue"));
        }
        catch (LaunchDescException e) {
            ExtraData extraData = getExtraData(e);
            assertEquals(extraData.buildDictionary().get("testKey"), "testValue");
        }
        catch (Throwable e) {
            fail("wrong exception type was thrown");
        }
    }

    @Test
    public void enrichException_addExtraDataToExceptionWithExtraData() {
        try {
            Throwable exception = enrichedException(new LaunchDescException(), ExtraData.create().with("key1", "value1"));
            throw enrichedException(exception, ExtraData.create().with("key2", "value2"));
        }
        catch (LaunchDescException e) {
            ExtraData extraData = getExtraData(e);
            assertEquals(extraData.buildDictionary().get("key1"), "value1");
            assertEquals(extraData.buildDictionary().get("key2"), "value2");
        }
        catch (Throwable e) {
            fail("wrong exception type was thrown");
        }
    }

    @Test
    public void enrichException_addExtraDataToThrownException() throws Throwable {
        try {
            throw new RuntimeException("some runtime exception");
        }
        catch (RuntimeException runtimeException) {
            try {
                throw enrichedException(new IllegalStateException("some text", runtimeException), ExtraData.create().with("key1", "value1"));
            }
            catch (IllegalStateException illegalStateException) {
                try {
                    throw enrichedException(illegalStateException, ExtraData.create().with("key2", "value2"));

                }
                catch (Exception topLevelException) {
                    Throwable throwable = enrichedException(topLevelException, ExtraData.create().with("key3", "value3"));
                    ExtraData extraData = getExtraData(throwable);
                    assertEquals(extraData.buildDictionary().get("key1"), "value1");
                    assertEquals(extraData.buildDictionary().get("key2"), "value2");
                    assertEquals(extraData.buildDictionary().get("key3"), "value3");
                }
            }
        }
    }

    @Test
    public void getExtraData_fromExtraDataObject() {
        ExtraData extraData = ExtraData.create().with("key1", "value1");
        assertEquals(getExtraData(extraData).buildDictionary().get("key1"), "value1");
    }

    @Test
    public void getExtraData_whenNoExtraData_shouldEmptyExtraData() {
        ExtraData extraData = getExtraData(new Exception());
        assertEquals(extraData.buildDictionary().size(), 0);
    }

    @Test
    public void getExtraData_withCorrelationId() {
        String correlationId = getCorrelationId(withCorrelationId(getExtraData(ExtraData.create())));
        assertNotNull(UUID.fromString(correlationId));
    }
}

