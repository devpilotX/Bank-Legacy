package com.corewise.modernization.web.dto;

/** What we take to ask the AI for an explanation. Both lines are optional; leave
 * them out to explain the whole file. */
public record ExplainRequest(Integer startLine, Integer endLine) {
}
