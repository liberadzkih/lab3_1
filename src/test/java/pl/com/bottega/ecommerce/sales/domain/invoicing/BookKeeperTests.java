package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookKeeperTests {
    private Tax tax;
    private TaxPolicy taxPolicy;
    private BookKeeper bookKeeper;
    private ClientData clientData;

    @BeforeEach
    public void prepareTest() {
        tax = mock(Tax.class);
        taxPolicy = mock(TaxPolicy.class);
        bookKeeper = new BookKeeper(new InvoiceFactory());
        clientData = new ClientData(Id.generate(), "ClientName");
        // Tax
        when(tax.getAmount()).thenReturn(Money.ZERO);
        // TaxPolicy
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(Money.ZERO, "tax"));
    }

    // STATE TESTS
    @Test
    public void singlePositionIssuanceReturnsSinglePositionInvoice() {
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);
        Money money = new Money(100);
        Product product = new Product(Id.generate(), money, "Facemask", ProductType.STANDARD);
        ProductData productData = product.generateSnapshot();
        RequestItem requestItem = new RequestItem(productData, 1, money);
        invoiceRequest.add(requestItem);

         assertEquals(1, bookKeeper.issuance(invoiceRequest, taxPolicy).getItems().size());
    }

    // BEHAVIORAL TESTS
    @Test
    public void calculateTaxShouldBeCalledTwoTimes() {
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);
        Money money = new Money(100);
        Product product = new Product(Id.generate(), money, "Facemask", ProductType.STANDARD);
        ProductData productData = product.generateSnapshot();
        RequestItem requestItem = new RequestItem(productData, 1, money);
        invoiceRequest.add(requestItem);
        invoiceRequest.add(requestItem);

        bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(2)).calculateTax(any(ProductType.class), any(Money.class));
    }
}
