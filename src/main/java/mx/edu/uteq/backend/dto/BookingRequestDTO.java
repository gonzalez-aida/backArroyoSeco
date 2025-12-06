package mx.edu.uteq.backend.dto;

import java.util.Date;

public class BookingRequestDTO {
    private Long propertyId;
    private Long userId;
    //private Long paymentId;
    private String status;
    private Date startDate;
    private Date endDate;

    // Getters y Setters
    public Long getPropertyId() {
        return propertyId;
    }
    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    // public Long getPaymentId() {
    //     return paymentId;
    // }
    // public void setPaymentId(Long paymentId) {
    //     this.paymentId = paymentId;
    // }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}