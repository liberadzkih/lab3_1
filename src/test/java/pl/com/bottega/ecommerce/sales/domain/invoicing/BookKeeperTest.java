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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class BookKeeperTest {
    InvoiceFactory invoiceFactory;
    TaxPolicy taxPolicy;
    InvoiceRequest invoiceRequest;
    BookKeeper bookKeeper;

    @Before
    public void init() {
        invoiceFactory = new InvoiceFactory();
        bookKeeper = new BookKeeper(invoiceFactory);
        taxPolicy = Mockito.mock(TaxPolicy.class);
        invoiceRequest = new InvoiceRequest(new ClientData(Id.generate(), "Jan Nowak"));

        Mockito.when(taxPolicy.calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class))).thenReturn(new Tax(Money.ZERO, "tax"));
    }

    //State test
    @Test
    public void shouldReturnInvoiceWithOnePosition() {
        Money money = new Money(BigDecimal.valueOf(50));
        Product product = new ProductBuilder().withName("Orange").withProductType(ProductType.FOOD).withPrice(money).build();
        RequestItem requestItem = new RequestItemBuilder().withProductData(product.generateSnapshot()).withQuantity(2).withTotalCost(money).build();
        invoiceRequest.add(requestItem);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(1, equalTo(invoice.getItems().size()));
    }

    @Test
    public void shouldReturnInvoiceWithoutPosition() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(0, equalTo(invoice.getItems().size()));
    }

    @Test
    public void shouldReturnInvoiceWithTwoPosition() {
        Money money = new Money(BigDecimal.valueOf(50));
        Product product = new ProductBuilder().withName("Orange").withProductType(ProductType.FOOD).withPrice(money).build();
        RequestItem requestItem = new RequestItemBuilder().withProductData(product.generateSnapshot()).withQuantity(2).withTotalCost(money).build();
        invoiceRequest.add(requestItem);

        Money secondMoney = new Money(BigDecimal.valueOf(36));
        Product secondProduct = new ProductBuilder().withName("Phone").withPrice(secondMoney).build();
        RequestItem secondRequestItem = new RequestItemBuilder().withProductData(secondProduct.generateSnapshot()).withTotalCost(secondMoney).build();
        invoiceRequest.add(secondRequestItem);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(2, equalTo(invoice.getItems().size()));
    }

    //Behavior test
    @Test
    public void shouldCallCalculateTaxMethodTwice() {
        Money money = new Money(BigDecimal.valueOf(50));
        Product product = new ProductBuilder().withName("Orange").withProductType(ProductType.FOOD).withPrice(money).build();
        RequestItem requestItem = new RequestItemBuilder().withProductData(product.generateSnapshot()).withQuantity(2).withTotalCost(money).build();
        invoiceRequest.add(requestItem);

        Money secondMoney = new Money(BigDecimal.valueOf(36));
        Product secondProduct = new ProductBuilder().withName("Phone").withPrice(secondMoney).build();
        RequestItem secondRequestItem = new RequestItemBuilder().withProductData(secondProduct.generateSnapshot()).withTotalCost(secondMoney).build();
        invoiceRequest.add(secondRequestItem);

        bookKeeper.issuance(invoiceRequest, taxPolicy);
        Mockito.verify(taxPolicy, Mockito.times(2)).calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class));
    }

    @Test
    public void shouldCallCalculateTaxMethodAtLeastOnce() {
        Money money = new Money(BigDecimal.valueOf(50));
        Product product = new ProductBuilder().withName("Orange").withProductType(ProductType.FOOD).withPrice(money).build();
        RequestItem requestItem = new RequestItemBuilder().withProductData(product.generateSnapshot()).withQuantity(2).withTotalCost(money).build();
        invoiceRequest.add(requestItem);

        bookKeeper.issuance(invoiceRequest, taxPolicy);
        Mockito.verify(taxPolicy, Mockito.atLeastOnce()).calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class));
    }

    @Test
    public void shouldNeverCallCalculateTaxMethod() {
        Mockito.when(taxPolicy.calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class))).thenReturn(new Tax(Money.ZERO, "tax"));
        Mockito.verify(taxPolicy, Mockito.never()).calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class));
    }
}
