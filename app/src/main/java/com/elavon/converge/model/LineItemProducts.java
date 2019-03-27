package com.elavon.converge.model;

import org.simpleframework.xml.ElementList;

import java.util.List;

public class LineItemProducts {

    @ElementList(name = "product", required = false)
    private List<Product> product;

    public List<Product> getProduct() {
        return product;
    }

    public void setProduct(List<Product> product) {
        this.product = product;
    }
}