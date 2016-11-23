package br.com.trapp.deviceserver.cryptographicmodule.objects;

public class SignatureRequest {

    private String tokenSerial;
    private String keyLabel;
    private String keyId;
    private String hashAlgorithm;
    private String data;

    public SignatureRequest() {
    }

    public SignatureRequest(String tokenSerial, String keyLabel, String keyId, String hashAlgorithm, String data) {
	this.tokenSerial = tokenSerial;
	this.keyLabel = keyLabel;
	this.keyId = keyId;
	this.hashAlgorithm = hashAlgorithm;
	this.data = data;
    }

    public String getKeyLabel() {
	return keyLabel;
    }

    public void setKeyLabel(String keyLabel) {
	this.keyLabel = keyLabel;
    }

    public String getKeyId() {
	return keyId;
    }

    public void setKeyId(String keyId) {
	this.keyId = keyId;
    }

    public String getHashAlgorithm() {
	return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
	this.hashAlgorithm = hashAlgorithm;
    }

    public String getTokenSerial() {
	return tokenSerial;
    }

    public void setTokenSerial(String tokenSerial) {
	this.tokenSerial = tokenSerial;
    }

    public String getData() {
	return data;
    }

    public void setData(String data) {
	this.data = data;
    }

    @Override
    public String toString() {
	return "SignatureRequest [tokenSerial=" + tokenSerial + ", keyLabel=" + keyLabel + ", keyId=" + keyId
		+ ", hashAlgorithm=" + hashAlgorithm + ", data=" + data + "]";
    }

}
