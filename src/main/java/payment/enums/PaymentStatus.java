package payment.enums;

public enum PaymentStatus {
    ERROR(""),
    DONE("SUCCESS"),
    PENDING("PENDING"),
    FAILED("FAILED");

    private final String status;

    PaymentStatus(String status){
        this.status = status;
    }

    public String getString(){
        return status;
    }
}
