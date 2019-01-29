package com.elavon.converge.model;

import org.simpleframework.xml.ElementList;

import java.util.List;

public class LineItemProducts {

    public List<Product> getProduct() {
        return product;
    }

    public void setProduct(List<Product> product) {
        this.product = product;
    }

    @ElementList(name = "product", required = false)
    private List<Product> product;
}