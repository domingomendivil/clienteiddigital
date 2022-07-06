package udelar;

import java.util.List;

public class IssueCredentialRequest {
    public String credDefId;
    public String schemaId;
    public String schemaIssuerDid;
    public String issuerDid;

    public List<Attribute> attributes;
}
