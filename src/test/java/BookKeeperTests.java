import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.invoicing.*;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookKeeperTests {

    private TaxPolicy taxMock;
    private Money money;
    private BookKeeper bookKeeper;

    @BeforeEach
    void init() {
        this.money = new Money(1);

        this.bookKeeper = new BookKeeper(new InvoiceFactory());

        this.taxMock = mock(TaxPolicy.class);
        when(this.taxMock.calculateTax(any(), any())).thenReturn(new Tax(this.money, "Tax"));
    }

    private ProductData createProduct(String name, ProductType productType) {
        return new Product(Id.generate(), this.money, name, productType).generateSnapshot();
    }

    //===STATE TESTS===
    @Test
    public void invoiceWithSingleItem_returnInvoiceWithSingleItem() {
        ClientData clientData = new ClientData(Id.generate(), "Example");
        InvoiceRequest request = new InvoiceRequest(clientData);
        request.add(new RequestItem(createProduct("Single item", ProductType.DRUG), 1, this.money));
        Invoice invoice = this.bookKeeper.issuance(request, this.taxMock);

        assertEquals(1, invoice.getItems().size());
    }

    @Test
    public void invoiceWithoutItem_returnInvoiceWithoutAnyItem() {
        ClientData clientData = new ClientData(Id.generate(), "Example");
        InvoiceRequest request = new InvoiceRequest(clientData);
        Invoice invoice = this.bookKeeper.issuance(request, this.taxMock);

        assertEquals(0, invoice.getItems().size());
    }

    @Test
    public void invoiceWithTenItems_returnInvoiceWithTenItems() {
        ClientData clientData = new ClientData(Id.generate(), "Example");
        InvoiceRequest request = new InvoiceRequest(clientData);
        IntStream.range(0, 10).forEach(index -> {
            request.add(new RequestItem(createProduct(String.valueOf(index), ProductType.DRUG), 1, this.money));
        });
        Invoice invoice = this.bookKeeper.issuance(request, this.taxMock);

        assertEquals(10, invoice.getItems().size());
    }

    //===BEHAVIOUR TESTS===
    @Test
    public void invoiceWithTwoItems_calculateTaxCalledTwice() {
        ClientData clientData = new ClientData(Id.generate(), "Example");
        InvoiceRequest request = new InvoiceRequest(clientData);

        RequestItem firstItem = new RequestItem(createProduct("First item", ProductType.FOOD), 1, this.money);
        RequestItem secondItem = new RequestItem(createProduct("Second item", ProductType.STANDARD), 1, this.money);

        request.add(firstItem);
        request.add(secondItem);

        this.bookKeeper.issuance(request, this.taxMock);

        verify(this.taxMock).calculateTax(firstItem.getProductData().getType(), this.money);
        verify(this.taxMock).calculateTax(secondItem.getProductData().getType(), this.money);
        verify(this.taxMock, times(2)).calculateTax(any(), any());
    }

    @Test
    public void invoiceWithoutItem_calculateTaxCalledZeroTimes() {
        ClientData clientData = new ClientData(Id.generate(), "Example");
        InvoiceRequest request = new InvoiceRequest(clientData);

        this.bookKeeper.issuance(request, this.taxMock);

        verify(this.taxMock, times(0)).calculateTax(any(), any());
    }

    @Test
    public void invoiceWithTenItems_calculateTaxCalledTenTimes() {
        ClientData clientData = new ClientData(Id.generate(), "Example");
        InvoiceRequest request = new InvoiceRequest(clientData);

        IntStream.range(0, 10).forEach(index -> {
            RequestItem item = new RequestItem(createProduct(String.valueOf(index), ProductType.FOOD), 1, this.money);
            request.add(item);
        });

        this.bookKeeper.issuance(request, this.taxMock);

        verify(this.taxMock, times(10)).calculateTax(any(), any());
    }
}
