package com.elavon.converge.model;

import org.simpleframework.xml.Element;

import java.math.BigDecimal;

public class Product {

    public Product() {

    }

    @Element(name = "ssl_line_Item_description", required = false)
    private String productItemDescription;

    @Element(name = "ssl_line_Item_product_code", required = false)
    private String productItemCode;

    @Element(name = "ssl_line_Item_commodity_code", required = false)
    private String productItemCommodityCode;

    @Element(name = "ssl_line_Item_quantity", required = false)
    private Float productItemQuantity;

    @Element(name = "ssl_line_Item_unit_of_measure", required = false)
    private String productItemUom;

    @Element(name = "ssl_line_Item_unit_cost", required = false)
    private BigDecimal productItemUnitCost;

    @Element(name = "ssl_line_Item_discount_indicator", required = false)
    private String productItemDiscountIndicator;

    @Element(name = "ssl_line_Item_discount_amount", required = false)
    private BigDecimal productItemDiscount;

    @Element(name = "ssl_line_Item_tax_Indicator", required = false)
    private String productItemTaxIndicator;

    @Element(name = "ssl_line_Item_tax_rate", required = false)
    private BigDecimal productItemTaxRate;

    @Element(name = "ssl_line_Item_tax_amount", required = false)
    private BigDecimal productItemTaxAmount;

    @Element(name = "ssl_line_Item_tax_type", required = false)
    private String productItemTaxType;

    @Element(name = "ssl_line_Item_extended_total", required = false)
    private BigDecimal productItemExtendedTotal;

    @Element(name = "ssl_line_Item_total", required = false)
    private BigDecimal productItemTotal;

    @Element(name = "ssl_line_Item_alternative_tax", required = false)
    private String productItemAlternativeTax;

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

    public String getProductItemCommodityCode() {
        return productItemCommodityCode;
    }

    public void setProductItemCommodityCode(String productItemCommodityCode) {
        this.productItemCommodityCode = productItemCommodityCode;
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

    public BigDecimal getProductItemUnitCost() {
        return productItemUnitCost;
    }

    public void setProductItemUnitCost(BigDecimal productItemUnitCost) {
        this.productItemUnitCost = productItemUnitCost;
    }

    public String getProductItemDiscountIndicator() {
        return productItemDiscountIndicator;
    }

    public void setProductItemDiscountIndicator(String productItemDiscountIndicator) {
        this.productItemDiscountIndicator = productItemDiscountIndicator;
    }

    public BigDecimal getProductItemDiscount() {
        return productItemDiscount;
    }

    public void setProductItemDiscount(BigDecimal productItemDiscount) {
        this.productItemDiscount = productItemDiscount;
    }

    public String getProductItemTaxIndicator() {
        return productItemTaxIndicator;
    }

    public void setProductItemTaxIndicator(String productItemTaxIndicator) {
        this.productItemTaxIndicator = productItemTaxIndicator;
    }

    public BigDecimal getProductItemTaxRate() {
        return productItemTaxRate;
    }

    public void setProductItemTaxRate(BigDecimal productItemTaxRate) {
        this.productItemTaxRate = productItemTaxRate;
    }

    public BigDecimal getProductItemTaxAmount() {
        return productItemTaxAmount;
    }

    public void setProductItemTaxAmount(BigDecimal productItemTaxAmount) {
        this.productItemTaxAmount = productItemTaxAmount;
    }

    public String getProductItemTaxType() {
        return productItemTaxType;
    }

    public void setProductItemTaxType(String productItemTaxType) {
        this.productItemTaxType = productItemTaxType;
    }

    public BigDecimal getProductItemExtendedTotal() {
        return productItemExtendedTotal;
    }

    public void setProductItemExtendedTotal(BigDecimal productItemExtendedTotal) {
        this.productItemExtendedTotal = productItemExtendedTotal;
    }

    public BigDecimal getProductItemTotal() {
        return productItemTotal;
    }

    public void setProductItemTotal(BigDecimal productItemTotal) {
        this.productItemTotal = productItemTotal;
    }

    public String getProductItemAlternativeTax() {
        return productItemAlternativeTax;
    }

    public void setProductItemAlternativeTax(String productItemAlternativeTax) {
        this.productItemAlternativeTax = productItemAlternativeTax;
    }
}