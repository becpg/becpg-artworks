package fr.becpg.artworks.signature;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.springframework.stereotype.Service;

/**
 * Utility class for the signature / timestamp examples.
 * 
 * @author Tilman Hausherr, Valentin Leblanc
 */

@Service
public class SignatureUtils {
	
    	private static final String REFERENCE = "Reference";

	private static final String TRANSFORM_METHOD = "TransformMethod";

	private static final String TRANSFORM_PARAMS = "TransformParams";

	private static final Log logger = LogFactory.getLog(SignatureUtils.class);

	private static String encryptionKeystoreType;
	
	private static String encryptionKeystoreLocation;
	
	private static String metadataEncryptionPassword;

	private static KeyStore alfrescoKeyStore;
	
	private static Map<String, PrivateKey> signaturePrivateKeyCache = new HashMap<>();
	
	private static Map<String, Certificate[]> certificateChainCache = new HashMap<>();
	
	static {
		encryptionKeystoreType = System.getProperty("encryption.keystore.type");
		encryptionKeystoreLocation = System.getProperty("encryption.keystore.location");
		metadataEncryptionPassword = System.getProperty("metadata-keystore.password");
	}
	
	private SignatureUtils() {
		
	}
	
	private static KeyStore getAlfrescoKeyStore() {
		
		if (alfrescoKeyStore == null) {
			
			try {
				alfrescoKeyStore = KeyStore.getInstance(encryptionKeystoreType);
			} catch (KeyStoreException e) {
				throw new SignatureException("Failed to load alfresco keystore");
			}
			
			if (alfrescoKeyStore == null) {
				throw new IllegalStateException("Alfresco keystore cannot be found");
			}
			
			try (FileInputStream in = new FileInputStream(encryptionKeystoreLocation)){
				alfrescoKeyStore.load(in, metadataEncryptionPassword.toCharArray());
			} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
				throw new SignatureException("Failed to load alfresco keystore at : " + encryptionKeystoreLocation);
			}
		}
		
		return alfrescoKeyStore;
	}
	
	public static PrivateKey getSignaturePrivateKey(String alias, String password) {
		
		if (signaturePrivateKeyCache.get(alias) == null) {
			try {
				signaturePrivateKeyCache.put(alias, (PrivateKey) getAlfrescoKeyStore().getKey(alias, password.toCharArray()));
			} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e1) {
				throw new SignatureException("Failed to get private key from store for alias : " + alias);
			}
		}
		
		return signaturePrivateKeyCache.get(alias);
	}
	
	public static Certificate[] getCertificateChain(String alias) {
		
		if (alias != null && certificateChainCache.get(alias) == null) {
			try {
				certificateChainCache.put(alias, getAlfrescoKeyStore().getCertificateChain(alias));
			} catch (KeyStoreException e) {
				throw new SignatureException("Failed to get certificate chain from store for alias : " + alias);
			}
		}
		
		return certificateChainCache.getOrDefault(alias, certificateChainCache.values().iterator().next());
	}
	
	 /**
     * Get the access permissions granted for this document in the DocMDP transform parameters
     * dictionary. Details are described in the table "Entries in the DocMDP transform parameters
     * dictionary" in the PDF specification.
     *
     * @param doc document.
     * @return the permission value. 0 means no DocMDP transform parameters dictionary exists. Other
     * return values are 1, 2 or 3. 2 is also returned if the DocMDP transform parameters dictionary
     * is found but did not contain a /P entry, or if the value is outside the valid range.
     */
    public static int getMDPPermission(PDDocument doc)
    {
        COSDictionary permsDict = doc.getDocumentCatalog().getCOSObject()
                .getCOSDictionary(COSName.PERMS);
        if (permsDict != null)
        {
            COSDictionary signatureDict = permsDict.getCOSDictionary(COSName.DOCMDP);
            if (signatureDict != null)
            {
                COSArray refArray = signatureDict.getCOSArray(COSName.getPDFName(REFERENCE));
                if (refArray != null)
                {
                    for (int i = 0; i < refArray.size(); ++i)
                    {
                        COSBase base = refArray.getObject(i);
                        if (base instanceof COSDictionary)
                        {
                            COSDictionary sigRefDict = (COSDictionary) base;
                            if (COSName.DOCMDP.equals(sigRefDict.getDictionaryObject(COSName.getPDFName(TRANSFORM_METHOD))))
                            {
                                base = sigRefDict.getDictionaryObject(COSName.getPDFName(TRANSFORM_PARAMS));
                                if (base instanceof COSDictionary)
                                {
                                    COSDictionary transformDict = (COSDictionary) base;
                                    int accessPermissions = transformDict.getInt(COSName.P, 2);
                                    if (accessPermissions < 1 || accessPermissions > 3)
                                    {
                                        accessPermissions = 2;
                                    }
                                    return accessPermissions;
                                }
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Set the "modification detection and prevention" permissions granted for this document in the
     * DocMDP transform parameters dictionary. Details are described in the table "Entries in the
     * DocMDP transform parameters dictionary" in the PDF specification.
     *
     * @param doc The document.
     * @param signature The signature object.
     * @param accessPermissions The permission value (1, 2 or 3).
     *
     * @throws IOException if a signature exists.
     */
    public static void setMDPPermission(PDDocument doc, PDSignature signature, int accessPermissions)
            throws IOException
    {
        for (PDSignature sig : doc.getSignatureDictionaries())
        {
            // "Approval signatures shall follow the certification signature if one is present"
            // thus we don't care about timestamp signatures
            if (COSName.DOC_TIME_STAMP.equals(sig.getCOSObject().getItem(COSName.TYPE)))
            {
                continue;
            }
            if (sig.getCOSObject().containsKey(COSName.CONTENTS))
            {
                throw new IOException("DocMDP transform method not allowed if an approval signature exists");
            }
        }

        COSDictionary sigDict = signature.getCOSObject();

        // DocMDP specific stuff
        COSDictionary transformParameters = new COSDictionary();
        transformParameters.setItem(COSName.TYPE, COSName.getPDFName(TRANSFORM_PARAMS));
        transformParameters.setInt(COSName.P, accessPermissions);
        transformParameters.setName(COSName.V, "1.2");
        transformParameters.setNeedToBeUpdated(true);

        COSDictionary referenceDict = new COSDictionary();
        referenceDict.setItem(COSName.TYPE, COSName.getPDFName("SigRef"));
        referenceDict.setItem(COSName.getPDFName(TRANSFORM_METHOD), COSName.DOCMDP);
        referenceDict.setItem(COSName.DIGEST_METHOD, COSName.getPDFName("SHA1"));
        referenceDict.setItem(COSName.getPDFName(TRANSFORM_PARAMS), transformParameters);
        referenceDict.setNeedToBeUpdated(true);

        COSArray referenceArray = new COSArray();
        referenceArray.add(referenceDict);
        sigDict.setItem(COSName.getPDFName(REFERENCE), referenceArray);
        referenceArray.setNeedToBeUpdated(true);

        // Catalog
        COSDictionary catalogDict = doc.getDocumentCatalog().getCOSObject();
        COSDictionary permsDict = new COSDictionary();
        catalogDict.setItem(COSName.PERMS, permsDict);
        permsDict.setItem(COSName.DOCMDP, signature);
        catalogDict.setNeedToBeUpdated(true);
        permsDict.setNeedToBeUpdated(true);
    }

    /**
     * Log if the certificate is not valid for signature usage. Doing this
     * anyway results in Adobe Reader failing to validate the PDF.
     *
     * @param x509Certificate 
     * @throws java.security.cert.CertificateParsingException 
     */
    public static void checkCertificateUsage(X509Certificate x509Certificate)
            throws CertificateParsingException
    {
        // Check whether signer certificate is "valid for usage"
        // https://stackoverflow.com/a/52765021/535646
        // https://www.adobe.com/devnet-docs/acrobatetk/tools/DigSig/changes.html#id1
        boolean[] keyUsage = x509Certificate.getKeyUsage();
        if (keyUsage != null && !keyUsage[0] && !keyUsage[1])
        {
            // (unclear what "signTransaction" is)
            // https://tools.ietf.org/html/rfc5280#section-4.2.1.3
            logger.error("Certificate key usage does not include " +
                    "digitalSignature nor nonRepudiation");
        }
        List<String> extendedKeyUsage = x509Certificate.getExtendedKeyUsage();
        if (extendedKeyUsage != null &&
            !extendedKeyUsage.contains(KeyPurposeId.id_kp_emailProtection.toString()) &&
            !extendedKeyUsage.contains(KeyPurposeId.id_kp_codeSigning.toString()) &&
            !extendedKeyUsage.contains(KeyPurposeId.anyExtendedKeyUsage.toString()) &&
            !extendedKeyUsage.contains("1.2.840.113583.1.1.5") &&
            // not mentioned in Adobe document, but tolerated in practice
            !extendedKeyUsage.contains("1.3.6.1.4.1.311.10.3.12"))
        {
            logger.error("Certificate extended key usage does not include " +
                    "emailProtection, nor codeSigning, nor anyExtendedKeyUsage, " +
                    "nor 'Adobe Authentic Documents Trust'");
        }
    }

    /**
     * Log if the certificate is not valid for timestamping.
     *
     * @param x509Certificate 
     * @throws java.security.cert.CertificateParsingException 
     */
    public static void checkTimeStampCertificateUsage(X509Certificate x509Certificate)
            throws CertificateParsingException
    {
        List<String> extendedKeyUsage = x509Certificate.getExtendedKeyUsage();
        // https://tools.ietf.org/html/rfc5280#section-4.2.1.12
        if (extendedKeyUsage != null &&
            !extendedKeyUsage.contains(KeyPurposeId.id_kp_timeStamping.toString()))
        {
            logger.error("Certificate extended key usage does not include timeStamping");
        }
    }

    /**
     * Log if the certificate is not valid for responding.
     *
     * @param x509Certificate 
     * @throws java.security.cert.CertificateParsingException 
     */
    public static void checkResponderCertificateUsage(X509Certificate x509Certificate)
            throws CertificateParsingException
    {
        List<String> extendedKeyUsage = x509Certificate.getExtendedKeyUsage();
        // https://tools.ietf.org/html/rfc5280#section-4.2.1.12
        if (extendedKeyUsage != null &&
            !extendedKeyUsage.contains(KeyPurposeId.id_kp_OCSPSigning.toString()))
        {
            logger.error("Certificate extended key usage does not include OCSP responding");
        }
    }

    /**
     * Gets the last relevant signature in the document, i.e. the one with the highest offset.
     * 
     * @param document to get its last signature
     * @return last signature or null when none found
     * @throws IOException 
     */
    public static PDSignature getLastRelevantSignature(PDDocument document) throws IOException
    {
        Comparator<PDSignature> comparatorByOffset =
                Comparator.comparing(sig -> sig.getByteRange()[1]);

        // we can't use getLastSignatureDictionary() because this will fail (see PDFBOX-3978) 
        // if a signature is assigned to a pre-defined empty signature field that isn't the last.
        // we get the last in time by looking at the offset in the PDF file.
        Optional<PDSignature> optLastSignature =
                document.getSignatureDictionaries().stream().
                sorted(comparatorByOffset.reversed()).
                findFirst();
        if (optLastSignature.isPresent())
        {
            PDSignature lastSignature = optLastSignature.get();
            COSBase type = lastSignature.getCOSObject().getItem(COSName.TYPE);
            if (type == null || COSName.SIG.equals(type) || COSName.DOC_TIME_STAMP.equals(type))
            {
                return lastSignature;
            }
        }
        return null;
    }

    public static TimeStampToken extractTimeStampTokenFromSignerInformation(SignerInformation signerInformation)
            throws CMSException, IOException, TSPException
    {
        if (signerInformation.getUnsignedAttributes() == null)
        {
            return null;
        }
        AttributeTable unsignedAttributes = signerInformation.getUnsignedAttributes();
        // https://stackoverflow.com/questions/1647759/how-to-validate-if-a-signed-jar-contains-a-timestamp
        Attribute attribute = unsignedAttributes.get(
                PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
        if (attribute == null)
        {
            return null;
        }
        ASN1Object obj = (ASN1Object) attribute.getAttrValues().getObjectAt(0);
        CMSSignedData signedTSTData = new CMSSignedData(obj.getEncoded());
        return new TimeStampToken(signedTSTData);
    }

    public static void validateTimestampToken(TimeStampToken timeStampToken)
            throws TSPException, CertificateException, OperatorCreationException, IOException
    {
        // https://stackoverflow.com/questions/42114742/
        @SuppressWarnings("unchecked") // TimeStampToken.getSID() is untyped
        Collection<X509CertificateHolder> tstMatches =
                timeStampToken.getCertificates().getMatches(timeStampToken.getSID());
        X509CertificateHolder certificateHolder = tstMatches.iterator().next();
        SignerInformationVerifier siv = 
                new JcaSimpleSignerInfoVerifierBuilder().setProvider(SecurityProvider.getProvider()).build(certificateHolder);
        timeStampToken.validate(siv);
    }

    /**
     * Get certificate of a TSA.
     * 
     * @param tsaUrl URL
     * @return the X.509 certificate.
     *
     * @throws GeneralSecurityException
     * @throws IOException 
     */
    public static X509Certificate getTsaCertificate(String tsaUrl)
            throws GeneralSecurityException, IOException
    {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        TSAClient tsaClient = new TSAClient(new URL(tsaUrl), null, null, digest);
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TimeStampToken timeStampToken = tsaClient.getTimeStampToken(emptyStream);
        return getCertificateFromTimeStampToken(timeStampToken);
    }

    /**
     * Extract X.509 certificate from a timestamp
     * @param timeStampToken
     * @return the X.509 certificate.
     * @throws CertificateException 
     */
    public static X509Certificate getCertificateFromTimeStampToken(TimeStampToken timeStampToken)
            throws CertificateException
    {
        @SuppressWarnings("unchecked") // TimeStampToken.getSID() is untyped
        Collection<X509CertificateHolder> tstMatches =
                timeStampToken.getCertificates().getMatches(timeStampToken.getSID());
        X509CertificateHolder tstCertHolder = tstMatches.iterator().next();
        return new JcaX509CertificateConverter().getCertificate(tstCertHolder);
    }
}
