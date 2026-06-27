package com.corewise.modernization.ai;

/**
 * One hosted AI we can talk to, like Claude or GPT. Each provider knows how to call
 * its own vendor over HTTPS. Swapping providers is a config change, not a code
 * change, because the rest of the app only ever sees this interface.
 */
public interface AiProvider {

    /** A short name for logs, like "claude" or "openai". */
    String name();

    /**
     * Sends a system instruction and a user message to the AI and returns its text
     * reply. Throws AiException on any trouble, never a silent or made-up answer.
     */
    String complete(String systemPrompt, String userPrompt);
}
