package br.com.trapp.deviceserver.cryptographicmodule.objects;

import javax.xml.bind.DatatypeConverter;

import iaik.pkcs.pkcs11.objects.Attribute;
import iaik.pkcs.pkcs11.objects.ByteArrayAttribute;
import iaik.pkcs.pkcs11.objects.CharArrayAttribute;
import iaik.pkcs.pkcs11.objects.X509PublicKeyCertificate;

public class CertificateTemplate extends X509PublicKeyCertificate {

    public CertificateTemplate() {
	super();
    }

    @SuppressWarnings("unchecked")
    public void setIdAttribute(String value) {
	ByteArrayAttribute attr = new ByteArrayAttribute(Attribute.ID);
	attr.setByteArrayValue(DatatypeConverter.parseHexBinary(value));
	attributeTable_.put(Attribute.ID, attr);
    }

    @SuppressWarnings("unchecked")
    public void setLabelAttribute(String value) {
	CharArrayAttribute attr = new CharArrayAttribute(Attribute.LABEL);
	attr.setCharArrayValue(value.toCharArray());
	attributeTable_.put(Attribute.LABEL, attr);
    }
}
