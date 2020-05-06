package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BookKeeperTest {

    private BookKeeper bookKeeper;
    private InvoiceRequest invoiceRequest;
    private TaxPolicy taxPolicy;
    private ProductBuilder productBuilder;
    private RequestItemBuilder requestItemBuilder;

    @BeforeEach
    public void init() {
        bookKeeper = new BookKeeper(new InvoiceFactory());
        invoiceRequest = new InvoiceRequest(new ClientData(Id.generate(),"Imie Nazwisko"));
        taxPolicy = Mockito.mock(TaxPolicy.class);
        Mockito.when(taxPolicy.calculateTax(any(ProductType.class),
                any(Money.class))).thenReturn(new Tax(Money.ZERO, "tax"));
        productBuilder = new ProductBuilder();
        requestItemBuilder = new RequestItemBuilder();
    }

    //State tests

    @Test
    public void shouldReturnInvoiceWithOneElement() {
        Product product = productBuilder.build();
        RequestItem requestItem = requestItemBuilder.withProductData(product.generateSnapshot()).build();
        invoiceRequest.add(requestItem);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        Assertions.assertEquals(1, invoice.getItems().size());
    }

    @Test
    public void shouldReturnInvoiceWithoutElements() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        Assertions.assertEquals(0, invoice.getItems().size());
    }

    @Test
    public void shouldReturnInvoiceWithThreeElements() {
        Product product1 = productBuilder.withName("bike").withProductType(ProductType.STANDARD).build();
        RequestItem requestItem1 = requestItemBuilder.withProductData(product1.generateSnapshot()).withQuantity(5)
                                    .withTotalCost(Money.ZERO).build();
        invoiceRequest.add(requestItem1);

        Product product2 = productBuilder.withName("vitamin").withProductType(ProductType.DRUG).build();
        RequestItem requestItem2 = requestItemBuilder.withProductData(product2.generateSnapshot()).withQuantity(3)
                .withTotalCost(Money.ZERO).build();
        invoiceRequest.add(requestItem2);

        Product product3 = productBuilder.withName("break").withProductType(ProductType.FOOD).build();
        RequestItem requestItem3 = requestItemBuilder.withProductData(product3.generateSnapshot()).withQuantity(7)
                .withTotalCost(Money.ZERO).build();
        invoiceRequest.add(requestItem3);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        Assertions.assertEquals(3, invoice.getItems().size());
    }

    //Behaviour Tests

    @Test
    public void calculateTaxShouldBeCalledTwoTimes() {
        Product product1 = productBuilder.withName("bike").withProductType(ProductType.STANDARD).build();
        RequestItem requestItem1 = requestItemBuilder.withProductData(product1.generateSnapshot()).withQuantity(5)
                .withTotalCost(Money.ZERO).build();
        invoiceRequest.add(requestItem1);

        Product product2 = productBuilder.withName("vitamin").withProductType(ProductType.DRUG).build();
        RequestItem requestItem2 = requestItemBuilder.withProductData(product2.generateSnapshot()).withQuantity(3)
                .withTotalCost(Money.ZERO).build();
        invoiceRequest.add(requestItem2);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(2)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void calculateTaxShouldNotBeCalled() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(0)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void calculateTaxShouldBeCalledThreeTimes() {
        Product product1 = productBuilder.withName("bike").withProductType(ProductType.STANDARD).build();
        RequestItem requestItem1 = requestItemBuilder.withProductData(product1.generateSnapshot()).withQuantity(5)
                .withTotalCost(Money.ZERO).build();
        invoiceRequest.add(requestItem1);

        Product product2 = productBuilder.withName("vitamin").withProductType(ProductType.DRUG).build();
        RequestItem requestItem2 = requestItemBuilder.withProductData(product2.generateSnapshot()).withQuantity(3)
                .withTotalCost(Money.ZERO).build();
        invoiceRequest.add(requestItem2);

        Product product3 = productBuilder.withName("break").withProductType(ProductType.FOOD).build();
        RequestItem requestItem3 = requestItemBuilder.withProductData(product3.generateSnapshot()).withQuantity(7)
                .withTotalCost(Money.ZERO).build();
        invoiceRequest.add(requestItem3);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(3)).calculateTax(any(ProductType.class), any(Money.class));
    }

}