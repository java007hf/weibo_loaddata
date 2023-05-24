package com.xsoft.weibo;

public class CommodityInfo {
    String shortTitle;
    String price;
    String priceAfterCoupon;
    String monthSellCount;
    String couponEffectiveStartTime;
    String couponEffectiveEndTime;
    String whiteImage;
    String url;
    String itemId;

    float price_float;
    float priceAfterCoupon_float;

    public void changeDataFormat() {
        price_float = Float.parseFloat (price);
        priceAfterCoupon_float = Float.parseFloat (priceAfterCoupon);
    }

    @Override
    public String toString () {
        return url;
//        return "CommodityInfo{" +
//                "shortTitle='" + shortTitle + '\'' +
//                ", price='" + price + '\'' +
//                ", priceAfterCoupon='" + priceAfterCoupon + '\'' +
//                ", monthSellCount='" + monthSellCount + '\'' +
//                ", couponEffectiveStartTime='" + couponEffectiveStartTime + '\'' +
//                ", couponEffectiveEndTime='" + couponEffectiveEndTime + '\'' +
//                ", whiteImage='" + whiteImage + '\'' +
//                ", url='" + url + '\'' +
//                '}';
    }
}
