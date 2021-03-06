package security.general.keyagreement;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

/**
 * @Desc TODO
 * @Author shm
 * @Date 2021/12/8 5:49
 */
public class DHKeyAgreement3 {

    public static void main(String[] args) throws Exception {

        // Alice creates her own DH key pair with 2048-bit key size
        System.out.println("ALICE: Generate DH keypair ...");
        KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
        aliceKpairGen.initialize(2048);
        KeyPair aliceKpair = aliceKpairGen.generateKeyPair();

        // This DH parameters can also be constructed by creating a
        // DHParameterSpec io.object using agreed-upon values
//        DHParameterSpec dhParameterSpec =  ( (DHPrivateKey) aliceKpair.getPrivate()).getParams();
        DHParameterSpec dhParamShared = ((DHPublicKey)aliceKpair.getPublic()).getParams();

        // Bob creates his own DH key pair using the same params
        System.out.println("BOB: Generate DH keypair ...");
        KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
        bobKpairGen.initialize(dhParamShared);
        KeyPair bobKpair = bobKpairGen.generateKeyPair();

        // Carol creates her own DH key pair using the same params
        System.out.println("CAROL: Generate DH keypair ...");
        KeyPairGenerator carolKpairGen = KeyPairGenerator.getInstance("DH");
        carolKpairGen.initialize(dhParamShared);
        KeyPair carolKpair = carolKpairGen.generateKeyPair();

        // Alice initialize
        System.out.println("ALICE: Initialize ...");
        KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
        aliceKeyAgree.init(aliceKpair.getPrivate());

        // Bob initialize
        System.out.println("BOB: Initialize ...");
        KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
        bobKeyAgree.init(bobKpair.getPrivate());

        // Carol initialize
        System.out.println("CAROL: Initialize ...");
        KeyAgreement carolKeyAgree = KeyAgreement.getInstance("DH");
        carolKeyAgree.init(carolKpair.getPrivate());

        // Alice uses Carol's public key
        Key ac = aliceKeyAgree.doPhase(carolKpair.getPublic(), false);
        // Bob uses Alice's public key
        Key ba = bobKeyAgree.doPhase(aliceKpair.getPublic(), false);
        // Carol uses Bob's public key
        Key cb = carolKeyAgree.doPhase(bobKpair.getPublic(), false);



        // Alice uses Carol's result from above
        aliceKeyAgree.doPhase(cb, true);
//        aliceKeyAgree.doPhase(getPublicKey(cb.getEncoded()), true);
        // Bob uses Alice's result from above
//        bobKeyAgree.doPhase(ac, true);
        bobKeyAgree.doPhase(getPublicKey(ac.getEncoded()), true);
        // Carol uses Bob's result from above
//        carolKeyAgree.doPhase(ba, true);
        carolKeyAgree.doPhase(getPublicKey(ba.getEncoded()), true);

        // Alice, Bob and Carol compute their secrets
        byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();
        System.out.println("Alice secret: " + toHexString(aliceSharedSecret));
        byte[] bobSharedSecret = bobKeyAgree.generateSecret();
        System.out.println("Bob secret: " + toHexString(bobSharedSecret));
        byte[] carolSharedSecret = carolKeyAgree.generateSecret();
        System.out.println("Carol secret: " + toHexString(carolSharedSecret));

        // Compare Alice and Bob
        if (!java.util.Arrays.equals(aliceSharedSecret, bobSharedSecret))
            throw new Exception("Alice and Bob differ");
        System.out.println("Alice and Bob are the same");

        // Compare Bob and Carol
        if (!java.util.Arrays.equals(bobSharedSecret, carolSharedSecret))
            throw new Exception("Bob and Carol differ");
        System.out.println("Bob and Carol are the same");

    }

    static PublicKey getPublicKey(byte[] publicKeyEnc) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyEnc);
        return keyFactory.generatePublic(x509KeySpec);
    }

    /*
     * Converts a byte to hex digit and writes to the supplied buffer
     */
    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }
    /*
     * Converts a byte array to hex string
     */
    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len-1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }
}
