package br.com.trapp.deviceserver.cryptographicmodule.objects;

public class CertificateInfo {

    private String id;
    private String label;
    private String deviceSerial;

    public CertificateInfo() {
    }

    public CertificateInfo(String id, String label, String deviceSerial) {
	this.id = id;
	this.label = label;
	this.deviceSerial = deviceSerial;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    public String getDeviceSerial() {
	return deviceSerial;
    }

    public void setDeviceSerial(String deviceSerial) {
	this.deviceSerial = deviceSerial;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((id == null) ? 0 : id.hashCode());
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
	CertificateInfo other = (CertificateInfo) obj;
	if (id == null) {
	    if (other.id != null)
		return false;
	} else if (!id.equals(other.id))
	    return false;
	return true;
    }

}
