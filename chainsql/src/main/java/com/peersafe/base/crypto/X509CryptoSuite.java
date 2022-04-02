/*
Copyright (C) THL A29 Limited, a Tencent company. All rights reserved.

SPDX-License-Identifier: Apache-2.0
*/
package com.peersafe.base.crypto;

import sun.security.util.CurveDB;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class X509CryptoSuite {
    private static final String ALGORITHM_SM2_KEY = "SM3withSM2";
    private static final String CIPHER_GM = "ECDHE-SM2-WITH-SMS4-GCM-SM3";

    public static void enableX509CertificateWithGM() throws IllegalAccessException, InvocationTargetException,
            NoSuchFieldException, ClassNotFoundException {
        Method[] methods = CurveDB.class.getDeclaredMethods();
        Method method = null;
        Method getNamesByOID = null;
        boolean notSeted = true;

        Pattern splitPattern = Pattern.compile(",|\\[|\\]");
        for (Method m : methods) {
            if ("add".equals(m.getName())) {
                method = m;
            }
            if("getNamesByOID".equals(m.getName())) {
                getNamesByOID = m;
            }
        }
        if(getNamesByOID != null) {
            String[] nameArray = (String [])getNamesByOID.invoke(CurveDB.class, "1.2.156.10197.1.301");
            if(nameArray.length != 0)
            {
                notSeted = false;
            }
        }

        if(notSeted)
        {
            if (method == null) {
                throw new NoSuchFieldException();
            }
            method.setAccessible(true);
            method.invoke(CurveDB.class, "sm2p256v1", "1.2.156.10197.1.301", 1,
                    "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF",
                    "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC",
                    "28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93",
                    "32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7",
                    "BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0",
                    "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123",
                    1, splitPattern);
    
            final Field specCollection = CurveDB.class.getDeclaredField("specCollection");
            final Field oidMap = CurveDB.class.getDeclaredField("oidMap");
            oidMap.setAccessible(true);
            specCollection.setAccessible(true);
            specCollection.set(CurveDB.class, Collections.unmodifiableCollection(((Map) oidMap.get(CurveDB.class)).values()));
    
            Field nameTable = AlgorithmId.class.getDeclaredField("nameTable");
            nameTable.setAccessible(true);
            Map<ObjectIdentifier, String> map = (HashMap) nameTable.get(AlgorithmId.class);
            ObjectIdentifier objectIdentifier = ObjectIdentifier.newInternal(new int[]{1, 2, 156, 10197, 1, 501});
            map.put(objectIdentifier, ALGORITHM_SM2_KEY);
        } 

        Class clazz = Class.forName("io.netty.handler.ssl.ExtendedOpenSslSession");
        Field algorithmsField = clazz.getDeclaredField("LOCAL_SUPPORTED_SIGNATURE_ALGORITHMS");
        algorithmsField.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(algorithmsField, algorithmsField.getModifiers() & ~Modifier.FINAL);
        String[] algorithms = (String[]) algorithmsField.get(null);
        if(!Arrays.asList(algorithms).contains(ALGORITHM_SM2_KEY))
        {
            String[] newAlgorithms = new String[algorithms.length + 1];
            System.arraycopy(algorithms, 0, newAlgorithms, 0, algorithms.length);
            newAlgorithms[algorithms.length] = ALGORITHM_SM2_KEY;
            algorithmsField.set(null, newAlgorithms);
        }
        

        Class cipherClass = Class.forName("io.netty.handler.ssl.OpenSsl");
        Field cipherField = cipherClass.getDeclaredField("AVAILABLE_OPENSSL_CIPHER_SUITES");
        cipherField.setAccessible(true);
        Field modifiersCField = Field.class.getDeclaredField("modifiers");
        modifiersCField.setAccessible(true);
        modifiersCField.setInt(cipherField, cipherField.getModifiers() & ~Modifier.FINAL);
        Set<String> ciphers = (Set<String>) cipherField.get(null);
        if(!ciphers.contains(CIPHER_GM))
        {
            Set<String> newCiphers = new LinkedHashSet<String>();
            newCiphers.addAll(ciphers);
            newCiphers.add(CIPHER_GM);
            cipherField.set(null, newCiphers);
        }
    }
}
