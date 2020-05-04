package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
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
    }

    //State test
    @Test
    public void shouldReturnInvoiceWithOnePosition() {
        Money money = new Money(BigDecimal.valueOf(50));
        Product product = new Product(Id.generate(), money, "Orange", ProductType.FOOD);
        ProductData productData = product.generateSnapshot();
        RequestItem requestItem = new RequestItem(productData, 2, money);
        invoiceRequest.add(requestItem);

        Mockito.when(taxPolicy.calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class))).thenReturn(new Tax(Money.ZERO, "tax"));

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(1, equalTo(invoice.getItems().size()));
    }

    @Test
    public void shouldReturnInvoiceWithoutPosition() {
        Mockito.when(taxPolicy.calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class))).thenReturn(new Tax(Money.ZERO, "tax"));

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(0, equalTo(invoice.getItems().size()));
    }

    @Test
    public void shouldReturnInvoiceWithTwoPosition(){
        Money firstMoney = new Money(BigDecimal.valueOf(50));
        Product firstProduct = new Product(Id.generate(), firstMoney, "Orange", ProductType.FOOD);
        ProductData firstProductData = firstProduct.generateSnapshot();
        RequestItem firstRequestItem = new RequestItem(firstProductData, 2, firstMoney);
        invoiceRequest.add(firstRequestItem);

        Money secondMoney = new Money(BigDecimal.valueOf(36));
        Product secondProduct = new Product(Id.generate(), secondMoney, "Phone", ProductType.STANDARD);
        ProductData secondProductData = secondProduct.generateSnapshot();
        RequestItem secondRequestItem = new RequestItem(secondProductData, 2, secondMoney);
        invoiceRequest.add(secondRequestItem);

        Mockito.when(taxPolicy.calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class))).thenReturn(new Tax(Money.ZERO, "tax"));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(2, equalTo(invoice.getItems().size()));
    }

    //Behavior test
    @Test
    public void shouldCallCalculateTaxMethodTwice() {
        Money firstMoney = new Money(BigDecimal.valueOf(50));
        Product firstProduct = new Product(Id.generate(), firstMoney, "Orange", ProductType.FOOD);
        ProductData firstProductData = firstProduct.generateSnapshot();
        RequestItem firstRequestItem = new RequestItem(firstProductData, 2, firstMoney);
        invoiceRequest.add(firstRequestItem);

        Money secondMoney = new Money(BigDecimal.valueOf(36));
        Product secondProduct = new Product(Id.generate(), secondMoney, "Phone", ProductType.STANDARD);
        ProductData secondProductData = secondProduct.generateSnapshot();
        RequestItem secondRequestItem = new RequestItem(secondProductData, 2, secondMoney);
        invoiceRequest.add(secondRequestItem);

        Mockito.when(taxPolicy.calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class))).thenReturn(new Tax(Money.ZERO, "tax"));
        bookKeeper.issuance(invoiceRequest, taxPolicy);

        Mockito.verify(taxPolicy, Mockito.times(2)).calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class));
    }
}
