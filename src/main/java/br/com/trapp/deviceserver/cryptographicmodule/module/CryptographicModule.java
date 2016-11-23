package br.com.trapp.deviceserver.cryptographicmodule.module;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.trapp.deviceserver.api.DeviceException;
import br.com.trapp.deviceserver.api.DeviceMessage;
import br.com.trapp.deviceserver.api.DeviceModule;
import br.com.trapp.deviceserver.cryptographicmodule.objects.CertificateInfo;
import br.com.trapp.deviceserver.cryptographicmodule.objects.CryptoDeviceInfo;
import br.com.trapp.deviceserver.cryptographicmodule.objects.CryptoDevices;
import br.com.trapp.deviceserver.cryptographicmodule.objects.SignatureRequest;

public class CryptographicModule implements DeviceModule {

    public DeviceMessage incomingMessage(DeviceMessage message) throws DeviceException {
	switch (message.getMethod()) {
	case "list":
	    return this.list();
	case "listCerts":
	    return this.listCerts(message);
	case "sign":
	    return this.sign(message);
	case "verify":
	    return this.verify(message);
	default:
	    break;
	}
	return new DeviceMessage("return", "result: {}");
    }

    public String getName() {
	return "Cryptographic Module";
    }

    public String getVersion() {
	return "1.0";
    }

    public String getIdendifier() {
	return "crypto";
    }

    /**
     * List all connected devices
     * 
     * @return a list with label and serial of all connected devices
     */
    private DeviceMessage list() {
	Set<CryptoDeviceInfo> devices = CryptoDevices.list();
	ObjectMapper mapper = new ObjectMapper();
	// Object to JSON in String
	String jsonInString = null;
	try {
	    jsonInString = mapper.writeValueAsString(devices);
	} catch (JsonProcessingException e) {
	    e.printStackTrace();
	}
	return new DeviceMessage("list", jsonInString);
    }

    /**
     * List all certs inside the given device
     * 
     * @param message
     *            the message
     * @return the certs with public keys in this device
     */
    private DeviceMessage listCerts(DeviceMessage message) {
	String jsonInString = null;
	try {
	    ObjectMapper mapper = new ObjectMapper();
	    CryptoDeviceInfo deviceInfo = null;
	    deviceInfo = mapper.readValue(message.getMessage(), CryptoDeviceInfo.class);
	    List<CertificateInfo> certs = CryptoDevices.listCertsWithMatchingKey(deviceInfo);

	    // Object to JSON in String
	    jsonInString = mapper.writeValueAsString(certs);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return new DeviceMessage("listCerts", jsonInString);
    }

    /**
     * Sign a message
     * 
     * @param message
     * @return
     * @throws DeviceException
     */
    private DeviceMessage sign(DeviceMessage message) throws DeviceException {
	String jsonInString = null;
	try {
	    ObjectMapper mapper = new ObjectMapper();
	    SignatureRequest signReq = null;
	    signReq = mapper.readValue(message.getMessage(), SignatureRequest.class);
	    byte[] signedBytes = CryptoDevices.sign(signReq);

	    // Object to JSON in String
	    jsonInString = mapper.writeValueAsString(Base64.getEncoder().encodeToString(signedBytes));
	} catch (IOException e) {
	    throw new DeviceException(e.getMessage(), e);
	}
	return new DeviceMessage("sign", jsonInString);
    }

    /**
     * Verify a message
     * 
     * @param message
     * @return
     * @throws Exception
     */
    private DeviceMessage verify(DeviceMessage message) throws DeviceException {
	String jsonInString = null;
	try {
	    ObjectMapper mapper = new ObjectMapper();
	    SignatureRequest signReq = null;
	    signReq = mapper.readValue(message.getMessage(), SignatureRequest.class);
	    byte[] signedBytes = CryptoDevices.sign(signReq);

	    // Object to JSON in String
	    jsonInString = mapper.writeValueAsString(Base64.getEncoder().encodeToString(signedBytes));
	} catch (IOException e) {
	    throw new DeviceException(e.getMessage(), e);
	}
	return new DeviceMessage("verify", jsonInString);
    }

}
