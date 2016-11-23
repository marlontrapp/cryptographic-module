package br.com.trapp.deviceserver.cryptographicmodule.objects;

public class CryptoDeviceInfo {

    private String serial;
    private String label;

    public CryptoDeviceInfo() {
    }

    public CryptoDeviceInfo(String serial, String label) {
	this.serial = serial;
	this.label = label;
    }

    public String getSerial() {
	return serial;
    }

    public void setSerial(String serial) {
	this.serial = serial;
    }

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((serial == null) ? 0 : serial.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	CryptoDeviceInfo other = (CryptoDeviceInfo) obj;
	if (serial == null) {
	    if (other.serial != null)
		return false;
	} else if (!serial.equals(other.serial))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "CryptoDeviceInfo [serial=" + serial + ", label=" + label + "]";
    }

}
