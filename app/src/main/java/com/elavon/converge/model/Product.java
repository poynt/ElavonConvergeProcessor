package com.elavon.converge.model;

import org.simpleframework.xml.Element;

public class Product {

    public Product() {

    }

    @Element(name = "ssl_line_item_description", required = false)
    private String productItemDescription;

    @Element(name = "ssl_line_item_product_code", required = false)
    private String productItemCode;

    @Element(name = "ssl_line_item_commodity_code", required = false)
    private String productDescription;

    @Element(name = "ssl_line_item_quantity", required = false)
    private Float productItemQuantity;

    @Element(name = "ssl_line_item_unit_of_measure", required = false)
    private String productItemUom;

    @Element(name = "ssl_line_item_unit_cost", required = false)
    private Long productItemUnitCost;

    @Element(name = "ssl_line_item_discount_indicator", required = false)
    private String productItemDiscountIndicator;

    @Element(name = "ssl_line_item_discount_amount", required = false)
    private Long productItemDiscount;

    @Element(name = "ssl_line_item_tax_Indicator", required = false)
    private String productItemTaxIndicator;

    @Element(name = "ssl_line_item_tax_rate", required = false)
    private long productItemTaxRate;

    public String getProductItemDescription() {
        return productItemDescription;
    }

    public void setProductItemDescription(String productItemDescription) {
        this.productItemDescription = productItemDescription;
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public Float getProductItemQuantity() {
        return productItemQuantity;
    }

    public void setProductItemQuantity(Float productItemQuantity) {
        this.productItemQuantity = productItemQuantity;
    }

    public String getProductItemUom() {
        return productItemUom;
    }

    public void setProductItemUom(String productItemUom) {
        this.productItemUom = productItemUom;
    }

    public Long getProductItemUnitCost() {
        return productItemUnitCost;
    }

    public void setProductItemUnitCost(Long productItemUnitCost) {
        this.productItemUnitCost = productItemUnitCost;
    }

    public String getProductItemDiscountIndicator() {
        return productItemDiscountIndicator;
    }

    public void setProductItemDiscountIndicator(String productItemDiscountIndicator) {
        this.productItemDiscountIndicator = productItemDiscountIndicator;
    }

    public Long getProductItemDiscount() {
        return productItemDiscount;
    }

    public void setProductItemDiscount(Long productItemDiscount) {
        this.productItemDiscount = productItemDiscount;
    }

    public String getProductItemTaxIndicator() {
        return productItemTaxIndicator;
    }

    public void setProductItemTaxIndicator(String productItemTaxIndicator) {
        this.productItemTaxIndicator = productItemTaxIndicator;
    }

    public Long getProductItemTaxRate() {
        return productItemTaxRate;
    }

    public void setProductItemTaxRate(long productItemTaxRate) {
        this.productItemTaxRate = productItemTaxRate;
    }

    public String getProductItemTaxAmount() {
        return productItemTaxAmount;
    }

    public void setProductItemTaxAmount(String productItemTaxAmount) {
        this.productItemTaxAmount = productItemTaxAmount;
    }

    public String getProductItemTaxType() {
        return productItemTaxType;
    }

    public void setProductItemTaxType(String productItemTaxType) {
        this.productItemTaxType = productItemTaxType;
    }

    public String getProductItemExtendedTotal() {
        return productItemExtendedTotal;
    }

    public void setProductItemExtendedTotal(String productItemExtendedTotal) {
        this.productItemExtendedTotal = productItemExtendedTotal;
    }

    public Long getProductItemTotal() {
        return productItemTotal;
    }

    public void setProductItemTotal(Long productItemTotal) {
        this.productItemTotal = productItemTotal;
    }

    public String getProductItemAlternativeTax() {
        return productItemAlternativeTax;
    }

    public void setProductItemAlternativeTax(String productItemAlternativeTax) {
        this.productItemAlternativeTax = productItemAlternativeTax;
    }

    @Element(name = "ssl_line_item_tax_amount", required = false)
    private String productItemTaxAmount;

    @Element(name = "ssl_line_item_tax_type", required = false)
    private String productItemTaxType;

    @Element(name = "ssl_line_item_extended_total", required = false)
    private String productItemExtendedTotal;

    @Element(name = "ssl_line_item_total", required = false)
    private Long productItemTotal;

    @Element(name = "ssl_line_item_alternative_tax", required = false)
    private String productItemAlternativeTax;
}
