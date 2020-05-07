package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BookKeeperTest {

    BookKeeper bookKeeper;
    @Mock
    TaxPolicy mockTaxPolicy;


    @Before
    public void setUp() {
        mockTaxPolicy = mock(TaxPolicy.class);
        bookKeeper = new BookKeeper(new InvoiceFactory());
        when(mockTaxPolicy.calculateTax(any(), any())).thenReturn(new Tax(new Money(BigDecimal.ONE), "Tax"));
    }

    @Test
    public void issuance_oneItem() {
        ClientData clientData = new ClientData(Id.generate(), "Client");
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);

        Money money = new Money(BigDecimal.ONE);
        RequestItem requestItem = new RequestItem(new Product(Id.generate(), money, "Apple",
                ProductType.FOOD).generateSnapshot(), 3, money);
        invoiceRequest.add(requestItem);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, mockTaxPolicy);
        assertEquals(1, invoice.getItems().size());
    }

    @Test
    public void issuance_twoItems() {
        ClientData clientData = new ClientData(Id.generate(), "Client");
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);

        Money money1 = new Money(BigDecimal.ONE);
        RequestItem requestItem1 = new RequestItem(new Product(Id.generate(), money1, "Apple",
                ProductType.FOOD).generateSnapshot(), 3, money1);
        invoiceRequest.add(requestItem1);

        Money money2 = new Money(BigDecimal.TEN);
        RequestItem requestItem2 = new RequestItem(new Product(Id.generate(), money2, "Apap",
                ProductType.DRUG).generateSnapshot(), 2, money2);
        invoiceRequest.add(requestItem2);

        bookKeeper.issuance(invoiceRequest, mockTaxPolicy);

        verify(mockTaxPolicy).calculateTax(ProductType.FOOD, money1);
        verify(mockTaxPolicy).calculateTax(ProductType.DRUG, money2);
        verify(mockTaxPolicy, times(2)).calculateTax(any(ProductType.class), any(Money.class));

    }
}