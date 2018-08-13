package mobilePhoneNoFeatureEnvy;

public class Customer {
    private Phone mobilePhone;

    public String getMobilePhoneNumber() {
        return mobilePhone.toFormattedString();
    }
}