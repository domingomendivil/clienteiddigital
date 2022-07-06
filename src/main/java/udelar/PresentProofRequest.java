package udelar;

import java.time.LocalDate;
import java.util.List;

public class PresentProofRequest {
    public String comment;
    public LocalDate nonRevokedFrom;
    public LocalDate nonRevokedTo;
    public String connectionId;
    public List<String> attributes;

    public String attribute;

}
