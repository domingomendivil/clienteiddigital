package udelar;

import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.credential_definition.CredentialDefinition;
import org.hyperledger.aries.api.credentials.Credential;
import org.hyperledger.aries.api.credentials.CredentialAttributes;
import org.hyperledger.aries.api.credentials.CredentialPreview;
import org.hyperledger.aries.api.issue_credential_v1.V1CredentialCreate;
import org.hyperledger.aries.api.present_proof.PresentProofRequest;
import org.hyperledger.aries.api.present_proof.PresentationExchangeRecord;
import org.hyperledger.aries.api.present_proof.PresentationRequest;
import org.hyperledger.aries.api.schema.SchemaSendRequest;
import org.hyperledger.aries.api.schema.SchemasCreatedFilter;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class Cliente implements Agente{


    private static final String DID = "A9A3zmbBnPT6RcrKvTf9q7";
    private AriesClient ac;

    private static final String TITLE="titulo";

    private static final String CREDENTIAL_DEF_ID="";
    


    public void connect(String url ){
        ac = AriesClient
                .builder()
                .url(url) // optional - defaults to localhost:8031
                .build();
    }


    private void addAttribute(IssueCredentialRequest credReq, String name, String value){
        Attribute attr = new Attribute();
        attr.name=name;
        attr.value=value;
        if (credReq.attributes==null)
            credReq.attributes=new ArrayList<>();
        credReq.attributes.add(attr);
    }
    public static void main(String[] args) throws IOException {
        AriesClient ac = AriesClient
                .builder()
                .url("http://localhost:8102") // optional - defaults to localhost:8031
                .build();
        Cliente cliente = new Cliente();
        cliente.connect("http://localhost:8102");

        String title = TITLE;
        String cedula = "cedula";
        String schemaId = cliente.createSchema("certificados6","1.0",title,cedula);

        String credDefId= cliente.createCredentialDefinition(schemaId);

        IssueCredentialRequest credReq = new IssueCredentialRequest();
        credReq.schemaId = schemaId;
        credReq.credDefId = credDefId;
        credReq.issuerDid = DID;
        credReq.schemaIssuerDid = DID;
        cliente.addAttribute(credReq,title,"The God");
        cliente.addAttribute(credReq,cedula,"2");
        cliente.issueCredential(credReq);


        udelar.PresentProofRequest pref = new udelar.PresentProofRequest();
        pref.comment="Probar que es Ingeniero";
        pref.attribute="titulo";
        cliente.presentProofSendRequest(pref);

    }


    public void connectID(){

    }




    private String createCredentialDefinition(String schemaId) throws IOException {
        var builder = CredentialDefinition.CredentialDefinitionRequest.builder();
        builder.revocationRegistrySize(100).schemaId(schemaId);
        CredentialDefinition.CredentialDefinitionRequest defReq = builder.build();
        var def= ac.credentialDefinitionsCreate(defReq);
        return def.get().getCredentialDefinitionId();
    }

    private List<String> toList(String... attrs){
       return Arrays.stream(attrs).collect(Collectors.toList());
    }

    private  String createSchema(String schemaName, String schemaVersion,String... attrs) throws IOException {
        var list = toList(attrs);
      SchemaSendRequest schema = SchemaSendRequest.builder().schemaName(schemaName).attributes(list).schemaVersion(schemaVersion).build();
      var res= ac.schemas(schema);
      return res.get().getSchema().getId();
    }

    private  void listarSchemas(Optional<List<String>> list) {
        list.stream().forEach(p -> System.out.println(p));
    }

    public String issueCredential(IssueCredentialRequest req ) throws IOException {
        var builder=V1CredentialCreate.builder();
        builder = builder.credDefId(req.credDefId).schemaId(req.schemaId).schemaIssuerDid(req.schemaIssuerDid).issuerDid(req.issuerDid);
        CredentialPreview proposal = new CredentialPreview();
        List<CredentialAttributes> attrs = new ArrayList<>();
        req.attributes.stream().forEach( attr -> {
            CredentialAttributes credAttr = new CredentialAttributes();
            credAttr.setName(attr.name);
            credAttr.setValue(attr.value);
            attrs.add(credAttr);
        });
        proposal.setAttributes(attrs);
        builder.credentialProposal(proposal);
        var credDefReq = builder.build();
        var res = ac.issueCredentialCreate(credDefReq);
        return res.get().getCredentialId();
    }

    public void sendPresentation(String presentationExchangeId,PresentationRequest presentationRequest) throws IOException {
        ac.presentProofRecordsSendPresentation(presentationExchangeId,presentationRequest);
    }

    public void createPresentProof10Request(udelar.PresentProofRequest pref) throws IOException {
        var builder= PresentProofRequest.builder();
        var proofRequestBuilder = PresentProofRequest.ProofRequest.builder();
        String requestedAttribute="";
        var attributesBuilder = PresentProofRequest.ProofRequest.ProofRequestedAttributes.builder();
        attributesBuilder.names(pref.attributes);
        PresentProofRequest.ProofRequest.ProofRequestedAttributes value = attributesBuilder.build();
        proofRequestBuilder.requestedAttribute(requestedAttribute,value);
        PresentProofRequest.ProofRequest proofRequest = proofRequestBuilder.build();
        builder.comment(pref.comment).proofRequest(proofRequest);
        builder.connectionId(pref.connectionId);
        var req = builder.build();
        Optional<PresentationExchangeRecord> presentationExchangeRecord = ac.presentProofCreateRequest(req);

   }

   public void fetchCredentialsFromWallet(String exchangeId) throws IOException {
        var res=ac.presentProofRecordsCredentials(exchangeId);
   }


   public String  presentProofSendRequest(udelar.PresentProofRequest pref) throws IOException {
       PresentProofRequest presentProofRequest = new PresentProofRequest();
       presentProofRequest.setComment(pref.comment);
       PresentProofRequest.ProofRequest.ProofRequestBuilder proofReqBuilder = PresentProofRequest.ProofRequest.builder();
       var attributesBuilder = PresentProofRequest.ProofRequest.ProofRequestedAttributes.builder();
       attributesBuilder.names(pref.attributes);
       PresentProofRequest.ProofRequest.ProofRequestedAttributes value = attributesBuilder.build();
       proofReqBuilder  = proofReqBuilder.requestedAttribute(pref.attribute,value);
       PresentProofRequest. ProofRequest proofRequest = proofReqBuilder.build();
       presentProofRequest.setProofRequest(proofRequest);
       var res = ac.presentProofSendRequest(presentProofRequest);
       return res.get().getPresentationExchangeId();
   }


    public Optional<PresentationExchangeRecord> verifyPresentation(String presentationExchangeId) throws IOException {
        return ac.presentProofRecordsVerifyPresentation(presentationExchangeId);
    }

    private int getTimeStamp(){
        ZoneOffset zoneOffSet = ZoneOffset.ofHours(-3);
        return (int) LocalDate.now().toEpochSecond(LocalTime.now(),zoneOffSet);
    }

    private void sendPresentation(String exchangeId,String credentialDefId,String attribute) throws IOException {
        Optional<Credential> credential =getCredentialByDefinitionId(credentialDefId);
        var builder= PresentationRequest.builder();
        Map<String, PresentationRequest.IndyRequestedCredsRequestedAttr> attrs = new HashMap<>();
        var presentProofBuilder =PresentationRequest.IndyRequestedCredsRequestedAttr.builder();
        presentProofBuilder.revealed(true).credId(credential.get().getReferent()).timestamp(getTimeStamp());
        PresentationRequest.IndyRequestedCredsRequestedAttr presentProofAttr = presentProofBuilder.build();
        attrs.put(attribute,presentProofAttr);
        builder.requestedAttributes(attrs);
        PresentationRequest presentationRequest = builder.build();
       var res= ac.presentProofRecordsSendPresentation(exchangeId,presentationRequest);
        String state=res.get().getState().name();
    }

    private Optional<Credential> getCredentialByDefinitionId(String id) throws IOException {
        var res = ac.credentials();
        var referent;
        var list= res.get().stream().filter( c -> c.getCredentialDefinitionId().equals(id)).collect(Collectors.toList());
        if (list.size()==1){
           return Optional.of(list.get(0));
        }else {)
            Optional.empty();
        }
    }


    @Override
    public void sendPresentationProof(String exchangeId) {
        sendPresentation(exchangeId,CREDENTIAL_DEF_ID(),TITLE);
    }
}
