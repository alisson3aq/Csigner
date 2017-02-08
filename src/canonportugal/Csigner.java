/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package canonportugal;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.CrlClient;
import com.itextpdf.text.pdf.security.CrlClientOnline;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.ProviderDigest;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.poreid.config.POReIDConfig;
import org.poreid.crypto.POReIDProvider;

/**
 *
 * @author i.lourenco
 */
public class Csigner {
    private static String sourceFile;
    private static boolean useLTV;
    private static String workingDir = "C:\\temp\\";

    public static void main(String[] args) {

        Csigner csigner = new Csigner();

//        try {
//            if (!getConfig()) {
//                JOptionPane.showMessageDialog(null, "Erro no ficheiro de configuração.");
//                System.err.println("false");
//                return;
//            }
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(null, e.getMessage());
//            System.err.println("false");
//            return;
//        }
        
        if(args.length < 2){
            JOptionPane.showMessageDialog(null, "Argumentos inválidos");
            System.err.println("false");
            return;
        }
        
        if(!args[0].contains(".pdf")){
            JOptionPane.showMessageDialog(null, "PDF não encontrado.");
            System.err.println("false");
            return;
        }
        
        sourceFile = workingDir + args[0];
        useLTV = Boolean.parseBoolean(args[1]);
        
        
        
        
        try {
            
            Security.addProvider(new POReIDProvider());
            
            String destFile = sourceFile.replace(".pdf", ".signed.pdf");

            signPdf(sourceFile, destFile, useLTV);

            System.out.println("true");

            JOptionPane.showMessageDialog(null, "Documento assinado com sucesso.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Erro", ERROR_MESSAGE);
            System.err.println("false");
        }

    }

    /**
     *
     * @param src Source file
     * @param dest Destination file
     * @param useLTV If uses Long Term Validation or not
     * @throws IOException
     * @throws DocumentException
     * @throws GeneralSecurityException
     */
    public static void signPdf(String src, String dest, boolean useLTV)
            throws IOException, DocumentException, GeneralSecurityException {

        KeyStore ks = KeyStore.getInstance(POReIDConfig.POREID);
        ks.load(null);
        PrivateKey pk = (PrivateKey) ks.getKey(POReIDConfig.ASSINATURA, null);
        Certificate[] chain = ks.getCertificateChain(POReIDConfig.ASSINATURA);

        // reader and stamper
        PdfReader reader = new PdfReader(src);
        FileOutputStream os = new FileOutputStream(dest);

        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', null, true);

        // appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();

        appearance.setReason("qualquer motivo");
        appearance.setLocation("qualquer localização");
        appearance.setVisibleSignature(new Rectangle(72, 732, 144, 780), 1, null);

        // timestamp
        TSAClient tsc = new TSAClientBouncyCastle("http://ts.cartaodecidadao.pt/tsa/server", "", "");

        // OCSP
        OcspClient ocsp = new OcspClientBouncyCastle();

        List<CrlClient> crlList = new ArrayList<>();
        crlList.add(new CrlClientOnline(chain));

        // digital signature
        ExternalSignature es = new PrivateKeySignature(pk, "SHA-256", POReIDConfig.POREID);
        ExternalDigest digest = new ProviderDigest(null);
        if (useLTV) {
            MakeSignature.signDetached(appearance, digest, es, chain, crlList, ocsp, tsc, 0, CryptoStandard.CMS);

            
        } else {
            MakeSignature.signDetached(appearance, digest, es, chain, null, null, tsc, 0, CryptoStandard.CMS);
        }

    }

    /**
     * Gets the configuration XML, parse it and assign it to variables. After
     * processing the XML it deletes the file.
     *
     * @return
     */
    private static Boolean getConfig() {
        /*
        
        Exemplo config.xml:
            
            <?xml version="1.0"?>
            <cpt>
                <config>
                    <tempDir>c:\temp\</tempDir>
                </config>
            </cpt>
         */

        try {
            File inputFile = new File("config.xml");
            
            if(inputFile.exists() == false){
                return false;
            }
            
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("config");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    
                    workingDir = eElement.getElementsByTagName("tempDir").item(0).getTextContent();
                    
//                    sourceFile = eElement.getElementsByTagName("tempdir").item(0).getTextContent() + "\\" + eElement.getElementsByTagName("file").item(0).getTextContent();
//                    useLTV = Boolean.parseBoolean(eElement.getElementsByTagName("ltv").item(0).getTextContent());
                    
//                    System.out.println("file : " + sourceFile);
//                    System.out.println("file : " + useLTV);
                    
                }
            }
            
            if (!sourceFile.contains(".pdf")) {
                JOptionPane.showMessageDialog(null, "PDF não encontrado.");
                return false;
            }
            
            
            
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return false;
        }

        return true;
    }

}
