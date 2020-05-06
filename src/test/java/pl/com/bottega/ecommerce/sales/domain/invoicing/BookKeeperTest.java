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

    @BeforeEach
    public void init() {
        bookKeeper = new BookKeeper(new InvoiceFactory());
        invoiceRequest = new InvoiceRequest(new ClientData(Id.generate(),"Imie Nazwisko"));
        taxPolicy = Mockito.mock(TaxPolicy.class);
        Mockito.when(taxPolicy.calculateTax(any(ProductType.class),
                any(Money.class))).thenReturn(new Tax(Money.ZERO, "tax"));
    }

    //State tests

    @Test
    public void shouldReturnInvoiceWithOneElement() {
        Product product = new Product(Id.generate(),Money.ZERO,"name",ProductType.STANDARD);
        RequestItem requestItem = new RequestItem(product.generateSnapshot(),1, Money.ZERO);
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
        Product product1 = new Product(Id.generate(),Money.ZERO,"bike",ProductType.STANDARD);
        RequestItem requestItem1 = new RequestItem(product1.generateSnapshot(),5, Money.ZERO);
        invoiceRequest.add(requestItem1);

        Product product2 = new Product(Id.generate(),Money.ZERO,"vitamin",ProductType.DRUG);
        RequestItem requestItem2 = new RequestItem(product2.generateSnapshot(),1, Money.ZERO);
        invoiceRequest.add(requestItem2);

        Product product3 = new Product(Id.generate(),Money.ZERO,"bread",ProductType.FOOD);
        RequestItem requestItem3 = new RequestItem(product3.generateSnapshot(),2, Money.ZERO);
        invoiceRequest.add(requestItem3);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        Assertions.assertEquals(3, invoice.getItems().size());
    }

    //Behaviour Tests

    @Test
    public void calculateTaxShouldBeCalledTwoTimes() {
        Product product1 = new Product(Id.generate(),Money.ZERO,"bike",ProductType.STANDARD);
        RequestItem requestItem1 = new RequestItem(product1.generateSnapshot(),5, new Money(10));
        invoiceRequest.add(requestItem1);

        Product product2 = new Product(Id.generate(),Money.ZERO,"vitamin",ProductType.DRUG);
        RequestItem requestItem2 = new RequestItem(product2.generateSnapshot(),1, new Money(13));
        invoiceRequest.add(requestItem2);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(2)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void calculateTaxShouldNotBeCalled() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(0)).calculateTax(any(ProductType.class), any(Money.class));
    }

}