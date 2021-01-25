package pl.edu.pw.aasd.data;

import pl.edu.pw.aasd.Jsonable;

public class PromotionReservationRequest extends Jsonable {
    private String partner;
    private String promotionId;
    private String userId;

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
