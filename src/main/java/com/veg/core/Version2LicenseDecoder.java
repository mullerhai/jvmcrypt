package com.veg.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import org.apache.commons.codec.binary.Base64;

public class Version2LicenseDecoder extends AbstractLicenseDecoder {
    public static final int VERSION_NUMBER_1 = 1;
    public static final int VERSION_NUMBER_2 = 2;
    public static final int VERSION_LENGTH = 3;
    public static final int ENCODED_LICENSE_LENGTH_BASE = 31;
    public static final byte[] LICENSE_PREFIX = new byte[]{13, 14, 12, 10, 15};
    public static final char SEPARATOR = 'X';
    private static final PublicKey PUBLIC_KEY;
    private static final int ENCODED_LICENSE_LINE_LENGTH = 76;

    public Version2LicenseDecoder() {
    }

    public boolean canDecode(String licenseString) {
        licenseString = removeWhiteSpaces(licenseString);
        int pos = licenseString.lastIndexOf(88);
        if (pos != -1 && pos + 3 < licenseString.length()) {
            try {
                int version = Integer.parseInt(licenseString.substring(pos + 1, pos + 3));
                if (version != 1 && version != 2) {
                    return false;
                } else {
                    String lengthStr = licenseString.substring(pos + 3);
                    int encodedLicenseLength = Integer.valueOf(lengthStr, 31);
                    return pos == encodedLicenseLength;
                }
            } catch (NumberFormatException var6) {
                return false;
            }
        } else {
            return false;
        }
    }

    public Properties doDecode(String licenseString) {
        String encodedLicenseTextAndHash = this.getLicenseContent(removeWhiteSpaces(licenseString));
        byte[] zippedLicenseBytes = this.checkAndGetLicenseText(encodedLicenseTextAndHash);
        Reader licenseText = this.unzipText(zippedLicenseBytes);
        return this.loadLicenseConfiguration(licenseText);
    }

    protected int getLicenseVersion() {
        return 2;
    }

    private Reader unzipText(byte[] licenseText) {
        ByteArrayInputStream in = new ByteArrayInputStream(licenseText);
        in.skip((long)LICENSE_PREFIX.length);
        InflaterInputStream zipIn = new InflaterInputStream(in, new Inflater());

        try {
            return new InputStreamReader(zipIn, "UTF-8");
        } catch (UnsupportedEncodingException var5) {
            throw new LicenseException(var5);
        }
    }

    private String getLicenseContent(String licenseString) {
        String lengthStr = licenseString.substring(licenseString.lastIndexOf(88) + 3);

        try {
            int encodedLicenseLength = Integer.valueOf(lengthStr, 31);
            return licenseString.substring(0, encodedLicenseLength);
        } catch (NumberFormatException var4) {
            throw new LicenseException("Could NOT decode license length <" + lengthStr + ">", var4);
        }
    }

    private byte[] checkAndGetLicenseText(String licenseContent) {
        try {
            byte[] decodedBytes = Base64.decodeBase64(licenseContent.getBytes());
            ByteArrayInputStream in = new ByteArrayInputStream(decodedBytes);
            DataInputStream dIn = new DataInputStream(in);
            int textLength = dIn.readInt();
            byte[] licenseText = new byte[textLength];
            dIn.read(licenseText);
            byte[] hash = new byte[dIn.available()];
            dIn.read(hash);

            try {
                Signature signature = Signature.getInstance("SHA1withDSA");
                signature.initVerify(PUBLIC_KEY);
                signature.update(licenseText);
                if (!signature.verify(hash)) {
                    throw new LicenseException("Failed to verify the license.");
                } else {
                    return licenseText;
                }
            } catch (InvalidKeyException var9) {
                throw new LicenseException(var9);
            } catch (SignatureException var10) {
                throw new LicenseException(var10);
            } catch (NoSuchAlgorithmException var11) {
                throw new LicenseException(var11);
            }
        } catch (IOException var12) {
            throw new LicenseException(var12);
        }
    }

    private Properties loadLicenseConfiguration(Reader text) {
        try {
            Properties props = new Properties();
            (new DefaultPropertiesPersister()).load(props, text);
            return props;
        } catch (IOException var3) {
            throw new LicenseException("Could NOT load properties from reader", var3);
        }
    }

    private static String removeWhiteSpaces(String licenseData) {
        if (licenseData != null && licenseData.length() != 0) {
            char[] chars = licenseData.toCharArray();
            StringBuffer buf = new StringBuffer(chars.length);

            for(int i = 0; i < chars.length; ++i) {
                if (!Character.isWhitespace(chars[i])) {
                    buf.append(chars[i]);
                }
            }

            return buf.toString();
        } else {
            return licenseData;
        }
    }

    public static String packLicense(byte[] text, byte[] hash) throws LicenseException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dOut = new DataOutputStream(out);
            dOut.writeInt(text.length);
            dOut.write(text);
            dOut.write(hash);
            byte[] allData = out.toByteArray();
            String result = (new String(Base64.encodeBase64(allData))).trim();
            result = result + 'X' + "0" + 2 + Integer.toString(result.length(), 31);
            result = split(result);
            return result;
        } catch (IOException var6) {
            throw new LicenseException(var6);
        }
    }

    private static String split(String licenseData) {
        if (licenseData != null && licenseData.length() != 0) {
            char[] chars = licenseData.toCharArray();
            StringBuffer buf = new StringBuffer(chars.length + chars.length / 76);

            for(int i = 0; i < chars.length; ++i) {
                buf.append(chars[i]);
                if (i > 0 && i % 76 == 0) {
                    buf.append('\n');
                }
            }

            return buf.toString();
        } else {
            return licenseData;
        }
    }

    static {
        try {
            String pubKeyEncoded = "MIHwMIGoBgcqhkjOOAQBMIGcAkEA/KaCzo4Syrom78z3EQ5SbbB4sF7ey80etKII864WF64B81uRpH5t9jQTxeEu0ImbzRMqzVDZkVG9xD7nN1kuFwIVAJYu3cw2nLqOuyYO5rahJtk0bjjFAkBnhHGyepz0TukaScUUfbGpqvJE8FpDTWSGkx0tFCcbnjUDC3H9c9oXkGmzLik1Yw4cIGI1TQ2iCmxBblC+eUykA0MAAkBrKJN92XEUFWggagAhhhNtFVc/Nh/JTnB3xsQ5azfHq7UcFtPEq0ohc3vGZ7OGEQS7Ym08DB6B1DtD93CwaNdX";
            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            PUBLIC_KEY = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decodeBase64("MIHwMIGoBgcqhkjOOAQBMIGcAkEA/KaCzo4Syrom78z3EQ5SbbB4sF7ey80etKII864WF64B81uRpH5t9jQTxeEu0ImbzRMqzVDZkVG9xD7nN1kuFwIVAJYu3cw2nLqOuyYO5rahJtk0bjjFAkBnhHGyepz0TukaScUUfbGpqvJE8FpDTWSGkx0tFCcbnjUDC3H9c9oXkGmzLik1Yw4cIGI1TQ2iCmxBblC+eUykA0MAAkBrKJN92XEUFWggagAhhhNtFVc/Nh/JTnB3xsQ5azfHq7UcFtPEq0ohc3vGZ7OGEQS7Ym08DB6B1DtD93CwaNdX".getBytes())));
        } catch (NoSuchAlgorithmException var2) {
            throw new Error(var2);
        } catch (InvalidKeySpecException var3) {
            throw new Error(var3);
        }
    }
}
