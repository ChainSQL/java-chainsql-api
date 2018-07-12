package com.peersafe.codegen;

import org.junit.Test;

import com.peersafe.codegen.AbiTypesMapperGenerator;


public class AbiTypesMapperGeneratorTest extends TempFileProvider {

    @Test
    public void testGeneration() throws Exception {
        AbiTypesMapperGenerator.main(new String[] { tempDirPath });
    }
}
