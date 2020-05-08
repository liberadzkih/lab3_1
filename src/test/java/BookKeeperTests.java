import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.invoicing.*;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
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

    //===STATE TESTS===
    @Test
    public void invoiceWithSingleItem_returnInvoiceWithSingleItem() {
        ClientData clientData = new ClientData(Id.generate(), "Example");
        InvoiceRequest request = new InvoiceRequest(clientData);
        request.add(new RequestItem(new Product(Id.generate(), this.money, "Single item", ProductType.DRUG).generateSnapshot(), 1, this.money));
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
            request.add(new RequestItem(new Product(Id.generate(), this.money, String.valueOf(index), ProductType.DRUG).generateSnapshot(), 1, this.money));
        });
        Invoice invoice = this.bookKeeper.issuance(request, this.taxMock);

        assertEquals(10, invoice.getItems().size());
    }

    //===BEHAVIOUR TESTS===
    @Test
    public void invoiceWithTwoItems_calculateTaxCalledTwice() {
        ClientData clientData = new ClientData(Id.generate(), "Example");
        InvoiceRequest request = new InvoiceRequest(clientData);

        RequestItem firstItem = new RequestItem(new Product(Id.generate(), this.money, "First item", ProductType.FOOD).generateSnapshot(), 1, this.money);
        RequestItem secondItem = new RequestItem(new Product(Id.generate(), this.money, "Second item", ProductType.STANDARD).generateSnapshot(), 1, this.money);

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

        IntStream.range(0, 10).forEach(index ->{
            RequestItem item = new RequestItem(new Product(Id.generate(), this.money, String.valueOf(index), ProductType.FOOD).generateSnapshot(), 1, this.money);
            request.add(item);
        });

        this.bookKeeper.issuance(request, this.taxMock);

        verify(this.taxMock, times(10)).calculateTax(any(), any());
    }
}
