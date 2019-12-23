/*
 * Copyright 2019 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.extensions.services.auth;

import com.hivemq.extension.sdk.api.async.TimeoutFallback;
import com.hivemq.extension.sdk.api.packets.general.DisconnectedReasonCode;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.extensions.packets.general.ModifiableDefaultPermissionsImpl;
import com.hivemq.util.Bytes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;

import static com.hivemq.extensions.services.auth.AuthenticationState.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Florian Limpöck
*/
public class AuthTaskOutputTest {

    @Mock
    private PluginOutPutAsyncer asyncer;

    private AuthTaskOutput authTaskOutput;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        authTaskOutput = new AuthTaskOutput(asyncer, new ModifiableClientSettingsImpl(10), new ModifiableDefaultPermissionsImpl(), new AuthenticationContext(), true, false, 30, true);
    }

    @Test(timeout = 5000)
    public void test_user_properties_of_connect_are_not_present() {
        final List<String> one = authTaskOutput.getOutboundUserProperties().getAllForName("one");
        final List<String> two = authTaskOutput.getOutboundUserProperties().getAllForName("two");

        assertEquals(0, one.size());
        assertEquals(0, two.size());
    }

    @Test(timeout = 5000)
    public void test_copy_constructor_receives_user_properties() {

        authTaskOutput.getOutboundUserProperties().addUserProperty("testA", "valueA");

        authTaskOutput = new AuthTaskOutput(authTaskOutput);
        authTaskOutput.getOutboundUserProperties().addUserProperty("testB", "valueB");

        authTaskOutput = new AuthTaskOutput(authTaskOutput);
        authTaskOutput.getOutboundUserProperties().addUserProperty("testC", "valueC");

        authTaskOutput = new AuthTaskOutput(authTaskOutput);

        assertEquals(3, authTaskOutput.getChangedUserProperties().asImmutableList().size());

    }

    @Test(timeout = 5000)
    public void test_fail_sets_decided() {
        authTaskOutput.failAuthentication();
        assertEquals(FAILED, authTaskOutput.getAuthenticationState());
        assertEquals(DisconnectedReasonCode.NOT_AUTHORIZED, authTaskOutput.getDisconnectedReasonCode());
        assertEquals("Authentication failed by extension", authTaskOutput.getReasonString());
    }

    @Test(timeout = 5000)
    public void test_fail_with_reason() {
        authTaskOutput.failAuthentication(DisconnectedReasonCode.BAD_AUTHENTICATION_METHOD, "REASON");
        assertEquals(FAILED, authTaskOutput.getAuthenticationState());
        assertEquals(DisconnectedReasonCode.BAD_AUTHENTICATION_METHOD, authTaskOutput.getDisconnectedReasonCode());
        assertEquals("REASON", authTaskOutput.getReasonString());
    }

    @Test(timeout = 5000, expected = IllegalArgumentException.class)
    public void test_fail_with_reason_success() {
        authTaskOutput.failAuthentication(DisconnectedReasonCode.SUCCESS, "REASON");
    }

    @Test(timeout = 5000)
    public void test_success_sets_decided() {
        authTaskOutput.authenticateSuccessfully();
        assertEquals(SUCCESS, authTaskOutput.getAuthenticationState());
    }

    @Test(timeout = 5000)
    public void test_success_bytes_data() {
        authTaskOutput.authenticateSuccessfully("data".getBytes());
        assertEquals(SUCCESS, authTaskOutput.getAuthenticationState());
        assertArrayEquals("data".getBytes(), Bytes.fromReadOnlyBuffer(authTaskOutput.getAuthenticationData()));
    }

    @Test(timeout = 5000)
    public void test_success_buffer_data() {
        authTaskOutput.authenticateSuccessfully(ByteBuffer.wrap("data".getBytes()));
        assertEquals(SUCCESS, authTaskOutput.getAuthenticationState());
        assertArrayEquals("data".getBytes(), Bytes.fromReadOnlyBuffer(authTaskOutput.getAuthenticationData()));
    }

    @Test(timeout = 5000)
    public void test_continue() {
        authTaskOutput.continueAuthentication();
        assertEquals(CONTINUE, authTaskOutput.getAuthenticationState());
    }

    @Test(timeout = 5000)
    public void test_continue_bytes_data() {
        authTaskOutput.continueAuthentication("data".getBytes());
        assertEquals(CONTINUE, authTaskOutput.getAuthenticationState());
        assertArrayEquals("data".getBytes(), Bytes.fromReadOnlyBuffer(authTaskOutput.getAuthenticationData()));
    }

    @Test(timeout = 5000)
    public void test_continue_buffer_data() {
        authTaskOutput.continueAuthentication(ByteBuffer.wrap("data".getBytes()));
        assertEquals(CONTINUE, authTaskOutput.getAuthenticationState());
        assertArrayEquals("data".getBytes(), Bytes.fromReadOnlyBuffer(authTaskOutput.getAuthenticationData()));
    }

    @Test(timeout = 5000)
    public void test_continue_does_not_decide() {
        authTaskOutput.nextExtensionOrDefault();
        assertEquals(NEXT_EXTENSION_OR_DEFAULT, authTaskOutput.getAuthenticationState());
    }

    @Test(timeout = 5000)
    public void test_throwable_fails_authentication() {
        authTaskOutput.setThrowable(new RuntimeException());
        assertEquals(FAILED, authTaskOutput.getAuthenticationState());
        assertEquals(DisconnectedReasonCode.UNSPECIFIED_ERROR, authTaskOutput.getDisconnectedReasonCode());
        assertEquals("Unhandled exception in authentication extension", authTaskOutput.getReasonString());
    }

    @Test(timeout = 5000)
    public void test_async_duration() {

        authTaskOutput.async(Duration.ofSeconds(10));

        assertEquals(DisconnectedReasonCode.NOT_AUTHORIZED, authTaskOutput.getDisconnectedReasonCode());
        assertEquals("Authentication failed by timeout", authTaskOutput.getReasonString());

    }

    @Test(timeout = 5000, expected = NullPointerException.class)
    public void test_async_null() {

        authTaskOutput.async(null);

    }

    @Test(timeout = 5000)
    public void test_async_duration_fallback() {

        authTaskOutput.async(Duration.ofSeconds(10), TimeoutFallback.SUCCESS);

        assertEquals(DisconnectedReasonCode.NOT_AUTHORIZED, authTaskOutput.getDisconnectedReasonCode());
        assertEquals("Authentication failed by timeout", authTaskOutput.getReasonString());

    }

    @Test(timeout = 5000)
    public void test_async_duration_fallback_code() {

        authTaskOutput.async(
                Duration.ofSeconds(10), TimeoutFallback.SUCCESS, DisconnectedReasonCode.BAD_AUTHENTICATION_METHOD);

        assertEquals(DisconnectedReasonCode.BAD_AUTHENTICATION_METHOD, authTaskOutput.getDisconnectedReasonCode());
        assertEquals("Authentication failed by timeout", authTaskOutput.getReasonString());

    }

    @Test(timeout = 5000)
    public void test_async_duration_fallback_string() {

        authTaskOutput.async(Duration.ofSeconds(10), TimeoutFallback.SUCCESS, "Failed by me");

        assertEquals(DisconnectedReasonCode.NOT_AUTHORIZED, authTaskOutput.getDisconnectedReasonCode());
        assertEquals("Failed by me", authTaskOutput.getReasonString());

    }

    @Test(timeout = 5000, expected = IllegalArgumentException.class)
    public void test_async_success_reason_code() {

        authTaskOutput.async(
                Duration.ofSeconds(10), TimeoutFallback.SUCCESS, DisconnectedReasonCode.SUCCESS, "Failed by me");

    }

    @Test(timeout = 5000)
    public void test_async_duration_fallback_code_string() {

        authTaskOutput.async(
                Duration.ofSeconds(10), TimeoutFallback.SUCCESS, DisconnectedReasonCode.SERVER_BUSY, "Failed by me");

        assertEquals(DisconnectedReasonCode.SERVER_BUSY, authTaskOutput.getDisconnectedReasonCode());
        assertEquals("Failed by me", authTaskOutput.getReasonString());

    }

}