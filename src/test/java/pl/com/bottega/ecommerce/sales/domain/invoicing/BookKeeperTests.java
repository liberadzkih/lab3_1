package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;

public class BookKeeperTests {

    private BookKeeper bookKeeper = new BookKeeper(new InvoiceFactory());
    private InvoiceRequest invoiceRequest;
    private TaxPolicy taxPolicy;

    @Before
    public void initInvoiceRequest() {
        invoiceRequest = new InvoiceRequest(new ClientData(Id.generate(), "CBA"));

        taxPolicy = Mockito.mock(TaxPolicy.class);
        Mockito.when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class)))
               .thenReturn(new Tax(new Money(BigDecimal.ZERO), "Tax"));
    }

    @Test
    public void expectedInvoiceWithOneElement() {
        Product product = new ProductBuilder().build();
        RequestItem requestItem = new RequestItemBuilder().withProductData(product.generateSnapshot()).build();
        invoiceRequest.add(requestItem);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(1, equalTo(invoice.getItems().size()));
    }

    @Test
    public void expectedInvoiceWithoutElements() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(0, equalTo(invoice.getItems().size()));
    }

    @Test
    public void expectedSpecificInvoiceCost() {
        Product product1 = new ProductBuilder().withName("Banana").build();
        Product product2 = new ProductBuilder().withName("Orange").build();

        RequestItem requestItem1 = new RequestItemBuilder().withProductData(product1.generateSnapshot())
                                                           .withTotalCost(new Money(BigDecimal.valueOf(100)))
                                                           .build();
        RequestItem requestItem2 = new RequestItemBuilder().withProductData(product2.generateSnapshot())
                                                           .withTotalCost(new Money(BigDecimal.valueOf(200)))
                                                           .build();

        invoiceRequest.add(requestItem1);
        invoiceRequest.add(requestItem2);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(invoice.getNet(), equalTo(requestItem1.getTotalCost().add(requestItem2.getTotalCost())));
    }

    @Test
    public void expectedTwoCallsOfCalculateTax() {
        Product product1 = new ProductBuilder().withName("Aspirin").withProductType(ProductType.DRUG).build();
        Product product2 = new ProductBuilder().withName("Orange").withProductType(ProductType.FOOD).build();

        RequestItem requestItem1 = new RequestItemBuilder().withProductData(product1.generateSnapshot())
                                                           .withTotalCost(new Money(BigDecimal.valueOf(100)))
                                                           .build();
        RequestItem requestItem2 = new RequestItemBuilder().withProductData(product2.generateSnapshot())
                                                           .withTotalCost(new Money(BigDecimal.valueOf(200)))
                                                           .build();

        invoiceRequest.add(requestItem1);
        invoiceRequest.add(requestItem2);

        bookKeeper.issuance(invoiceRequest, taxPolicy);

        Mockito.verify(taxPolicy).calculateTax(ProductType.DRUG, requestItem1.getTotalCost());
        Mockito.verify(taxPolicy).calculateTax(ProductType.FOOD, requestItem2.getTotalCost());
        Mockito.verify(taxPolicy, Mockito.times(2)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void expectedZeroCallsOfCalculateTax() {
        bookKeeper.issuance(invoiceRequest, taxPolicy);

        Mockito.verify(taxPolicy, Mockito.times(0)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void expectedAtMostTwoCallsOfGetProductData() {
        Product product = new ProductBuilder().build();

        RequestItem requestItem = Mockito.mock(RequestItem.class);
        Mockito.when(requestItem.getProductData()).thenReturn(product.generateSnapshot());
        Mockito.when(requestItem.getTotalCost()).thenReturn(new Money(BigDecimal.ZERO));
        Mockito.when(requestItem.getQuantity()).thenReturn(0);

        invoiceRequest.add(requestItem);

        bookKeeper.issuance(invoiceRequest, taxPolicy);

        Mockito.verify(requestItem, Mockito.atMost(2)).getProductData();
    }
}
