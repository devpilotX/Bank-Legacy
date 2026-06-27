package com.corewise.modernization.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AiClientTest {

    // Tiny backoff so the tests stay fast.
    private AiProperties props(int maxAttempts) {
        return new AiProperties("claude", Duration.ofSeconds(5), maxAttempts, Duration.ofMillis(1), null, null);
    }

    @Test
    void returnsTheAnswerWhenTheProviderSucceeds() {
        AiProvider provider = Mockito.mock(AiProvider.class);
        when(provider.complete("system", "user")).thenReturn("the answer");

        AiClient client = new AiClient(provider, props(3));

        assertThat(client.complete("system", "user")).isEqualTo("the answer");
        verify(provider, times(1)).complete("system", "user");
    }

    @Test
    void retriesRetryableFailuresThenSucceeds() {
        AiProvider provider = Mockito.mock(AiProvider.class);
        when(provider.name()).thenReturn("test");
        when(provider.complete(anyString(), anyString()))
            .thenThrow(new AiException("timed out", true))
            .thenThrow(new AiException("timed out", true))
            .thenReturn("ok at last");

        AiClient client = new AiClient(provider, props(3));

        assertThat(client.complete("s", "u")).isEqualTo("ok at last");
        verify(provider, times(3)).complete("s", "u");
    }

    @Test
    void doesNotRetryANonRetryableFailure() {
        AiProvider provider = Mockito.mock(AiProvider.class);
        when(provider.complete(anyString(), anyString()))
            .thenThrow(new AiException("the key is missing", false));

        AiClient client = new AiClient(provider, props(3));

        assertThatThrownBy(() -> client.complete("s", "u"))
            .isInstanceOf(AiException.class)
            .hasMessageContaining("the key is missing");
        verify(provider, times(1)).complete("s", "u");
    }

    @Test
    void givesUpWithAClearErrorAfterTheAttemptCap() {
        AiProvider provider = Mockito.mock(AiProvider.class);
        when(provider.name()).thenReturn("test");
        when(provider.complete(anyString(), anyString()))
            .thenThrow(new AiException("timed out", true));

        AiClient client = new AiClient(provider, props(3));

        assertThatThrownBy(() -> client.complete("s", "u")).isInstanceOf(AiException.class);
        verify(provider, times(3)).complete("s", "u");
    }
}
