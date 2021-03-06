package wodss.timecastbackend.security;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import wodss.timecastbackend.exception.TimecastInternalServerErrorException;

import java.io.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Component
public class RsaUtil {

    private static ResourceLoader resourceLoader;

    @Autowired
    public RsaUtil (ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public static RSAPrivateKey getPrivateKeyFromString(String key) {
        String privateKeyPEM = key;
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
        privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
        byte[] encoded = Base64.decodeBase64(privateKeyPEM);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return (RSAPrivateKey) kf.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new TimecastInternalServerErrorException("Incorrect RSA Private Key: " + ex.getMessage());
        }
    }

    public static RSAPublicKey getPublicKeyFromString(String key)  {
        String publicKeyPEM = key;
        publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "");
        publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
        byte[] encoded = Base64.decodeBase64(publicKeyPEM);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(encoded));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new TimecastInternalServerErrorException("Incorrect RSA Public Key: " + ex.getMessage());
        }
    }

    public static String getKey(String filename) {
        // Read key from file
        StringBuilder strKeyPEM = new StringBuilder();
        InputStream is = null;
        try {
            Resource resource = resourceLoader.getResource(filename);
            is = resource.getInputStream();
        } catch (IOException ex) {
            throw new TimecastInternalServerErrorException(ex.getMessage());
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))){
            String line;
            while ((line = br.readLine()) != null) {
                strKeyPEM.append(line).append("\n");
            }
        } catch (IOException ex) {
            throw new TimecastInternalServerErrorException(ex.getMessage());
        }

        return strKeyPEM.toString();
    }
}