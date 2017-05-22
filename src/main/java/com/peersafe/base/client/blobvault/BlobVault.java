package com.peersafe.base.client.blobvault;

import com.peersafe.base.crypto.sjcljson.JSONEncrypt;
import com.peersafe.base.encodings.common.B16;
import org.json.JSONObject;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.util.encoders.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

public class BlobVault {
    String baseUrl;

    public JSONEncrypt sjcl = new JSONEncrypt();

    /**
     * @param baseUrl eg. "https://blobvault.payward.com/"
     */
    public BlobVault(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static class BlobNotFound extends RuntimeException {
        public BlobNotFound(String msg) {
            super(msg);
        }
    }

    /**
     * Get blob.
     * @param user User.
     * @param pass Pass.
     * @return Return value.
     * @throws IOException IOException.
     * @throws InvalidCipherTextException InvalidCipherTextException.
     */
    public JSONObject getBlob(String user, String pass) throws IOException,
            InvalidCipherTextException {
        // Everywhere, this is expected to be lower cased.
        user = user.toLowerCase();

        String userPassUrl = userPassHash(user, pass);
        URL blobUrl = new URL(baseUrl + userPassUrl);
        HttpURLConnection getRequest = createGETRequestConnection(blobUrl);

        int responseCode = getRequest.getResponseCode();
        String data = readAllFromConnection(getRequest);
        if (responseCode == 404 || data.length() == 0) {
            // We won't log the pass
            throw new BlobNotFound("No blob found for user: " + user);
        }

        String utf8 = base64decodeUTF8(data);
        String decryptionKey;

        try {
            decryptionKey = userPassDerivedDecryptionKey(user, pass);
            return sjcl.decrypt(decryptionKey, utf8);
        } catch (InvalidCipherTextException e) {
            decryptionKey = userPassDerivedDecryptionKeyOLD(user, pass);
            return sjcl.decrypt(decryptionKey, utf8);
        }
    }

    /**
     * @param user Username already lower cased
     * @return Retun value.
     */
    private String userPassDerivedDecryptionKey(String user, String pass) {
        return user.length() + "|" + user + pass;
    }

    /**
     * @param user Username already lower cased
     * @return Return value.
     */
    private String userPassDerivedDecryptionKeyOLD(String user, String pass) {
        return user + pass;
    }

    /**
     * @param user Username already lower cased
     * @param pass password.
     * @return Return value.
     */
    public String userPassHash(String user, String pass) {
        String toHash = user + pass;
        try {
            byte[] toHashBytes = toHash.getBytes("utf8");
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(toHashBytes);
            return B16.toString(sha256);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpURLConnection createGETRequestConnection(URL website) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) website.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();
        return connection;
    }

    private String readAllFromConnection(HttpURLConnection connection) throws IOException {
        BufferedReader buf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder text = new StringBuilder();
        while ((line = buf.readLine()) != null)
            text.append(line);
        return text.toString();
    }

    private String base64decodeUTF8(String data) throws UnsupportedEncodingException {
        return new String(Base64.decode(data), "utf8");
    }
}
