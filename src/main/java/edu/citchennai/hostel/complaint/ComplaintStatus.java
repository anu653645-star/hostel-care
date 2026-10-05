package edu.citchennai.hostel.complaint;

public enum ComplaintStatus {
    WAITING_FOR_WARDEN("Waiting for warden"),
    WAITING_FOR_HOSTEL_AUNTY("Waiting for hostel aunty"),
    VISIT_SCHEDULED("Visit scheduled"),
    SOLVED("Problem solved");

    private final String label;

    ComplaintStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
