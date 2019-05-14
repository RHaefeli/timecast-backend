package wodss.timecastbackend.security;

import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import wodss.timecastbackend.exception.TimecastInternalServerErrorException;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Component
public class HsUtil {
    private static ResourceLoader resourceLoader;

    @Autowired
    public HsUtil (ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public static Key getKeyFromFile(String filename) {
        String key = getKey(filename);
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(key);
        return new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    private static String getKey(String filename) {
        // Read key from file
        StringBuilder strKey = new StringBuilder();
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
                strKey.append(line).append("\n");
            }
        } catch (IOException ex) {
            throw new TimecastInternalServerErrorException(ex.getMessage());
        }

        return strKey.toString();
    }
}
