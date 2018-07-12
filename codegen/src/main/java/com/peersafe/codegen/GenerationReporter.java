package com.peersafe.codegen;

/**
 * Can be used to provide report about a code generation process.
 */
public interface GenerationReporter {
    void report(String msg);
}
