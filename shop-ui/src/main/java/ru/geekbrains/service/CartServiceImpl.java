package ru.geekbrains.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import ru.geekbrains.controller.repr.ProductRepr;
import ru.geekbrains.service.model.LineItem;
import ru.geekbrains.service.model.LineItemBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CartServiceImpl implements CartService, Serializable {

    private final Map<LineItem, Integer> lineItems;

    public CartServiceImpl() {
        this.lineItems = new HashMap<>();
    }

    @JsonCreator
    public CartServiceImpl(@JsonProperty("lineItems") List<LineItem> lineItems) {
        this.lineItems = lineItems.stream().collect(Collectors.toMap(li -> li, LineItem::getQty));
    }

    @Override
    public void addProductQty(ProductRepr productRepr, String color, String size, int qty) {
        LineItem lineItem = new LineItemBuilder(productRepr).color(color).size(size).qty(qty).build();
        lineItems.put(lineItem, lineItems.getOrDefault(lineItem, 0) + qty);
    }

    @Override
    public void adjustQty(ProductRepr productRepr, String color, String size, int adjQty) {
        LineItem lineItem = new LineItemBuilder(productRepr).color(color).size(size).qty(adjQty).build();
        int qty = lineItems.get(lineItem);
        int diff = adjQty - qty;
        if (adjQty == 0) {
            lineItems.remove(lineItem);
        }else lineItems.put(lineItem, lineItems.getOrDefault(lineItem, 0) + diff);
    }

    @Override
    public void removeProductQty(ProductRepr productRepr, String color, String size, int qty) {
        LineItem lineItem = new LineItemBuilder(productRepr).color(color).size(size).qty(qty).build();
        int currentQty = lineItems.getOrDefault(lineItem, 0);
        if (currentQty - qty > 0) {
            lineItems.put(lineItem, currentQty - qty);
        } else {
            lineItems.remove(lineItem);
        }
    }

    @Override
    public List<LineItem> getLineItems() {
        lineItems.forEach(LineItem::setQty);
        return new ArrayList<>(lineItems.keySet());
    }

    @JsonIgnore
    @Override
    public BigDecimal getSubTotal() {
        lineItems.forEach(LineItem::setQty);
        return lineItems.keySet().stream()
                .map(LineItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
