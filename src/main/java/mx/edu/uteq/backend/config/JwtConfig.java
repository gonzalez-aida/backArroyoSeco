package mx.edu.uteq.backend.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.InputStream;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

@Configuration
public class JwtConfig {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    public JwtConfig() {
        // Cargar las claves persistentes al iniciar el contexto de Spring
        KeyPair keyPair = loadRsaKey();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
    }

    // Método auxiliar para leer el contenido binario del archivo de clave
    private byte[] readKeyFile(String resourcePath) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                // Si la clave no se encuentra:
                throw new Exception("Archivo de clave no encontrado: " + resourcePath + ". Asegúrate de que esté en src/main/resources/");
            }
            return is.readAllBytes();
        }
    }

    // Método principal para cargar las claves desde los archivos .der
    private KeyPair loadRsaKey() {
        try {
            byte[] publicBytes = readKeyFile("/public.der"); 
            byte[] privateBytes = readKeyFile("/private.der");
            
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(publicBytes);
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(pubSpec);

            PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privateBytes);
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privSpec);

            return new KeyPair(publicKey, privateKey);

        } catch (Exception ex) {
            throw new IllegalStateException("FATAL: Error al cargar claves RSA persistentes. Causa: " + ex.getMessage(), ex);
        }
    }


    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        
        JWKSet jwkSet = new JWKSet(rsaKey);
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);
        
        return new NimbusJwtEncoder(jwkSource);
    }
}