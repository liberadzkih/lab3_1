package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;;

public class BooKeeperTest {
    TaxPolicy taxPolicy;
    Money money;
    Tax tax;
    InvoiceFactory invoiceFactory;
    ClientData clientData;
    InvoiceRequest invoiceRequest;
    Product product;
    ProductData productData;
    RequestItem requestItem;

    @BeforeEach
    void initialize(){
        taxPolicy = mock(TaxPolicy.class);
        money = new Money(BigDecimal.TEN);
        tax = new Tax(money, "Some tax");
        invoiceFactory =new InvoiceFactory();
        clientData = new ClientData(Id.generate(),"Tomasz Nowak");
        invoiceRequest = new InvoiceRequest(clientData);
        product = new Product(Id.generate(),money,"Kiełbasa", ProductType.FOOD);
        productData = product.generateSnapshot();
        requestItem = new RequestItem(productData,2,money);
    }

    @Test
    void shouldReturnInvoiceWithOnePosition(){
        when(taxPolicy.calculateTax(any(),any()))
                .thenReturn(tax);

        BookKeeper bookKeeper = new BookKeeper(invoiceFactory);
        invoiceRequest.add(requestItem);
        Invoice invoice = bookKeeper.issuance(invoiceRequest,taxPolicy);
        assertEquals(1,invoice.getItems().size());
    }

    @Test
    void shouldReturnInvoiceWithoutPosition(){
        when(taxPolicy.calculateTax(any(),any()))
                .thenReturn(tax);

        BookKeeper bookKeeper = new BookKeeper(invoiceFactory);
        Invoice invoice = bookKeeper.issuance(invoiceRequest,taxPolicy);
        assertEquals(0,invoice.getItems().size());
    }

    @Test
    void shouldAddNetToGros(){
        when(taxPolicy.calculateTax(any(),any()))
                .thenReturn(tax);

        BookKeeper bookKeeper = new BookKeeper(invoiceFactory);
        invoiceRequest.add(requestItem);
        Invoice invoice = bookKeeper.issuance(invoiceRequest,taxPolicy);
        Money gros = invoice.getGros();
        Money grosWithoutTax = gros.subtract(tax.getAmount());
        assertEquals(invoice.getNet(),grosWithoutTax);
    }

    @Test
    void shouldUseCalculateTaxTwice(){
        AtomicInteger invocationCount = new AtomicInteger();
        when(taxPolicy.calculateTax(any(),any()))
                .thenAnswer(invocationOnMock ->{
                    invocationCount.getAndIncrement();
                    return tax;
                });

        BookKeeper bookKeeper = new BookKeeper(invoiceFactory);
        invoiceRequest.add(requestItem);
        invoiceRequest.add(requestItem);
        Invoice invoice = bookKeeper.issuance(invoiceRequest,taxPolicy);
        assertEquals(2,invocationCount.get());
    }

    @Test
    void shouldNotUseCalculateTax(){
        AtomicInteger invocationCount = new AtomicInteger();
        when(taxPolicy.calculateTax(any(),any()))
                .thenAnswer(invocationOnMock ->{
                    invocationCount.getAndIncrement();
                    return tax;
                });
        BookKeeper bookKeeper = new BookKeeper(invoiceFactory);
        Invoice invoice = bookKeeper.issuance(invoiceRequest,taxPolicy);
        assertEquals(0,invocationCount.get());
    }

    @Test
    void shouldUseMoneyFromProductData(){
        AtomicReference<Money> moneyAtomicReference = new AtomicReference<>();
        when(taxPolicy.calculateTax(any(),any()))
                .thenAnswer(invocationOnMock ->{
                    Money money = invocationOnMock.getArgument(1,Money.class);
                    moneyAtomicReference.set(money);
                    return tax;
                });
        BookKeeper bookKeeper = new BookKeeper(invoiceFactory);
        invoiceRequest.add(requestItem);
        Invoice invoice = bookKeeper.issuance(invoiceRequest,taxPolicy);
        assertEquals(productData.getPrice(),moneyAtomicReference.get());
    }

}
