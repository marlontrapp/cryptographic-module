package br.com.trapp.deviceserver.cryptographicmodule;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import br.com.trapp.deviceserver.api.DeviceException;
import br.com.trapp.deviceserver.cryptographicmodule.objects.CertificateInfo;
import br.com.trapp.deviceserver.cryptographicmodule.objects.CryptoDeviceInfo;
import br.com.trapp.deviceserver.cryptographicmodule.objects.CryptoDevices;
import br.com.trapp.deviceserver.cryptographicmodule.objects.SignatureRequest;
import br.com.trapp.deviceserver.cryptographicmodule.objects.Utils;
import iaik.pkcs.pkcs11.TokenException;

public class TestCryptoDevices {

     @Test
    public void test() throws CertificateEncodingException, NoSuchAlgorithmException, InstantiationException,
	    IllegalAccessException, IllegalArgumentException, InvocationTargetException, DeviceException {
	//
	Set<CryptoDeviceInfo> tokens = CryptoDevices.list();
	System.out.println(tokens);

	Iterator<CryptoDeviceInfo> it = tokens.iterator();
//	it.next();
	CryptoDeviceInfo tokenId = it.next();
	List<CertificateInfo> certInfos = CryptoDevices.listCertsWithMatchingKey(tokenId);

	for (CertificateInfo certificateInfo : certInfos) {
	    X509Certificate cert = (X509Certificate) CryptoDevices.getCertificate(tokenId, certificateInfo);
	    System.out.println(cert.getSubjectDN().getName());
	    byte[] signedData = CryptoDevices.sign(new SignatureRequest(tokenId.getSerial(), certificateInfo.getLabel(),
		    certificateInfo.getId(), "SHA256", "bWVzc2FnZQ=="));
	    System.out.println(Base64.getEncoder().encodeToString(signedData));
	    PublicKey pubKey;
	    try {
		pubKey = cert.getPublicKey();
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(pubKey);
		sig.update("message".getBytes());

		System.out.println(sig.verify(signedData));
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

    }

    // @Test
    public void test2() throws TokenException, IOException {
	// Files.find(start, maxDepth, matcher, options);
	char[] psw = Utils.getPassword();
	System.out.println(psw);
    }

}
