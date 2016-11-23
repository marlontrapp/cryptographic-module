package br.com.trapp.deviceserver.cryptographicmodule.objects;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.cms.CMSSignedGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import br.com.trapp.deviceserver.api.DeviceException;
import iaik.pkcs.pkcs11.Mechanism;
import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.Session;
import iaik.pkcs.pkcs11.Slot;
import iaik.pkcs.pkcs11.Token;
import iaik.pkcs.pkcs11.TokenException;
import iaik.pkcs.pkcs11.TokenInfo;
import iaik.pkcs.pkcs11.objects.Attribute;
import iaik.pkcs.pkcs11.objects.Object;
import iaik.pkcs.pkcs11.objects.PrivateKey;
import iaik.pkcs.pkcs11.objects.PublicKey;
import iaik.pkcs.pkcs11.objects.X509PublicKeyCertificate;

public class CryptoDevices {

    private static List<String> MODULES;
    private static Map<CryptoDeviceInfo, Token> lastListingTokens;

    static {
	String os = System.getProperty("os.name").toLowerCase();
	if (os.contains("nux") || os.contains("nix"))
	    MODULES = new ArrayList<String>(
		    Arrays.asList("libesp11_bio3k.so", "libepsng_p11.so", "libaetpkss.so", "libacospkcs11.so",
			    "libASEP11.so", "libcastle.so.1.0.0.so", "libshuttle_p11v220.so", "opensc-pkcs11.so"));
	else if (os.contains("win"))
	    MODULES = new ArrayList<String>(Arrays.asList("es1b3k.dll", "ep2pk11.dll", "aetpkss1.dll", "acospkcs11.dll",
		    "asepkcs.dll", "ngp11v211.dll", "eps2003csp11.dll", "pronovacsp11.dll", "opensc-pkcs11.dll"));
	else if (os.contains("mac"))
	    MODULES = new ArrayList<String>(Arrays.asList("libepsng_p11.dylib", "libaetpkss.dylib", "libASEP11.dylib",
		    "libcastle.1.0.0.dylib", "libacospkcs11.so"));

	Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * List all cryptographic devices connected on this computer, since any
     * module can connect with it
     * 
     * @return a list with all cryptographic devices connected on this computer
     */
    public static Set<CryptoDeviceInfo> list() {
	Map<CryptoDeviceInfo, Token> tokens = new HashMap<CryptoDeviceInfo, Token>();

	for (int i = 0; i < MODULES.size(); i++) {
	    Module pkcs11Module = null;

	    try {
		pkcs11Module = Module.getInstance(MODULES.get(i));
		try {
		    pkcs11Module.initialize(null);
		} catch (TokenException e) {
		    // e.printStackTrace();
		    // It might be opened previously, so we ignore and keep
		    // going
		}
		Slot[] slots = pkcs11Module.getSlotList(Module.SlotRequirement.TOKEN_PRESENT);

		for (Slot slot : slots) {
		    Token token = null;
		    try {
			token = slot.getToken();
			token.openSession(Token.SessionType.SERIAL_SESSION, Token.SessionReadWriteBehavior.RO_SESSION,
				null, null);
			TokenInfo tokenInfo = token.getTokenInfo();
			CryptoDeviceInfo cryptoInfo = new CryptoDeviceInfo(tokenInfo.getSerialNumber(),
				tokenInfo.getLabel());
			if (!tokens.containsKey(cryptoInfo)) {
			    tokens.put(cryptoInfo, token);
			} else {
			    // Select the compatible module with more available
			    // mechanisms.
			    Mechanism[] oldMechanisms = tokens.get(cryptoInfo).getMechanismList();
			    Mechanism[] newMechanisms = token.getMechanismList();
			    if (oldMechanisms.length < newMechanisms.length)
				tokens.put(cryptoInfo, token);
			}
		    } catch (Exception e) {
			// Error openning session, the PKCS#11 module doesn't
			// support the card
		    } finally {
			try {
			    token.closeAllSessions();
			} catch (Exception e) {

			}
		    }
		}
	    } catch (IOException e) {
		// Error loading library, PKCS#11 module not found, or it is not
		// a PKCS#11 module
	    } catch (TokenException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }
	}
	lastListingTokens = tokens;
	return tokens.keySet();
    }

    /**
     * This method will return all certificates that have a public key with the
     * same ID of it, if the device contains multiple certificates with the same
     * ID it can cause strange effects.
     * 
     * @param tokenSerial
     *            the serial of the token that will receive this operation
     * 
     * @return a list of ID and Label of each certificate that match the
     *         conditions
     */
    public static List<CertificateInfo> listCertsWithMatchingKey(CryptoDeviceInfo tokenSerial) {
	if (lastListingTokens == null)
	    CryptoDevices.list();
	Token token = lastListingTokens.get(tokenSerial);
	Session session;
	List<CertificateInfo> matchedCerts = new ArrayList<CertificateInfo>();
	try {
	    session = token.openSession(Token.SessionType.SERIAL_SESSION, Token.SessionReadWriteBehavior.RO_SESSION,
		    null, null);

	    Object[] objects;
	    // FIND public keys
	    List<PublicKey> publicKeys = new ArrayList<PublicKey>();
	    session.findObjectsInit(new PublicKey());
	    while ((objects = session.findObjects(10)).length > 0) {
		for (Object object : objects) {
		    publicKeys.add((PublicKey) object);
		}
	    }
	    session.findObjectsFinal();

	    // FIND certificates
	    List<X509PublicKeyCertificate> certificates = new ArrayList<X509PublicKeyCertificate>();
	    session.findObjectsInit(new X509PublicKeyCertificate());
	    while ((objects = session.findObjects(10)).length > 0) {
		for (Object object : objects) {
		    certificates.add((X509PublicKeyCertificate) object);
		}
	    }
	    session.findObjectsFinal();

	    for (X509PublicKeyCertificate certificate : certificates) {
		for (PublicKey key : publicKeys) {
		    if (key.getAttribute(Attribute.ID).equals(certificate.getAttribute(Attribute.ID))) {
			CertificateInfo certInfo = new CertificateInfo();
			certInfo.setId(certificate.getAttribute(Attribute.ID).toString());
			certInfo.setLabel(certificate.getAttribute(Attribute.LABEL).toString());
			certInfo.setDeviceSerial(tokenSerial.getSerial());
			matchedCerts.add(certInfo);
		    }
		}
	    }

	} catch (TokenException e) {
	    e.printStackTrace();
	} finally {
	    try {
		token.closeAllSessions();
	    } catch (TokenException e) {
	    }
	}
	return matchedCerts;
    }

    public static Certificate getCertificate(CryptoDeviceInfo tokenId, CertificateInfo certificateInfo) {
	if (lastListingTokens == null)
	    CryptoDevices.list();
	Token token = lastListingTokens.get(tokenId);
	Session session;
	Certificate certificate = null;
	try {
	    session = token.openSession(Token.SessionType.SERIAL_SESSION, Token.SessionReadWriteBehavior.RO_SESSION,
		    null, null);

	    Object[] objects;
	    // FIND certificates
	    CertificateTemplate template = new CertificateTemplate();
	    if (certificateInfo.getId() != null)
		template.setIdAttribute(certificateInfo.getId());
	    if (certificateInfo.getLabel() != null)
		template.setLabelAttribute(certificateInfo.getLabel());

	    session.findObjectsInit(template);
	    objects = session.findObjects(1);
	    session.findObjectsFinal();
	    if (objects.length > 0) {
		X509PublicKeyCertificate cert = (X509PublicKeyCertificate) objects[0];
		Attribute attr = cert.getAttribute(Attribute.VALUE);
		byte[] certBytes = DatatypeConverter.parseHexBinary(attr.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
		certificate = CertificateFactory.getInstance("x509").generateCertificate(bais);
	    }

	} catch (TokenException e) {
	    e.printStackTrace();
	} catch (CertificateException e) {
	} finally {
	    try {
		token.closeAllSessions();
	    } catch (TokenException e) {
	    }
	}
	return certificate;

    }

    public static byte[] sign(SignatureRequest signReq) throws DeviceException {
	if (lastListingTokens == null)
	    CryptoDevices.list();
	Token token = lastListingTokens.get(new CryptoDeviceInfo(signReq.getTokenSerial(), null));
	Session session;
	try {
	    session = token.openSession(Token.SessionType.SERIAL_SESSION, Token.SessionReadWriteBehavior.RW_SESSION,
		    null, null);
	    char[] psw = Utils.getPassword();
	    if (psw != null) {
		session.login(Session.UserType.USER, psw);

		PrivateKeyTemplate templateSignatureKey = new PrivateKeyTemplate();
		templateSignatureKey.getSign().setBooleanValue(Boolean.TRUE);
		templateSignatureKey.setIdAttribute(signReq.getKeyId());

		byte[] bytes = Base64.getDecoder().decode(signReq.getData());
		session.findObjectsInit(templateSignatureKey);
		Object[] objects = session.findObjects(1);
		session.findObjectsFinal();
		if (objects.length > 0) {
		    PrivateKey key = (PrivateKey) objects[0];
		    MessageDigest digestEngine = MessageDigest.getInstance(signReq.getHashAlgorithm(), "BC");
		    // String sha1Oid = CMSSignedGenerator.DIGEST_SHA1;
		    Field field = CMSSignedGenerator.class.getDeclaredField("DIGEST_" + signReq.getHashAlgorithm());
		    String hashOid = (String) field.get(null);

		    // be sure that your token can process the specified
		    // mechanism
		    Mechanism signatureMechanism = Mechanisms.getCorrespondentMechanism(key);
		    if (signatureMechanism != null) {
			// initialize for signing
			session.signInit(signatureMechanism, key);

			byte[] digest = digestEngine.digest(bytes);
			DigestInfo di = new DigestInfo(new AlgorithmIdentifier(new ASN1ObjectIdentifier(hashOid)),
				digest);
			byte[] signatureValue = session.sign(di.getEncoded());
			session.closeSession();
			return signatureValue;
		    } else {
			throw new DeviceException("Key type not supported");
		    }
		}
	    } else {
		throw new DeviceException("The user doesn't allowed the signature");
	    }
	} catch (Exception e) {
	    throw new DeviceException(e.getMessage(), e);
	} finally {
	    try {
		token.closeAllSessions();
	    } catch (TokenException e) {
		// e.printStackTrace();
	    }
	}
	return null;

    }

    // Set<String> keySet = tokens.keySet();
    // for (String serial : keySet) {
    // Token t = tokens.get(serial);
    // System.out.println("Token label: " + t.getTokenInfo().getLabel());
    // Session session = t.openSession(Token.SessionType.SERIAL_SESSION,
    // Token.SessionReadWriteBehavior.RW_SESSION, null, null);
    // session.login(Session.UserType.USER, "123456".toCharArray());
    // session.findObjectsInit(new PublicKey());
    // Object[] objects = session.findObjects(10);
    // for (Object object : objects) {
    // System.out.println("Object:" + object.getClass());
    // }
    // t.closeAllSessions();
    // // byte[] dataBuffer = session.generateRandom(128);
    // // System.out.println(new BigInteger(dataBuffer));
    // }

}
